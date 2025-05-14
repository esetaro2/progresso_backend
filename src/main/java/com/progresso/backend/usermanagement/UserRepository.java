package com.progresso.backend.usermanagement;

import com.progresso.backend.entity.User;
import com.progresso.backend.enumeration.Role;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  @Query("SELECT COUNT(u) FROM User u WHERE  u.role = :role")
  int countByRole(@Param("role") Role role);

  @Query("SELECT u FROM User u WHERE "
      + "(:role IS NULL OR u.role = :role) AND "
      + "(:active IS NULL OR u.active = :active) AND "
      + "(:searchTerm IS NULL OR ("
      + "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.lastName, ' ', u.firstName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.firstName, ' ', u.username)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.username, ' ', u.firstName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.lastName, ' ', u.username)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.username, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.firstName, ' ', u.lastName, ' ', u.username)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.firstName, ' ', u.username, ' ', u.lastName)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.lastName, ' ', u.firstName, ' ', u.username)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.lastName, ' ', u.username, ' ', u.firstName)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.username, ' ', u.firstName, ' ', u.lastName)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.username, ' ', u.lastName, ' ', u.firstName)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%'))"
      + "))")
  Page<User> findAllWithFilters(
      @Param("role") Role role,
      @Param("active") Boolean active,
      @Param("searchTerm") String searchTerm,
      Pageable pageable);

  @Query("SELECT u FROM User u WHERE "
      + "u.role = 'PROJECTMANAGER' AND "
      + "u.active = true AND "
      + "(:searchTerm IS NULL OR ("
      + "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.lastName, ' ', u.firstName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.firstName, ' ', u.username)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.username, ' ', u.firstName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.lastName, ' ', u.username)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.username, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.firstName, ' ', u.lastName, ' ', u.username)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.firstName, ' ', u.username, ' ', u.lastName)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.lastName, ' ', u.firstName, ' ', u.username)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.lastName, ' ', u.username, ' ', u.firstName)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.username, ' ', u.firstName, ' ', u.lastName)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.username, ' ', u.lastName, ' ', u.firstName)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%'))"
      + ")) "
      + "AND ("
      + "SELECT COUNT(p) FROM Project p "
      + "WHERE p.projectManager = u "
      + "AND p.status NOT IN ('CANCELLED', 'COMPLETED')"
      + ") < 5")
  Page<User> findAvailableProjectManagers(
      @Param("searchTerm") String searchTerm,
      Pageable pageable);

  @Query("SELECT u FROM User u WHERE "
      + "u.role = 'TEAMMEMBER' AND "
      + "u.active = true AND "
      + "(:searchTerm IS NULL OR ("
      + "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.lastName, ' ', u.firstName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.firstName, ' ', u.username)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.username, ' ', u.firstName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.lastName, ' ', u.username)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.username, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.firstName, ' ', u.lastName, ' ', u.username)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.firstName, ' ', u.username, ' ', u.lastName)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.lastName, ' ', u.firstName, ' ', u.username)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.lastName, ' ', u.username, ' ', u.firstName)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.username, ' ', u.firstName, ' ', u.lastName)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.username, ' ', u.lastName, ' ', u.firstName)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%'))"
      + ")) AND NOT EXISTS ("
      + "SELECT 1 FROM Team t WHERE t MEMBER OF u.teams AND t.active = true"
      + ")")
  Page<User> findAvailableTeamMembers(
      @Param("searchTerm") String searchTerm,
      Pageable pageable);

  @Query("SELECT u FROM User u JOIN u.teams t WHERE t.id = :teamId AND u.active = true AND "
      + "(:searchTerm IS NULL OR ("
      + "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.lastName, ' ', u.firstName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.firstName, ' ', u.username)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.username, ' ', u.firstName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.lastName, ' ', u.username)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.username, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.firstName, ' ', u.lastName, ' ', u.username)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.firstName, ' ', u.username, ' ', u.lastName)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.lastName, ' ', u.firstName, ' ', u.username)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.lastName, ' ', u.username, ' ', u.firstName)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.username, ' ', u.firstName, ' ', u.lastName)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR "
      + "LOWER(CONCAT(u.username, ' ', u.lastName, ' ', u.firstName)) "
      + "LIKE LOWER(CONCAT('%', :searchTerm, '%'))"
      + "))")
  Page<User> findUsersByTeamId(
      @Param("teamId") Long teamId,
      @Param("searchTerm") String searchTerm,
      Pageable pageable);
}
