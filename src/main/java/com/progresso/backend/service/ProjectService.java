package com.progresso.backend.service;

import com.progresso.backend.dto.ProjectDto;
import com.progresso.backend.enumeration.Priority;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.NoDataFoundException;
import com.progresso.backend.exception.ProjectNotFoundException;
import com.progresso.backend.exception.TeamNotFoundException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.Comment;
import com.progresso.backend.model.Project;
import com.progresso.backend.model.Task;
import com.progresso.backend.model.Team;
import com.progresso.backend.model.User;
import com.progresso.backend.repository.ProjectRepository;
import com.progresso.backend.repository.TeamRepository;
import com.progresso.backend.repository.UserRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final TeamRepository teamRepository;

  @Autowired
  public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
      TeamRepository teamRepository) {
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
    this.teamRepository = teamRepository;
  }

  public ProjectDto convertToDto(Project project) {
    ProjectDto dto = new ProjectDto();
    dto.setId(project.getId());
    dto.setName(project.getName());
    dto.setDescription(project.getDescription());
    dto.setPriority(project.getPriority().toString());
    dto.setStartDate(project.getStartDate());
    dto.setDueDate(project.getDueDate());
    dto.setCompletionDate(project.getCompletionDate());
    dto.setStatus(project.getStatus().toString());
    dto.setProjectManagerId(project.getProjectManager().getId());
    dto.setTaskIds(
        !CollectionUtils.isEmpty(project.getTasks()) ? project.getTasks().stream()
            .map(Task::getId).toList() : new ArrayList<>());
    dto.setTeamId(project.getTeam() != null ? project.getTeam().getId() : null);
    dto.setCommentIds(
        !CollectionUtils.isEmpty(project.getComments()) ? project.getComments().stream().map(
            Comment::getId).toList() : new ArrayList<>());
    return dto;
  }

  public Priority updateProjectPriority(Project project) {
    LocalDate currentDate = LocalDate.now();
    LocalDate startDate = project.getStartDate();
    LocalDate dueDate = project.getDueDate();

    Priority priority = Priority.LOW;

    if (currentDate.isAfter(startDate) || currentDate.equals(startDate)) {
      long daysRemaining = ChronoUnit.DAYS.between(currentDate, dueDate);

      System.out.println("DaysRemaining: " + daysRemaining);

      if (daysRemaining <= 7) {
        priority = Priority.HIGH;
      } else if (daysRemaining <= 30) {
        priority = Priority.MEDIUM;
      }
    }

    return priority;
  }

  public void updateAllProjectsPriorities() {
    List<Project> projects = projectRepository.findAll();
    projects.forEach(project -> {
      project.setPriority(updateProjectPriority(project));
      projectRepository.save(project);
    });
  }

  public boolean isManagerOfProject(Long projectId, String username) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ProjectNotFoundException("Project not found"));
    User projectManager = project.getProjectManager();

    return projectManager.getUsername().equals(username);
  }

  public Page<ProjectDto> findAllProjects(Pageable pageable) {
    Page<ProjectDto> page = projectRepository.findAllProjects(pageable).map(this::convertToDto);
    if (page.isEmpty()) {
      throw new NoDataFoundException("No data found");
    }

    return page;
  }

  public ProjectDto findProjectById(Long id) {
    Project project = projectRepository.findById(id)
        .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + id));
    project.setPriority(updateProjectPriority(project));
    projectRepository.save(project);
    return convertToDto(project);
  }

  public Page<ProjectDto> findProjectsByStatus(Status status, Pageable pageable) {
    updateAllProjectsPriorities();

    Page<ProjectDto> page = projectRepository.findByStatus(status, pageable)
        .map(this::convertToDto);
    if (page.isEmpty()) {
      throw new NoDataFoundException("No project found with status: " + status);
    }

    return page;
  }

  public Page<ProjectDto> findByStatusAndPriority(String status, String priority,
      Pageable pageable) {
    if (!EnumUtils.isValidEnum(Status.class, status)) {
      throw new IllegalArgumentException("Invalid status: " + status);
    }
    if (!EnumUtils.isValidEnum(Priority.class, priority)) {
      throw new IllegalArgumentException("Invalid priority: " + priority);
    }

    updateAllProjectsPriorities();

    Status statusEnum = Status.valueOf(status);
    Priority priorityEnum = Priority.valueOf(priority);

    Page<Project> projects = projectRepository.findByStatusAndPriority(statusEnum, priorityEnum,
        pageable);

    if (projects.isEmpty()) {
      throw new NoDataFoundException(
          "No projects found for status: " + status + " and priority: " + priority);
    }

    return projects.map(this::convertToDto);
  }

  public Page<ProjectDto> findByStartDateBetween(LocalDate startDate, LocalDate endDate,
      Pageable pageable) {
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Start date and end date cannot be null");
    }
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date cannot be after end date");
    }

    updateAllProjectsPriorities();

    Page<Project> projects = projectRepository.findByStartDateBetween(startDate, endDate,
        pageable);

    if (projects.isEmpty()) {
      throw new NoDataFoundException(
          "No projects found between dates: " + startDate + " and " + endDate);
    }

    return projects.map(this::convertToDto);
  }

  public Page<ProjectDto> findByCompletionDateBefore(LocalDate completionDate, Pageable pageable) {
    if (completionDate == null) {
      throw new IllegalArgumentException("Completion date cannot be null");
    }

    updateAllProjectsPriorities();

    Page<Project> projects = projectRepository.findByCompletionDateBefore(completionDate,
        pageable);

    if (projects.isEmpty()) {
      throw new NoDataFoundException(
          "No projects found before completion date: " + completionDate);
    }

    return projects.map(this::convertToDto);
  }

  public Page<ProjectDto> findProjectsByProjectManager(Long projectManagerId, Pageable pageable) {
    User projectManager = userRepository.findById(projectManagerId)
        .orElseThrow(() -> new UserNotFoundException(
            "Project Manager not found with ID: " + projectManagerId));

    if (!projectManager.getRole().equals(Role.PROJECTMANAGER)) {
      throw new InvalidRoleException(
          "The user is not a project manager: " + projectManager.getUsername());
    }

    updateAllProjectsPriorities();

    Page<ProjectDto> page = projectRepository.findByProjectManagerId(projectManagerId, pageable)
        .map(this::convertToDto);
    if (page.isEmpty()) {
      throw new NoDataFoundException(
          "No project found with project manager: " + projectManager.getUsername());
    }

    return page;
  }

  public Page<ProjectDto> findByPriority(String priority, Pageable pageable) {
    if (!EnumUtils.isValidEnum(Priority.class, priority)) {
      throw new IllegalArgumentException("Invalid priority: " + priority);
    }

    updateAllProjectsPriorities();

    Page<ProjectDto> page = projectRepository.findByPriority(Priority.valueOf(priority), pageable)
        .map(this::convertToDto);
    if (page.isEmpty()) {
      throw new NoDataFoundException(
          "No project found with priority: " + priority);
    }

    return page;
  }

  public Page<ProjectDto> findByCompletionDateAfter(LocalDate completionDate, Pageable pageable) {
    updateAllProjectsPriorities();

    Page<ProjectDto> page = projectRepository.findByCompletionDateAfter(completionDate, pageable)
        .map(this::convertToDto);
    if (page.isEmpty()) {
      throw new NoDataFoundException("No projects found after the given completion date.");
    }

    return page;
  }

  public Page<ProjectDto> findProjectsDueBefore(LocalDate date, Pageable pageable) {
    updateAllProjectsPriorities();

    Page<ProjectDto> page = projectRepository.findByDueDateBefore(date, pageable)
        .map(this::convertToDto);
    if (page.isEmpty()) {
      throw new NoDataFoundException("No projects found due before the given date.");
    }

    return page;
  }

  public Page<ProjectDto> findActiveByProjectManager(Long managerId, Pageable pageable) {
    User manager = userRepository.findById(managerId)
        .orElseThrow(
            () -> new UserNotFoundException("User with ID " + managerId + " not found."));

    if (!manager.getRole().equals(Role.PROJECTMANAGER)) {
      throw new IllegalStateException("User with ID " + managerId + " is not a project manager.");
    }

    updateAllProjectsPriorities();

    Page<ProjectDto> page = projectRepository.findActiveByProjectManager(managerId, pageable)
        .map(this::convertToDto);
    if (page.isEmpty()) {
      throw new NoDataFoundException("No active projects found for the given project manager.");
    }

    return page;
  }

  public Page<ProjectDto> findByTaskStatus(String taskStatus, Pageable pageable) {
    if (!EnumUtils.isValidEnum(Status.class, taskStatus)) {
      throw new IllegalArgumentException("Invalid task status: " + taskStatus);
    }

    updateAllProjectsPriorities();

    Page<ProjectDto> page = projectRepository.findByTaskStatus(Status.valueOf(taskStatus),
            pageable)
        .map(this::convertToDto);
    if (page.isEmpty()) {
      throw new NoDataFoundException("No projects found for the given task status.");
    }

    return page;
  }

  public Page<ProjectDto> findByTeamId(Long teamId, Pageable pageable) {
    teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));

    updateAllProjectsPriorities();

    Page<ProjectDto> page = projectRepository.findByTeamId(teamId, pageable)
        .map(this::convertToDto);
    if (page.isEmpty()) {
      throw new NoDataFoundException("No projects found for the given team.");
    }

    return page;
  }

  public Page<ProjectDto> findByTeamIdAndStatus(Long teamId, String status, Pageable pageable) {
    teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));

    if (!EnumUtils.isValidEnum(Status.class, status)) {
      throw new IllegalArgumentException("Invalid status: " + status);
    }

    updateAllProjectsPriorities();

    Status statusEnum = Status.valueOf(status);
    Page<ProjectDto> page = projectRepository.findByTeamIdAndStatus(teamId, statusEnum, pageable)
        .map(this::convertToDto);
    if (page.isEmpty()) {
      throw new NoDataFoundException("No projects found for the given team and status.");
    }

    return page;
  }

  public Page<ProjectDto> findByTeamIdAndDueDateBefore(Long teamId, LocalDate dueDate,
      Pageable pageable) {
    teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));

    updateAllProjectsPriorities();

    Page<ProjectDto> page = projectRepository.findByTeamIdAndDueDateBefore(teamId, dueDate,
        pageable).map(this::convertToDto);
    if (page.isEmpty()) {
      throw new NoDataFoundException("No projects found for the given team and due date.");
    }

    return page;
  }

  public Page<ProjectDto> findActiveByTeamId(Long teamId, Pageable pageable) {
    teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));

    updateAllProjectsPriorities();

    Page<ProjectDto> page = projectRepository.findActiveByTeamId(teamId, pageable)
        .map(this::convertToDto);
    if (page.isEmpty()) {
      throw new NoDataFoundException("No active projects found for the given team.");
    }

    return page;
  }

  @Transactional
  public ProjectDto createProject(ProjectDto projectDto) {
    if (projectDto.getStartDate().isBefore(LocalDate.now()) || projectDto.getDueDate()
        .isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("Start date and due date must be after the current date.");
    }

    if (projectDto.getStartDate().isAfter(projectDto.getDueDate())) {
      throw new IllegalArgumentException("Start date cannot be after due date.");
    }

    User projectManager = userRepository.findById(projectDto.getProjectManagerId())
        .orElseThrow(() -> new IllegalArgumentException(
            "Project Manager not found with ID: " + projectDto.getProjectManagerId()));

    if (!projectManager.getRole().equals(Role.PROJECTMANAGER)) {
      throw new IllegalStateException(
          "User with ID " + projectDto.getProjectManagerId() + " is not a project manager.");
    }

    if (projectRepository.countByProjectManagerAndStatusNotIn(projectManager,
        List.of(Status.CANCELLED, Status.COMPLETED)) >= 5) {
      throw new IllegalArgumentException(
          "Project Manager cannot manage more than 5 active projects.");
    }

    if (projectRepository.existsByName(projectDto.getName())) {
      throw new IllegalArgumentException("Project name already exists.");
    }

    Project project = new Project();
    project.setName(projectDto.getName());
    project.setDescription(projectDto.getDescription());
    project.setStartDate(projectDto.getStartDate());
    project.setDueDate(projectDto.getDueDate());
    project.setPriority(updateProjectPriority(project));
    project.setStatus(Status.NOT_STARTED);
    project.setProjectManager(projectManager);

    Project savedProject = projectRepository.save(project);
    return convertToDto(savedProject);
  }

  @Transactional
  public ProjectDto updateProject(Long projectId, ProjectDto projectDto) {
    if (projectDto.getStartDate().isBefore(LocalDate.now()) || projectDto.getDueDate()
        .isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("Start date and due date must be after the current date.");
    }
    if (projectDto.getStartDate().isAfter(projectDto.getDueDate())) {
      throw new IllegalArgumentException("Start date cannot be after due date.");
    }

    Project project = projectRepository.findById(projectId)
        .orElseThrow(
            () -> new ProjectNotFoundException("Project not found with ID: " + projectId));

    if (!projectDto.getCompletionDate().equals(project.getCompletionDate())) {
      throw new IllegalArgumentException("Cannot change completion date here.");
    }

    if (!projectDto.getProjectManagerId().equals(project.getProjectManager().getId())) {
      throw new IllegalArgumentException("Cannot change project manager here.");
    }

    if (!projectDto.getTaskIds().equals(project.getTasks().stream().map(Task::getId).toList())) {
      throw new IllegalArgumentException("Cannot change tasks here.");
    }

    if (!projectDto.getTeamId().equals(project.getTeam().getId())) {
      throw new IllegalArgumentException("Cannot change team here.");
    }

    if (!projectDto.getCommentIds()
        .equals(project.getComments().stream().map(Comment::getId).toList())) {
      throw new IllegalArgumentException("Cannot change comments here.");
    }

    project.setName(projectDto.getName());
    project.setDescription(projectDto.getDescription());
    project.setPriority(Priority.valueOf(projectDto.getPriority()));
    project.setStartDate(projectDto.getStartDate());
    project.setDueDate(projectDto.getDueDate());
    project.setStatus(Status.valueOf(projectDto.getStatus()));

    Project updatedProject = projectRepository.save(project);
    return convertToDto(updatedProject);
  }

  @Transactional
  public ProjectDto completeProject(Long projectId) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(
            () -> new ProjectNotFoundException("Project not found with ID: " + projectId));

    if (project.getStatus().equals(Status.COMPLETED)) {
      throw new IllegalStateException("Project is already completed.");
    }

    if (project.getTasks().stream()
        .anyMatch(task -> !task.getStatus().equals(Status.COMPLETED))) {
      throw new IllegalStateException("At least one task is not completed.");
    }

    project.setStatus(Status.COMPLETED);
    project.setCompletionDate(LocalDate.now());

    Project updatedProject = projectRepository.save(project);

    return convertToDto(updatedProject);
  }

  @Transactional
  public ProjectDto removeProject(Long projectId) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(
            () -> new ProjectNotFoundException("Project not found with ID: " + projectId));

    project.setStatus(Status.CANCELLED);

    if (!CollectionUtils.isEmpty(project.getTasks())) {
      project.getTasks().forEach(task -> task.setStatus(Status.CANCELLED));
    }

    return convertToDto(project);
  }

  @Transactional
  public ProjectDto updateProjectManager(Long projectId, Long projectManagerId) {
    User projectManager = userRepository.findById(projectManagerId).orElseThrow(
        () -> new UserNotFoundException("User not found with ID: " + projectManagerId));

    if (!projectManager.getRole().equals(Role.PROJECTMANAGER)) {
      throw new InvalidRoleException(
          "User is not a project manager: " + projectManager.getUsername());
    }

    if (projectRepository.countByProjectManagerAndStatusNotIn(projectManager,
        List.of(Status.CANCELLED, Status.COMPLETED)) >= 5) {
      throw new IllegalArgumentException(
          "Project Manager cannot manage more than 5 active projects.");
    }

    Project project = projectRepository.findById(projectId)
        .orElseThrow(
            () -> new ProjectNotFoundException("Project not found with ID: " + projectId));

    if (project.getProjectManager().equals(projectManager)) {
      throw new IllegalArgumentException("User is already the current project manager");
    }

    project.setProjectManager(projectManager);
    Project updatedProject = projectRepository.save(project);

    return convertToDto(updatedProject);
  }

  @Transactional
  public ProjectDto assignTeamToProject(Long projectId, Long teamId) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(
            () -> new ProjectNotFoundException("Project not found with ID: " + projectId));
    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));

    if (!team.getActive()) {
      throw new IllegalArgumentException("Team is not active");
    }

    if (project.getTeam() != null) {
      throw new IllegalArgumentException("Project is already assigned to a team.");
    }

    if (projectRepository.countByTeamAndStatusNotIn(team,
        List.of(Status.COMPLETED, Status.CANCELLED)) >= 1) {
      throw new IllegalArgumentException("Team has already been assigned to an active project.");
    }

    if (List.of(Status.CANCELLED, Status.COMPLETED).contains(project.getStatus())) {
      throw new IllegalArgumentException(
          "Cannot assign a team to a cancelled or completed project.");
    }

    project.setTeam(team);
    Project updatedProject = projectRepository.save(project);

    return convertToDto(updatedProject);
  }

  @Transactional
  public ProjectDto reassignTeamToProject(Long projectId, Long teamId) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(
            () -> new ProjectNotFoundException("Project not found with ID: " + projectId));
    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));
    Team currTeam = project.getTeam();

    if (currTeam == null) {
      throw new TeamNotFoundException("Team is not assigned to a project.");
    }

    if (currTeam.equals(team)) {
      throw new IllegalArgumentException("Team is already assigned to this project.");
    }

    project.setTeam(team);
    projectRepository.save(project);

    currTeam.getProjects().remove(project);
    team.getProjects().add(project);

    teamRepository.save(currTeam);
    teamRepository.save(team);

    return convertToDto(project);
  }
}
