package com.progresso.backend.repository;

import com.progresso.backend.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  @Query("SELECT COUNT(u) FROM User u WHERE  u.role.name = :roleName")
  int countByRole(@Param("roleName") String roleName);
}
