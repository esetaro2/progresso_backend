package com.progresso.backend.repository;

import com.progresso.backend.enumeration.RoleType;
import com.progresso.backend.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  @Query("SELECT COUNT(u) FROM User u WHERE  u.role = :role")
  int countByRole(@Param("role")RoleType role);

  List<User> findByFirstNameAndLastName(String firstName, String lastName);

  List<User> findByManagedTeamsId(Long teamId);

  List<User> findByTeamMembershipsTeamId(Long teamId);
}
