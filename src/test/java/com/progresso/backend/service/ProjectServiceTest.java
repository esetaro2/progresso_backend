package com.progresso.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.progresso.backend.dto.ProjectDto;
import com.progresso.backend.enumeration.Priority;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.UserNotActiveException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.Project;
import com.progresso.backend.model.User;
import com.progresso.backend.repository.ProjectRepository;
import com.progresso.backend.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

  @InjectMocks
  private ProjectService projectService;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private UserRepository userRepository;

  @Test
  void createProject_StartDateInThePast() {
    ProjectDto projectDto = new ProjectDto();
    projectDto.setName("Test Project");
    projectDto.setDescription("Test Description");
    projectDto.setStartDate(LocalDate.now().minusDays(1));
    projectDto.setDueDate(LocalDate.now().plusDays(10));
    projectDto.setProjectManagerId(1L);

    User projectManager = new User();
    projectManager.setId(1L);
    projectManager.setActive(true);
    projectManager.setRole(Role.PROJECTMANAGER);

    assertThrows(IllegalArgumentException.class, () -> projectService.createProject(projectDto));

    verify(projectRepository, never()).save(any(Project.class));
  }

  @Test
  void createProject_StartDateAfterDueDate() {
    ProjectDto projectDto = new ProjectDto();
    projectDto.setName("Test Project");
    projectDto.setDescription("Test Description");
    projectDto.setStartDate(LocalDate.now().plusDays(10));
    projectDto.setDueDate(LocalDate.now().plusDays(5));
    projectDto.setProjectManagerId(1L);

    User projectManager = new User();
    projectManager.setId(1L);
    projectManager.setActive(true);
    projectManager.setRole(Role.PROJECTMANAGER);

    assertThrows(IllegalArgumentException.class, () -> projectService.createProject(projectDto));

    verify(projectRepository, never()).save(any(Project.class));
  }

  @Test
  void createProject_ProjectNameAlreadyExists() {
    ProjectDto projectDto = new ProjectDto();
    projectDto.setName("Test Project");
    projectDto.setDescription("Test Description");
    projectDto.setStartDate(LocalDate.now().plusDays(1));
    projectDto.setDueDate(LocalDate.now().plusDays(10));
    projectDto.setProjectManagerId(1L);

    Project existingProject = new Project();
    existingProject.setName("Test Project");
    when(projectRepository.existsByNameIgnoreCase("Test Project")).thenReturn(true);
    when(projectRepository.existsByNameIgnoreCase("Test Project (1)")).thenReturn(false);

    User projectManager = new User();
    projectManager.setId(1L);
    projectManager.setActive(true);
    projectManager.setRole(Role.PROJECTMANAGER);
    when(userRepository.findById(1L)).thenReturn(Optional.of(projectManager));

    Project savedProject = new Project();
    savedProject.setId(1L);
    savedProject.setName("Test Project (1)");
    savedProject.setDescription(projectDto.getDescription());
    savedProject.setProjectManager(projectManager);
    savedProject.setStartDate(projectDto.getStartDate());
    savedProject.setDueDate(projectDto.getDueDate());
    savedProject.setStatus(Status.NOT_STARTED);
    savedProject.setPriority(Priority.LOW);

    when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

    ProjectDto createdProjectDto = projectService.createProject(projectDto);

    assertEquals("Test Project (1)", createdProjectDto.getName());
  }

  @Test
  void createProject_NameWithSuffixExceeds100Characters() {
    String longProjectName = "Test Project with a very long name that will definitely exceed one hundred characters in total if not properly handled by the system";

    ProjectDto projectDto = new ProjectDto();
    projectDto.setName(longProjectName);
    projectDto.setDescription("Test Description");
    projectDto.setStartDate(LocalDate.now().plusDays(1));
    projectDto.setDueDate(LocalDate.now().plusDays(10));
    projectDto.setProjectManagerId(1L);

    when(projectRepository.existsByNameIgnoreCase(longProjectName)).thenReturn(true);

    String suffixedName = longProjectName.substring(0, 100 - " (1)".length()) + " (1)";
    when(projectRepository.existsByNameIgnoreCase(suffixedName)).thenReturn(false);

    User projectManager = new User();
    projectManager.setId(1L);
    projectManager.setUsername("p.manager.pm1@progresso.com");
    projectManager.setActive(true);
    projectManager.setRole(Role.PROJECTMANAGER);
    when(userRepository.findById(1L)).thenReturn(Optional.of(projectManager));

    when(projectRepository.countByProjectManagerAndStatusNotIn(projectManager,
        List.of(Status.CANCELLED, Status.COMPLETED))).thenReturn(0L);

    Project savedProject = new Project();
    savedProject.setId(1L);
    savedProject.setName(suffixedName);
    savedProject.setDescription(projectDto.getDescription());
    savedProject.setProjectManager(projectManager);
    savedProject.setStartDate(projectDto.getStartDate());
    savedProject.setDueDate(projectDto.getDueDate());
    savedProject.setStatus(Status.NOT_STARTED);
    savedProject.setPriority(Priority.LOW);

    when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

    ProjectDto createdProjectDto = projectService.createProject(projectDto);

    assertEquals(suffixedName, createdProjectDto.getName());

    assertTrue(createdProjectDto.getName().endsWith(" (1)"));

    verify(projectRepository).save(any(Project.class));
  }

  @Test
  void createProject_ManagerNotFound() {
    ProjectDto projectDto = new ProjectDto();
    projectDto.setName("Test Project");
    projectDto.setDescription("Test Description");
    projectDto.setStartDate(LocalDate.now().plusDays(1));
    projectDto.setDueDate(LocalDate.now().plusDays(10));
    projectDto.setProjectManagerId(1L);

    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> projectService.createProject(projectDto));
  }

  @Test
  void createProject_ManagerHasNoProjectManagerRole() {
    ProjectDto projectDto = new ProjectDto();
    projectDto.setName("Test Project");
    projectDto.setDescription("Test Description");
    projectDto.setStartDate(LocalDate.now().plusDays(1));
    projectDto.setDueDate(LocalDate.now().plusDays(10));
    projectDto.setProjectManagerId(1L);

    User projectManager = new User();
    projectManager.setId(1L);
    projectManager.setUsername("t.member.tm1@progresso.com");
    projectManager.setActive(true);
    projectManager.setRole(Role.TEAMMEMBER);
    when(userRepository.findById(1L)).thenReturn(Optional.of(projectManager));

    when(projectRepository.existsByNameIgnoreCase("Test Project")).thenReturn(false);

    assertThrows(InvalidRoleException.class, () -> projectService.createProject(projectDto));
  }

  @Test
  void createProject_ManagerNotActive() {
    ProjectDto projectDto = new ProjectDto();
    projectDto.setName("Test Project");
    projectDto.setDescription("Test Description");
    projectDto.setStartDate(LocalDate.now().plusDays(1));
    projectDto.setDueDate(LocalDate.now().plusDays(10));
    projectDto.setProjectManagerId(1L);

    User projectManager = new User();
    projectManager.setId(1L);
    projectManager.setUsername("p.manager.pm1@progresso.com");
    projectManager.setActive(false);
    projectManager.setRole(Role.PROJECTMANAGER);
    when(userRepository.findById(1L)).thenReturn(Optional.of(projectManager));

    when(projectRepository.existsByNameIgnoreCase("Test Project")).thenReturn(false);

    assertThrows(UserNotActiveException.class, () -> projectService.createProject(projectDto));
  }

  @Test
  void createProject_ManagerHasMoreThanFiveActiveProjects() {
    ProjectDto projectDto = new ProjectDto();
    projectDto.setName("Test Project");
    projectDto.setDescription("Test Description");
    projectDto.setStartDate(LocalDate.now().plusDays(1));
    projectDto.setDueDate(LocalDate.now().plusDays(10));
    projectDto.setProjectManagerId(1L);

    User projectManager = new User();
    projectManager.setId(1L);
    projectManager.setUsername("p.manager.pm1@progresso.com");
    projectManager.setActive(true);
    projectManager.setRole(Role.PROJECTMANAGER);

    when(projectRepository.countByProjectManagerAndStatusNotIn(projectManager,
        List.of(Status.CANCELLED, Status.COMPLETED))).thenReturn(6L);

    when(projectRepository.existsByNameIgnoreCase("Test Project")).thenReturn(false);

    when(userRepository.findById(1L)).thenReturn(Optional.of(projectManager));

    assertThrows(IllegalArgumentException.class, () -> projectService.createProject(projectDto));

    verify(projectRepository, times(1)).countByProjectManagerAndStatusNotIn(projectManager,
        List.of(Status.CANCELLED, Status.COMPLETED));
  }

  @Test
  void createProject_Success() {
    ProjectDto projectDto = new ProjectDto();
    projectDto.setName("Test Project");
    projectDto.setDescription("Test Description");
    projectDto.setStartDate(LocalDate.now().plusDays(1));
    projectDto.setDueDate(LocalDate.now().plusDays(10));
    projectDto.setProjectManagerId(1L);

    when(projectRepository.existsByNameIgnoreCase("Test Project")).thenReturn(false);

    User projectManager = new User();
    projectManager.setId(1L);
    projectManager.setUsername("p.manager.pm1@progresso.com");
    projectManager.setActive(true);
    projectManager.setRole(Role.PROJECTMANAGER);
    when(userRepository.findById(1L)).thenReturn(Optional.of(projectManager));

    when(projectRepository.countByProjectManagerAndStatusNotIn(projectManager,
        List.of(Status.CANCELLED, Status.COMPLETED))).thenReturn(0L);

    Project savedProject = new Project();
    savedProject.setId(1L);
    savedProject.setName("Test Project");
    savedProject.setDescription(projectDto.getDescription());
    savedProject.setProjectManager(projectManager);
    savedProject.setStartDate(projectDto.getStartDate());
    savedProject.setDueDate(projectDto.getDueDate());
    savedProject.setStatus(Status.NOT_STARTED);
    savedProject.setPriority(Priority.LOW);

    when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

    ProjectDto createdProjectDto = projectService.createProject(projectDto);

    assertEquals("Test Project", createdProjectDto.getName());
    assertEquals("Test Description", createdProjectDto.getDescription());
    assertEquals(projectDto.getStartDate(), createdProjectDto.getStartDate());
    assertEquals(projectDto.getDueDate(), createdProjectDto.getDueDate());
    assertEquals(projectManager.getId(), createdProjectDto.getProjectManagerId());

    verify(projectRepository).save(any(Project.class));
  }
}
