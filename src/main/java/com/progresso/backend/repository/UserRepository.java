package com.progresso.backend.repository;

import com.progresso.backend.enumeration.RoleType;
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
  int countByRole(@Param("role") RoleType role);

  Page<User> findByRole(RoleType role, Pageable pageable);

  Page<User> findByFirstNameContainingOrLastNameContainingOrUsernameContaining(String firstName,
      String lastName, String username, Pageable pageable);

  @Query("SELECT u FROM User u")
  Page<User> findAllUsers(Pageable pageable);
}
