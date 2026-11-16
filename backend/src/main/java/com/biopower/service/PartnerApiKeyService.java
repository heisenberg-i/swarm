// PURPOSE: Application service implementing SWARM partnerapikey business rules.
package com.biopower.service;

import com.biopower.dto.request.PartnerApiKeyRequest;
import com.biopower.dto.request.PartnerOrganizationRequest;
import com.biopower.dto.response.PartnerApiKeyCreatedResponse;
import com.biopower.dto.response.PartnerApiKeyResponse;
import com.biopower.dto.response.PartnerOrganizationResponse;
import com.biopower.exception.BadRequestException;
import com.biopower.exception.ResourceNotFoundException;
import com.biopower.model.entity.PartnerApiKey;
import com.biopower.model.entity.PartnerOrganization;
import com.biopower.model.entity.Plant;
import com.biopower.repository.PartnerApiKeyRepository;
import com.biopower.repository.PartnerOrganizationRepository;
import com.biopower.repository.PlantRepository;
import com.biopower.security.PartnerPrincipal;
import com.biopower.util.ApiKeyHasher;
import com.biopower.util.ExternalOrgIds;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnerApiKeyService {

    private final PartnerOrganizationRepository organizationRepository;
    private final PartnerApiKeyRepository apiKeyRepository;
    private final PlantRepository plantRepository;

    @Transactional
    public Optional<PartnerPrincipal> authenticate(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return Optional.empty();
        }
        String hash = ApiKeyHasher.hash(rawKey.trim());
        Optional<PartnerApiKey> keyOpt = apiKeyRepository.findByKeyHash(hash).filter(PartnerApiKey::isUsable);
        if (keyOpt.isEmpty()) {
            return Optional.empty();
        }
        PartnerApiKey key = keyOpt.get();
        key.setLastUsedAt(LocalDateTime.now());
        apiKeyRepository.save(key);
        PartnerOrganization org = key.getOrganization();
        if (!org.isActive()) {
            return Optional.empty();
        }
        Set<Long> plantIds = org.getAllowedPlants().stream()
                .map(Plant::getPlantId)
                .collect(Collectors.toSet());
        return Optional.of(new PartnerPrincipal(
                org.getId(),
                org.getExternalOrgId(),
                org.getName(),
                key.getId(),
                plantIds
        ));
    }

    @Transactional
    public PartnerOrganizationResponse createOrganization(PartnerOrganizationRequest request) {
        String externalOrgId = requireExternalOrgId(request.getExternalOrgId());
        if (organizationRepository.existsByExternalOrgId(externalOrgId)) {
            throw new BadRequestException("Organization already exists: " + externalOrgId);
        }
        PartnerOrganization org = PartnerOrganization.builder()
                .name(request.getName().trim())
                .externalOrgId(externalOrgId)
                .active(true)
                .allowedPlants(resolvePlants(request.getPlantIds()))
                .build();
        return toOrganizationResponse(organizationRepository.save(org));
    }

    @Transactional
    public PartnerOrganizationResponse updateOrganization(Long id, PartnerOrganizationRequest request) {
        PartnerOrganization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner organization not found"));
        String externalOrgId = requireExternalOrgId(request.getExternalOrgId());
        organizationRepository.findByExternalOrgId(externalOrgId)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException("Organization already exists: " + externalOrgId);
                });
        org.setName(request.getName().trim());
        org.setExternalOrgId(externalOrgId);
        org.setAllowedPlants(resolvePlants(request.getPlantIds()));
        return toOrganizationResponse(organizationRepository.save(org));
    }

    private String requireExternalOrgId(String raw) {
        String externalOrgId = ExternalOrgIds.normalize(raw);
        if (externalOrgId.isEmpty()) {
            throw new BadRequestException(
                    "emPOWER organization ID is required. Copy it from emPOWER Settings "
                            + "(“This tenant's emPOWER organization ID is …”), e.g. 001. "
                            + "Do not invent slugs such as org_vulcan_mozambique.");
        }
        return externalOrgId;
    }

    @Transactional
    public PartnerApiKeyCreatedResponse createApiKey(PartnerApiKeyRequest request) {
        PartnerOrganization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner organization not found"));
        String rawKey = ApiKeyHasher.generateRawKey(request.getName());
        PartnerApiKey apiKey = PartnerApiKey.builder()
                .organization(org)
                .name(request.getName().trim())
                .keyHash(ApiKeyHasher.hash(rawKey))
                .keyPrefix(ApiKeyHasher.prefixFor(rawKey))
                .active(true)
                .build();
        PartnerApiKey saved = apiKeyRepository.save(apiKey);
        return PartnerApiKeyCreatedResponse.builder()
                .id(saved.getId())
                .organizationId(org.getId())
                .name(saved.getName())
                .keyPrefix(saved.getKeyPrefix())
                .apiKey(rawKey)
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional
    public PartnerApiKeyCreatedResponse createApiKeyForOrganization(PartnerOrganization org, String name, String rawKey) {
        PartnerApiKey apiKey = PartnerApiKey.builder()
                .organization(org)
                .name(name)
                .keyHash(ApiKeyHasher.hash(rawKey))
                .keyPrefix(ApiKeyHasher.prefixFor(rawKey))
                .active(true)
                .build();
        PartnerApiKey saved = apiKeyRepository.save(apiKey);
        return PartnerApiKeyCreatedResponse.builder()
                .id(saved.getId())
                .organizationId(org.getId())
                .name(saved.getName())
                .keyPrefix(saved.getKeyPrefix())
                .apiKey(rawKey)
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional
    public void revokeApiKey(Long keyId) {
        PartnerApiKey key = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new ResourceNotFoundException("API key not found"));
        key.setActive(false);
        key.setRevokedAt(LocalDateTime.now());
        apiKeyRepository.save(key);
    }

    @Transactional(readOnly = true)
    public List<PartnerOrganizationResponse> listOrganizations() {
        return organizationRepository.findAll().stream()
                .map(this::toOrganizationResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PartnerApiKeyResponse> listApiKeys(Long organizationId) {
        if (organizationId == null) {
            throw new BadRequestException("organizationId is required");
        }
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Partner organization not found");
        }
        return apiKeyRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId).stream()
                .map(this::toApiKeyResponse)
                .collect(Collectors.toList());
    }

    private Set<Plant> resolvePlants(List<Long> plantIds) {
        if (plantIds == null || plantIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Plant> plants = plantRepository.findAllById(plantIds);
        if (plants.size() != plantIds.size()) {
            throw new BadRequestException("One or more plant IDs are invalid");
        }
        return new HashSet<>(plants);
    }

    private PartnerOrganizationResponse toOrganizationResponse(PartnerOrganization org) {
        return PartnerOrganizationResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .externalOrgId(org.getExternalOrgId())
                .active(org.isActive())
                .plantIds(org.getAllowedPlants().stream().map(Plant::getPlantId).toList())
                .createdAt(org.getCreatedAt())
                .build();
    }

    private PartnerApiKeyResponse toApiKeyResponse(PartnerApiKey key) {
        return PartnerApiKeyResponse.builder()
                .id(key.getId())
                .organizationId(key.getOrganization().getId())
                .name(key.getName())
                .keyPrefix(key.getKeyPrefix())
                .active(key.isActive())
                .expiresAt(key.getExpiresAt())
                .revokedAt(key.getRevokedAt())
                .lastUsedAt(key.getLastUsedAt())
                .createdAt(key.getCreatedAt())
                .build();
    }
}
