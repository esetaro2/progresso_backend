package com.progresso.backend.config;

import com.progresso.backend.model.Role;
import com.progresso.backend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

  private final RoleRepository roleRepository;

  public DataInitializer(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @Override
  public void run(String... args) {
    if (roleRepository.count() == 0) {

      Role roleAdmin = new Role();
      roleAdmin.setName(Role.ROLE_ADMIN);
      Role roleProjectManager = new Role();
      roleProjectManager.setName(Role.ROLE_PROJECT_MANAGER);
      Role roleTeamMember = new Role();
      roleTeamMember.setName(Role.ROLE_TEAM_MEMBER);

      roleRepository.save(roleAdmin);
      roleRepository.save(roleProjectManager);
      roleRepository.save(roleTeamMember);

      System.out.println("Default roles created.");
      System.out.println(roleRepository.findAll());
    }
  }
}
