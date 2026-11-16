// PURPOSE: Application service implementing SWARM user business rules.
package com.biopower.service;

import com.biopower.dto.request.UserRequest;
import com.biopower.dto.response.UserResponse;
import com.biopower.exception.BadRequestException;
import com.biopower.exception.ResourceNotFoundException;
import com.biopower.model.entity.Company;
import com.biopower.model.entity.Plant;
import com.biopower.model.entity.SensorNode;
import com.biopower.model.entity.User;
import com.biopower.model.enums.UserRole;
import com.biopower.model.enums.UserStatus;
import com.biopower.repository.CompanyRepository;
import com.biopower.repository.PlantRepository;
import com.biopower.repository.SensorNodeRepository;
import com.biopower.repository.UserRepository;
import com.biopower.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PlantRepository plantRepository;
    private final SensorNodeRepository sensorNodeRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlantAccessService plantAccessService;

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersForAdmin(UserPrincipal principal) {
        plantAccessService.assertPlantAppUser(principal);
        if (plantAccessService.isSuperAdmin(principal)) {
            Company company = requireCompany(principal);
            return userRepository.findByCompany(company).stream()
                    .filter(user -> user.getRole() != UserRole.CONTROL_CENTER_ADMIN)
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        return userRepository.findAll().stream()
                .filter(this::isManageableByPlantAdmin)
                .filter(user -> canPlantAdminAccessUser(principal, user))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id, UserPrincipal principal) {
        User user = findUser(id);
        assertCanManage(principal, user);
        return toResponse(user);
    }

    @Transactional
    public UserResponse createUser(UserRequest request, UserPrincipal principal) {
        plantAccessService.assertPlantAppUser(principal);
        String email = request.getEmail().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }
        UserRole role = resolveRoleForCreate(request.getRole(), principal);
        Company company = resolveCompanyForNewUser(principal);

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .mobile(request.getMobile())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .status(UserStatus.ACTIVE)
                .company(company)
                .assignedPlants(new HashSet<>(resolvePlants(request.getPlantIds(), principal)))
                .assignedSensorNodes(new HashSet<>(resolveSensorNodes(request.getNodeIds(), request.getPlantIds(), principal)))
                .build();
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request, UserPrincipal principal) {
        User user = findUser(id);
        assertCanManage(principal, user);

        String email = request.getEmail().trim();
        if (!user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }

        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setMobile(request.getMobile());
        user.setRole(resolveRoleForUpdate(user, request.getRole(), principal));
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setAssignedPlants(new HashSet<>(resolvePlants(request.getPlantIds(), principal)));
        user.setAssignedSensorNodes(new HashSet<>(resolveSensorNodes(request.getNodeIds(), request.getPlantIds(), principal)));
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void disableUser(Long id, UserPrincipal principal) {
        User user = findUser(id);
        assertCanManage(principal, user);
        user.setStatus(UserStatus.DISABLED);
        userRepository.save(user);
    }

    @Transactional
    public void enableUser(Long id, UserPrincipal principal) {
        User user = findUser(id);
        assertCanManage(principal, user);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.delete(findUser(id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private Company resolveCompanyForNewUser(UserPrincipal principal) {
        if (principal.getCompanyId() != null) {
            return companyRepository.findById(principal.getCompanyId())
                    .orElseThrow(() -> new BadRequestException("Company not found"));
        }
        List<Long> plantIds = plantAccessService.resolveAccessiblePlantIds(principal);
        if (!plantIds.isEmpty()) {
            return plantRepository.findById(plantIds.get(0))
                    .map(Plant::getCompany)
                    .orElse(null);
        }
        if (plantAccessService.isSuperAdmin(principal)) {
            throw new BadRequestException("Your account is not linked to a company");
        }
        return null;
    }

    private Company requireCompany(UserPrincipal principal) {
        if (principal.getCompanyId() == null) {
            throw new BadRequestException("Your account is not linked to a company");
        }
        return companyRepository.findById(principal.getCompanyId())
                .orElseThrow(() -> new BadRequestException("Company not found"));
    }

    private boolean isManageableByPlantAdmin(User user) {
        return user.getRole() == UserRole.OPERATOR;
    }

    private boolean canPlantAdminAccessUser(UserPrincipal principal, User user) {
        if (user.getAssignedPlants().isEmpty()) {
            return true;
        }
        return user.getAssignedPlants().stream()
                .map(Plant::getPlantId)
                .anyMatch(id -> principal.getPlantIds().contains(id));
    }

    private void assertCanManage(UserPrincipal principal, User user) {
        plantAccessService.assertPlantAppUser(principal);
        if (user.getRole() == UserRole.CONTROL_CENTER_ADMIN) {
            throw new AccessDeniedException("You cannot manage this user");
        }
        if (plantAccessService.isSuperAdmin(principal)) {
            if (user.getRole() == UserRole.SUPER_ADMIN && !user.getId().equals(principal.getId())) {
                throw new AccessDeniedException("You cannot manage another Super Admin");
            }
            if (user.getCompany() == null || principal.getCompanyId() == null
                    || !user.getCompany().getId().equals(principal.getCompanyId())) {
                throw new AccessDeniedException("You cannot manage this user");
            }
            return;
        }
        if (!isManageableByPlantAdmin(user) || !canPlantAdminAccessUser(principal, user)) {
            throw new AccessDeniedException("You cannot manage this user");
        }
    }

    private UserRole resolveRoleForCreate(UserRole requestedRole, UserPrincipal principal) {
        if (plantAccessService.isSuperAdmin(principal)) {
            if (requestedRole == UserRole.PLANT_ADMIN || requestedRole == UserRole.OPERATOR) {
                return requestedRole;
            }
            return UserRole.OPERATOR;
        }
        return UserRole.OPERATOR;
    }

    private UserRole resolveRoleForUpdate(User user, UserRole requestedRole, UserPrincipal principal) {
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            return UserRole.SUPER_ADMIN;
        }
        if (plantAccessService.isSuperAdmin(principal)) {
            if (requestedRole == UserRole.PLANT_ADMIN || requestedRole == UserRole.OPERATOR) {
                return requestedRole;
            }
            return user.getRole();
        }
        return UserRole.OPERATOR;
    }

    private List<Plant> resolvePlants(List<Long> plantIds, UserPrincipal principal) {
        if (plantIds == null || plantIds.isEmpty()) {
            return List.of();
        }
        List<Plant> plants = plantRepository.findAllById(plantIds);
        List<Long> allowed = plantAccessService.resolveAccessiblePlantIds(principal);
        for (Plant plant : plants) {
            if (!allowed.contains(plant.getPlantId())) {
                throw new BadRequestException("Cannot assign plant outside your scope");
            }
        }
        return plants;
    }

    private List<SensorNode> resolveSensorNodes(List<Long> nodeIds, List<Long> plantIds, UserPrincipal principal) {
        if (nodeIds == null || nodeIds.isEmpty()) {
            return List.of();
        }
        List<SensorNode> nodes = sensorNodeRepository.findAllById(nodeIds);
        List<Long> allowed = plantAccessService.resolveAccessiblePlantIds(principal);
        for (SensorNode node : nodes) {
            Long plantId = node.getPlant().getPlantId();
            if (plantIds != null && !plantIds.isEmpty() && !plantIds.contains(plantId)) {
                throw new BadRequestException("Assigned sensors must belong to selected plants");
            }
            if (!allowed.contains(plantId)) {
                throw new BadRequestException("Cannot assign sensor outside your plants");
            }
        }
        return nodes;
    }

    private UserResponse toResponse(User user) {
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
}
