package com.progresso.backend;

import com.progresso.backend.entity.User;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.usermanagement.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {


  @Profile("!test")
  @Bean
  public CommandLineRunner initAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    return args -> {
      Optional<User> existingAdmin = userRepository.findByUsername("a.superuser.am1@progresso.com");
      if (existingAdmin.isEmpty()) {
        User admin = new User();
        admin.setActive(true);
        admin.setBirthDate(LocalDate.of(1990, 1, 1));
        admin.setUsername("a.superuser.am1@progresso.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@example.com");
        admin.setFirstName("Admin");
        admin.setLastName("Superuser");
        admin.setRole(Role.ADMIN);
        admin.setCity("Admin City");
        admin.setCountry("Admin Country");
        admin.setPhoneNumber("+1 123 456 7890");
        admin.setStateProvinceRegion("Admin State");
        admin.setStreetAddress("Admin Street");
        admin.setZipCode("12345");

        userRepository.save(admin);
      }
    };
  }
}