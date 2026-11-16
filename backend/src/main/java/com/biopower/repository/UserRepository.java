// PURPOSE: Spring Data access for SWARM User persistence.
package com.biopower.repository;

import com.biopower.model.entity.Company;
import com.biopower.model.entity.User;
import com.biopower.model.enums.UserRole;
import com.biopower.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);
    Optional<User> findByIdentityUserId(String identityUserId);
    boolean existsByEmail(String email);
    List<User> findByStatus(UserStatus status);
    List<User> findByCompany(Company company);
    Optional<User> findByCompanyAndRole(Company company, UserRole role);
    List<User> findByRole(UserRole role);
    long countByCompany(Company company);
}
