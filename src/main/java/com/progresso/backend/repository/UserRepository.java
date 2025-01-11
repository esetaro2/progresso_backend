package com.progresso.backend.repository;

import com.progresso.backend.enumeration.Role;
import com.progresso.backend.model.User;
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

  Page<User> findByRole(Role role, Pageable pageable);

  Page<User> findByFirstNameContainingOrLastNameContainingOrUsernameContaining(String firstName,
      String lastName, String username, Pageable pageable);

  @Query("SELECT u FROM User u")
  Page<User> findAllUsers(Pageable pageable);

  @Query("SELECT u FROM User u JOIN u.teams t WHERE t.id = :teamId")
  Page<User> findUsersByTeamId(@Param("teamId") Long teamId, Pageable pageable);

  @Query("SELECT u FROM User u JOIN u.teams t WHERE t.id = :teamId AND u.id = :userId")
  Optional<User> findUserInTeam(@Param("teamId") Long teamId, @Param("userId") Long userId);

  @Query("SELECT u FROM User u JOIN u.assignedTasks t WHERE t.project.id = :projectId")
  Page<User> findUsersByProjectId(@Param("projectId") Long projectId, Pageable pageable);

  Page<User> findByActiveTrue(Pageable pageable);
}
