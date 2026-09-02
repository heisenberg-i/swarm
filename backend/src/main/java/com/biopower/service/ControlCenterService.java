package com.biopower.service;

import com.biopower.dto.request.ApproveUserRequest;
import com.biopower.dto.request.AssignPlantsRequest;
import com.biopower.dto.request.CompanyRequest;
import com.biopower.dto.request.PlantRequest;
import com.biopower.dto.request.SuperAdminRequest;
import com.biopower.dto.response.CompanyDetailResponse;
import com.biopower.dto.response.CompanyResponse;
import com.biopower.dto.response.ControlCenterOverviewResponse;
import com.biopower.dto.response.ControlCenterSensorNodeResponse;
import com.biopower.dto.response.ControlCenterUserResponse;
import com.biopower.dto.response.PlantResponse;
import com.biopower.dto.response.UserResponse;
import com.biopower.exception.BadRequestException;
import com.biopower.exception.ResourceNotFoundException;
import com.biopower.model.entity.Company;
import com.biopower.model.entity.Plant;
import com.biopower.model.entity.SensorNode;
import com.biopower.model.entity.User;
import com.biopower.model.enums.CompanyStatus;
import com.biopower.model.enums.PlantStatus;
import com.biopower.model.enums.UserRole;
import com.biopower.model.enums.UserStatus;
import com.biopower.repository.CompanyRepository;
import com.biopower.repository.PlantRepository;
import com.biopower.repository.SensorNodeRepository;
import com.biopower.repository.UserRepository;
import com.biopower.util.PlantSensorTypes;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ControlCenterService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PlantRepository plantRepository;
    private final SensorNodeRepository sensorNodeRepository;
    private final PlantService plantService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<CompanyResponse> listCompanies() {
        return companyRepository.findAll().stream().map(this::toCompanyResponse).collect(Collectors.toList());
    }

    @Transactional
    public CompanyResponse createCompany(CompanyRequest request) {
        String name = request.getName().trim();
        if (companyRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Company name already exists");
        }
        Company company = Company.builder()
                .name(name)
                .status(CompanyStatus.ACTIVE)
                .build();
        return toCompanyResponse(companyRepository.save(company));
    }

    @Transactional
    public CompanyResponse setCompanyStatus(Long id, CompanyStatus status) {
        Company company = findCompany(id);
        company.setStatus(status);
        return toCompanyResponse(companyRepository.save(company));
    }

    @Transactional(readOnly = true)
    public ControlCenterOverviewResponse overview() {
        List<CompanyResponse> companies = listCompanies();
        long unassigned = plantRepository.findByCompanyIsNull().size();
        long plantsTotal = plantRepository.count();
        return ControlCenterOverviewResponse.builder()
                .companiesTotal(companies.size())
                .companiesActive(companies.stream().filter(c -> c.getStatus() == CompanyStatus.ACTIVE).count())
                .companiesDisabled(companies.stream().filter(c -> c.getStatus() == CompanyStatus.DISABLED).count())
                .tenantsReady(companies.stream().filter(c -> "READY".equals(c.getOnboardingState())).count())
                .tenantsNeedAdmin(companies.stream().filter(c -> "NEEDS_ADMIN".equals(c.getOnboardingState())).count())
                .tenantsNeedPlants(companies.stream().filter(c -> "NEEDS_PLANTS".equals(c.getOnboardingState())).count())
                .plantsTotal(plantsTotal)
                .plantsUnassigned(unassigned)
                .plantsAssigned(plantsTotal - unassigned)
                .pendingApprovals(countPendingApprovals())
                .build();
    }

    @Transactional(readOnly = true)
    public CompanyDetailResponse getCompany(Long id) {
        Company company = findCompany(id);
        User superAdmin = userRepository.findByCompanyAndRole(company, UserRole.SUPER_ADMIN).orElse(null);
        List<PlantResponse> plantResponses = plantRepository.findByCompany(company).stream()
                .map(p -> plantService.getPlantById(p.getPlantId()))
                .collect(Collectors.toList());
        List<User> companyUsers = userRepository.findByCompany(company).stream()
                .filter(u -> u.getRole() != UserRole.CONTROL_CENTER_ADMIN)
                .collect(Collectors.toList());
        companyUsers.forEach(u -> {
            u.getAssignedPlants().size();
            u.getAssignedSensorNodes().size();
        });
        List<UserResponse> users = companyUsers.stream().map(this::toUserResponse).collect(Collectors.toList());
        return CompanyDetailResponse.builder()
                .company(toCompanyResponse(company))
                .superAdmin(superAdmin == null ? null : toUserResponse(superAdmin))
                .plants(plantResponses)
                .users(users)
                .sensors(listSensorsForCompany(visiblePlantsForSensors(company), companyUsers))
                .build();
    }

    private List<Plant> visiblePlantsForSensors(Company company) {
        List<Plant> plants = new ArrayList<>(plantRepository.findByCompany(company));
        plants.addAll(plantRepository.findByCompanyIsNull());
        return plants;
    }

    @Transactional
    public UserResponse createSuperAdmin(Long companyId, SuperAdminRequest request) {
        Company company = findCompany(companyId);
        if (company.getStatus() == CompanyStatus.DISABLED) {
            throw new BadRequestException("Company is disabled");
        }
        userRepository.findByCompanyAndRole(company, UserRole.SUPER_ADMIN).ifPresent(existing -> {
            throw new BadRequestException("This company already has a Super Admin: " + existing.getEmail());
        });
        String email = request.getEmail().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }
        List<Plant> plants = plantRepository.findByCompany(company);
        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .mobile(request.getMobile())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.SUPER_ADMIN)
                .status(UserStatus.ACTIVE)
                .company(company)
                .assignedPlants(new HashSet<>(plants))
                .assignedSensorNodes(new HashSet<>())
                .build();
        return toUserResponse(userRepository.save(user));
    }

    @Transactional
    public CompanyDetailResponse assignPlants(Long companyId, AssignPlantsRequest request) {
        Company company = findCompany(companyId);
        List<Long> plantIds = request.getPlantIds() == null ? List.of() : request.getPlantIds();
        List<Plant> currentlyAssigned = plantRepository.findByCompany(company);
        Set<Long> nextIds = new HashSet<>(plantIds);

        for (Plant plant : currentlyAssigned) {
            if (!nextIds.contains(plant.getPlantId())) {
                unlinkPlantFromCompanyUsers(plant, company);
                plant.setCompany(null);
                plantRepository.save(plant);
            }
        }

        for (Long plantId : nextIds) {
            Plant plant = plantService.findPlant(plantId);
            if (plant.getCompany() != null && !plant.getCompany().getId().equals(company.getId())) {
                throw new BadRequestException("Plant already assigned to another company: " + plant.getPlantName());
            }
            plant.setCompany(company);
            plantRepository.save(plant);
        }

        User superAdmin = userRepository.findByCompanyAndRole(company, UserRole.SUPER_ADMIN).orElse(null);
        if (superAdmin != null) {
            superAdmin.getAssignedPlants().clear();
            superAdmin.getAssignedPlants().addAll(plantRepository.findByCompany(company));
            userRepository.save(superAdmin);
        }
        return getCompany(companyId);
    }

    @Transactional(readOnly = true)
    public List<PlantResponse> listUnassignedPlants() {
        return plantRepository.findByCompanyIsNull().stream()
                .map(p -> plantService.getPlantById(p.getPlantId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlantResponse> listAllPlants() {
        return plantRepository.findAll().stream()
                .map(p -> plantService.getPlantById(p.getPlantId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public PlantResponse createUnassignedPlant(PlantRequest request) {
        Plant plant = Plant.builder()
                .plantName(request.getPlantName())
                .plantType(request.getPlantType())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .feedstockType(request.getFeedstockType())
                .installationDate(request.getInstallationDate())
                .status(request.getStatus() != null ? request.getStatus() : PlantStatus.ACTIVE)
                .enabledSensorTypes(PlantSensorTypes.serialize(
                        Set.copyOf(PlantSensorTypes.parseList(request.getEnabledSensorTypes()))))
                .build();
        plant = plantRepository.save(plant);
        return plantService.getPlantById(plant.getPlantId());
    }

    @Transactional(readOnly = true)
    public List<ControlCenterUserResponse> listUsers(boolean queueOnly) {
        return userRepository.findAll().stream()
                .map(this::toCcUserResponse)
                .filter(u -> u.getRole() != UserRole.CONTROL_CENTER_ADMIN)
                .filter(u -> !queueOnly || isApprovalQueue(u))
                .collect(Collectors.toList());
    }

    @Transactional
    public ControlCenterUserResponse approveUser(Long userId, ApproveUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (user.getRole() == UserRole.CONTROL_CENTER_ADMIN) {
            throw new BadRequestException("Cannot approve a Control Center admin here");
        }
        if (request.getRole() == UserRole.CONTROL_CENTER_ADMIN) {
            throw new BadRequestException("Cannot grant Control Center admin via approval");
        }
        Company company = findCompany(request.getCompanyId());
        if (company.getStatus() == CompanyStatus.DISABLED) {
            company.setStatus(CompanyStatus.ACTIVE);
            company = companyRepository.save(company);
        }
        UserRole role = request.getRole();
        if (role == UserRole.SUPER_ADMIN) {
            var existingAdmin = userRepository.findByCompanyAndRole(company, UserRole.SUPER_ADMIN);
            if (existingAdmin.isPresent() && !existingAdmin.get().getId().equals(user.getId())) {
                role = UserRole.PLANT_ADMIN;
            }
        }
        List<Plant> companyPlants = plantRepository.findByCompany(company);
        List<Plant> assigned;
        if (role == UserRole.SUPER_ADMIN || request.getPlantIds() == null || request.getPlantIds().isEmpty()) {
            assigned = companyPlants;
        } else {
            assigned = companyPlants.stream()
                    .filter(p -> request.getPlantIds().contains(p.getPlantId()))
                    .collect(Collectors.toList());
            if (assigned.isEmpty()) {
                throw new BadRequestException("Select plants that belong to this tenant");
            }
        }
        user.setCompany(company);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.getAssignedPlants().clear();
        user.getAssignedPlants().addAll(assigned);
        user.getAssignedSensorNodes().clear();
        return toCcUserResponse(userRepository.save(user));
    }

    @Transactional
    public ControlCenterUserResponse rejectUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (user.getRole() == UserRole.CONTROL_CENTER_ADMIN) {
            throw new BadRequestException("Cannot reject a Control Center admin");
        }
        user.setStatus(UserStatus.DISABLED);
        return toCcUserResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (user.getRole() == UserRole.CONTROL_CENTER_ADMIN) {
            throw new BadRequestException("Cannot delete a Control Center admin");
        }
        user.getAssignedPlants().clear();
        user.getAssignedSensorNodes().clear();
        userRepository.save(user);
        userRepository.delete(user);
    }

    @Transactional
    public void deleteCompany(Long companyId) {
        Company company = findCompany(companyId);
        for (Plant plant : plantRepository.findByCompany(company)) {
            plant.setCompany(null);
            plantRepository.save(plant);
        }
        for (User user : userRepository.findByCompany(company)) {
            user.setCompany(null);
            user.getAssignedPlants().clear();
            user.getAssignedSensorNodes().clear();
            userRepository.save(user);
        }
        companyRepository.delete(company);
    }

    private long countPendingApprovals() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() != UserRole.CONTROL_CENTER_ADMIN)
                .filter(this::isApprovalQueueUser)
                .count();
    }

    private boolean isApprovalQueueUser(User user) {
        return user.getStatus() == UserStatus.PENDING;
    }

    private boolean isApprovalQueue(ControlCenterUserResponse user) {
        return user.getStatus() == UserStatus.PENDING;
    }

    private ControlCenterUserResponse toCcUserResponse(User user) {
        user.getAssignedPlants().size();
        return ControlCenterUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(user.getRole())
                .status(user.getStatus())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .companyName(user.getCompany() != null ? user.getCompany().getName() : null)
                .plantIds(user.getAssignedPlants().stream().map(Plant::getPlantId).collect(Collectors.toList()))
                .queueReason(queueReason(user))
                .createdAt(user.getCreatedAt())
                .build();
    }

    private String queueReason(User user) {
        if (user.getStatus() == UserStatus.PENDING) {
            return "PENDING_SIGNUP";
        }
        if (user.getCompany() == null && user.getStatus() != UserStatus.DISABLED) {
            return "NO_TENANT";
        }
        return null;
    }

    private void unlinkPlantFromCompanyUsers(Plant plant, Company company) {
        for (User user : userRepository.findByCompany(company)) {
            if (user.getAssignedPlants().removeIf(p -> p.getPlantId().equals(plant.getPlantId()))) {
                user.getAssignedSensorNodes().removeIf(n -> n.getPlant().getPlantId().equals(plant.getPlantId()));
                userRepository.save(user);
            }
        }
    }

    private Company findCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
    }

    private CompanyResponse toCompanyResponse(Company company) {
        User superAdmin = userRepository.findByCompanyAndRole(company, UserRole.SUPER_ADMIN).orElse(null);
        int plantCount = plantRepository.findByCompany(company).size();
        int userCount = (int) userRepository.countByCompany(company);
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .status(company.getStatus())
                .superAdminEmail(superAdmin != null ? superAdmin.getEmail() : null)
                .superAdminName(superAdmin != null ? superAdmin.getName() : null)
                .plantCount(plantCount)
                .userCount(userCount)
                .onboardingState(onboardingState(company.getStatus(), superAdmin != null, plantCount))
                .createdAt(company.getCreatedAt())
                .build();
    }

    private String onboardingState(CompanyStatus status, boolean hasAdmin, int plantCount) {
        if (status == CompanyStatus.DISABLED) {
            return "DISABLED";
        }
        if (!hasAdmin) {
            return "NEEDS_ADMIN";
        }
        if (plantCount == 0) {
            return "NEEDS_PLANTS";
        }
        return "READY";
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(user.getRole())
                .status(user.getStatus())
                .plantIds(user.getAssignedPlants().stream().map(Plant::getPlantId).collect(Collectors.toList()))
                .nodeIds(user.getAssignedSensorNodes().stream().map(SensorNode::getNodeId).collect(Collectors.toList()))
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }

    private List<ControlCenterSensorNodeResponse> listSensorsForCompany(List<Plant> plants, List<User> users) {
        List<ControlCenterSensorNodeResponse> rows = new ArrayList<>();
        for (Plant plant : plants) {
            for (SensorNode node : sensorNodeRepository.findByPlantPlantId(plant.getPlantId())) {
                List<ControlCenterSensorNodeResponse.Grant> grants = new ArrayList<>();
                for (User user : users) {
                    String scope = sensorAccessScope(user, plant, node);
                    if (scope == null) {
                        continue;
                    }
                    grants.add(ControlCenterSensorNodeResponse.Grant.builder()
                            .userId(user.getId())
                            .name(user.getName())
                            .email(user.getEmail())
                            .role(user.getRole())
                            .scope(scope)
                            .build());
                }
                boolean paired = node.getDeviceChipId() != null && !node.getDeviceChipId().isBlank();
                rows.add(ControlCenterSensorNodeResponse.builder()
                        .nodeId(node.getNodeId())
                        .nodeName(node.getNodeName())
                        .sensorType(node.getSensorType())
                        .status(node.getStatus())
                        .plantId(plant.getPlantId())
                        .plantName(plant.getPlantName())
                        .paired(paired)
                        .hasReading(node.getLastReadingAt() != null)
                        .lastReadingAt(node.getLastReadingAt())
                        .access(grants)
                        .build());
            }
        }
        return rows;
    }

    private String sensorAccessScope(User user, Plant plant, SensorNode node) {
        boolean plantAssigned = user.getAssignedPlants().stream()
                .anyMatch(p -> p.getPlantId().equals(plant.getPlantId()));
        if (user.getRole() == UserRole.SUPER_ADMIN || user.getRole() == UserRole.PLANT_ADMIN) {
            return plantAssigned ? "PLANT" : null;
        }
        if (user.getRole() == UserRole.OPERATOR) {
            boolean nodeAssigned = user.getAssignedSensorNodes().stream()
                    .anyMatch(n -> n.getNodeId().equals(node.getNodeId()));
            if (nodeAssigned) {
                return "NODE";
            }
            if (plantAssigned && user.getAssignedSensorNodes().isEmpty()) {
                return "PLANT";
            }
        }
        return null;
    }
}
