package com.progresso.backend.service;

import com.progresso.backend.dto.ProjectDto;
import com.progresso.backend.enumeration.Priority;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.NoDataFoundException;
import com.progresso.backend.exception.ProjectNotFoundException;
import com.progresso.backend.exception.TeamNotFoundException;
import com.progresso.backend.exception.UserNotActiveException;
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
  private final CommentService commentService;

  @Autowired
  public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
      TeamRepository teamRepository, CommentService commentService) {
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
    this.teamRepository = teamRepository;
    this.commentService = commentService;
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

      if (daysRemaining <= 7) {
        priority = Priority.HIGH;
      } else if (daysRemaining <= 30) {
        priority = Priority.MEDIUM;
      }
    }

    return priority;
  }

  public boolean isManagerOfProject(Long projectId, String username) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ProjectNotFoundException("Project not found"));
    User projectManager = project.getProjectManager();

    return projectManager.getUsername().equals(username);
  }

  public long getProjectCompletionPercentage(Long projectId) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

    long totalTasks = project.getTasks().stream()
        .filter(task -> !task.getStatus().equals(Status.CANCELLED)).count();
    int completedTasks = 0;

    for (Task task : project.getTasks()) {
      if (Status.COMPLETED.equals(task.getStatus())) {
        completedTasks++;
      }
    }

    if (totalTasks == 0) {
      return 0;
    }

    return (completedTasks * 100L) / totalTasks;
  }


  public Page<ProjectDto> findAllProjectsWithFilters(String status, String priority, String name,
      Pageable pageable) {

    Status statusEnum = (status != null && EnumUtils.isValidEnum(Status.class, status))
        ? Status.valueOf(status)
        : null;

    Priority priorityEnum =
        (priority != null && EnumUtils.isValidEnum(Priority.class, priority)) ? Priority.valueOf(
            priority) : null;

    Page<Project> projectsPage = projectRepository.findAllWithFilters(statusEnum, priorityEnum,
        name, pageable);

    projectsPage.getContent().stream().filter(
            project -> !project.getStatus().equals(Status.CANCELLED) && !project.getStatus()
                .equals(Status.COMPLETED))
        .forEach(project -> project.setPriority(updateProjectPriority(project)));

    Page<ProjectDto> page = projectsPage.map(this::convertToDto);

    if (page.isEmpty()) {
      throw new NoDataFoundException("No projects found with given filters.");
    }

    return page;
  }

  public Page<ProjectDto> findProjectsByProjectManagerUsernameAndFilters(String managerUsername,
      String status, String priority, String name, Pageable pageable) {
    User projectManager = userRepository.findByUsername(managerUsername)
        .orElseThrow(() -> new UserNotFoundException(
            "Project Manager not found with username: " + managerUsername));

    if (!projectManager.getRole().equals(Role.PROJECTMANAGER)) {
      throw new InvalidRoleException(
          "The user is not a project manager: " + projectManager.getUsername());
    }

    Status statusEnum = (status != null && EnumUtils.isValidEnum(Status.class, status))
        ? Status.valueOf(status)
        : null;

    Priority priorityEnum = (priority != null && EnumUtils.isValidEnum(Priority.class, priority))
        ? Priority.valueOf(priority)
        : null;

    Page<Project> projectsPage = projectRepository.findByProjectManagerUsernameAndFilters(
        managerUsername, statusEnum, priorityEnum, name, pageable);

    projectsPage.getContent().stream().filter(
            project -> !project.getStatus().equals(Status.CANCELLED) && !project.getStatus()
                .equals(Status.COMPLETED))
        .forEach(project -> project.setPriority(updateProjectPriority(project)));

    Page<ProjectDto> page = projectsPage.map(this::convertToDto);

    if (page.isEmpty()) {
      throw new NoDataFoundException(
          "No project found of " + projectManager.getUsername() + " with given filters.");
    }

    return page;
  }

  public Page<ProjectDto> findProjectsByTeamMemberUsernameAndFilters(String teamMemberUsername,
      String status, String priority, String name, Pageable pageable) {

    Status statusEnum = (status != null && EnumUtils.isValidEnum(Status.class, status))
        ? Status.valueOf(status)
        : null;

    Priority priorityEnum = (priority != null && EnumUtils.isValidEnum(Priority.class, priority))
        ? Priority.valueOf(priority)
        : null;

    Page<Project> projectsPage = projectRepository.findByTeamMemberUsernameAndFilters(
        teamMemberUsername, statusEnum, priorityEnum, name, pageable);

    projectsPage.getContent().stream().filter(
            project -> !project.getStatus().equals(Status.CANCELLED) && !project.getStatus()
                .equals(Status.COMPLETED))
        .forEach(project -> project.setPriority(updateProjectPriority(project)));

    Page<ProjectDto> page = projectsPage.map(this::convertToDto);

    if (page.isEmpty()) {
      throw new NoDataFoundException(
          "No project found of " + teamMemberUsername + " with given filters.");
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

  public Page<ProjectDto> findActiveByProjectManager(Long managerId, Pageable pageable) {
    User manager = userRepository.findById(managerId)
        .orElseThrow(
            () -> new UserNotFoundException("User with ID " + managerId + " not found."));

    if (!manager.getRole().equals(Role.PROJECTMANAGER)) {
      throw new IllegalStateException("User with ID " + managerId + " is not a project manager.");
    }

    Page<Project> projectsPage = projectRepository.findActiveByProjectManager(managerId, pageable);

    projectsPage.getContent().stream().filter(
            project -> !project.getStatus().equals(Status.CANCELLED) && !project.getStatus()
                .equals(Status.COMPLETED))
        .forEach(project -> project.setPriority(updateProjectPriority(project)));

    Page<ProjectDto> page = projectsPage.map(this::convertToDto);

    if (page.isEmpty()) {
      throw new NoDataFoundException("No active projects found for the given project manager.");
    }

    return page;
  }

  public Page<ProjectDto> findByTaskStatus(String taskStatus, Pageable pageable) {
    if (!EnumUtils.isValidEnum(Status.class, taskStatus)) {
      throw new IllegalArgumentException("Invalid task status: " + taskStatus);
    }

    Page<Project> projectsPage = projectRepository.findByTaskStatus(Status.valueOf(taskStatus),
        pageable);

    projectsPage.getContent().stream().filter(
            project -> !project.getStatus().equals(Status.CANCELLED) && !project.getStatus()
                .equals(Status.COMPLETED))
        .forEach(project -> project.setPriority(updateProjectPriority(project)));

    Page<ProjectDto> page = projectsPage.map(this::convertToDto);

    if (page.isEmpty()) {
      throw new NoDataFoundException("No projects found for the given task status.");
    }

    return page;
  }

  public Page<ProjectDto> findByTeamId(Long teamId, Pageable pageable) {
    teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));

    Page<Project> projectsPage = projectRepository.findByTeamId(teamId, pageable);

    projectsPage.getContent().stream().filter(
            project -> !project.getStatus().equals(Status.CANCELLED) && !project.getStatus()
                .equals(Status.COMPLETED))
        .forEach(project -> project.setPriority(updateProjectPriority(project)));

    Page<ProjectDto> page = projectsPage.map(this::convertToDto);

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

    Status statusEnum = Status.valueOf(status);
    Page<Project> projectsPage = projectRepository.findByTeamIdAndStatus(teamId, statusEnum,
        pageable);

    projectsPage.getContent().stream().filter(
            project -> !project.getStatus().equals(Status.CANCELLED) && !project.getStatus()
                .equals(Status.COMPLETED))
        .forEach(project -> project.setPriority(updateProjectPriority(project)));

    Page<ProjectDto> page = projectsPage.map(this::convertToDto);

    if (page.isEmpty()) {
      throw new NoDataFoundException("No projects found for the given team and status.");
    }

    return page;
  }

  public Page<ProjectDto> findByTeamIdAndDueDateBefore(Long teamId, LocalDate dueDate,
      Pageable pageable) {
    teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));

    Page<Project> projectsPage = projectRepository.findByTeamIdAndDueDateBefore(teamId, dueDate,
        pageable);

    projectsPage.getContent().stream().filter(
            project -> !project.getStatus().equals(Status.CANCELLED) && !project.getStatus()
                .equals(Status.COMPLETED))
        .forEach(project -> project.setPriority(updateProjectPriority(project)));

    Page<ProjectDto> page = projectsPage.map(this::convertToDto);

    if (page.isEmpty()) {
      throw new NoDataFoundException("No projects found for the given team and due date.");
    }

    return page;
  }

  public Page<ProjectDto> findActiveByTeamId(Long teamId, Pageable pageable) {
    teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));

    Page<Project> projectsPage = projectRepository.findActiveByTeamId(teamId, pageable);

    projectsPage.getContent().stream().filter(
            project -> !project.getStatus().equals(Status.CANCELLED) && !project.getStatus()
                .equals(Status.COMPLETED))
        .forEach(project -> project.setPriority(updateProjectPriority(project)));

    Page<ProjectDto> page = projectsPage.map(this::convertToDto);

    if (page.isEmpty()) {
      throw new NoDataFoundException("No active projects found for the given team.");
    }

    return page;
  }

  @Transactional
  public ProjectDto createProject(ProjectDto projectDto) {
    if (!projectDto.getStartDate().isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("Start date must be after the current date.");
    }

    if (!projectDto.getDueDate().isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("Due date must be after the current date.");
    }

    if (projectDto.getStartDate().isAfter(projectDto.getDueDate())) {
      throw new IllegalArgumentException("Start date cannot be after due date.");
    }

    String baseName;
    baseName = projectDto.getName();
    String finalName;
    finalName = baseName;
    int counter = 1;
    while (projectRepository.existsByNameIgnoreCase(finalName)) {
      String suffix = " (" + counter + ")";
      if ((baseName.length() + suffix.length()) > 100) {
        finalName = baseName.substring(0, 100 - suffix.length());
        break;
      }
      finalName = baseName + suffix;
      counter++;
    }

    User projectManager = userRepository.findById(projectDto.getProjectManagerId()).orElseThrow(
        () -> new UserNotFoundException(
            "User not found with ID: " + projectDto.getProjectManagerId()));

    if (!projectManager.getRole().equals(Role.PROJECTMANAGER)) {
      throw new InvalidRoleException(
          "User is not a project manager: " + projectManager.getUsername());
    }
    if (!projectManager.getActive()) {
      throw new UserNotActiveException("User " + projectManager.getUsername() + " is not active.");
    }

    long activeProjectsCount = projectRepository.countByProjectManagerAndStatusNotIn(
        projectManager, List.of(Status.CANCELLED, Status.COMPLETED));
    if (activeProjectsCount >= 5) {
      throw new IllegalArgumentException(
          "Project Manager cannot manage more than 5 active projects.");
    }

    Project project = new Project();
    project.setName(finalName);
    project.setDescription(projectDto.getDescription());
    project.setProjectManager(projectManager);
    project.setStartDate(projectDto.getStartDate());
    project.setDueDate(projectDto.getDueDate());
    project.setPriority(updateProjectPriority(project));
    project.setStatus(Status.NOT_STARTED);

    Project savedProject = projectRepository.save(project);
    return convertToDto(savedProject);
  }


  @Transactional
  public ProjectDto updateProject(Long projectId, ProjectDto projectDto) {
    if (projectDto.getStartDate().isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("Start date must be after the current date.");
    }
    if (projectDto.getDueDate().isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("Due date must be after the current date.");
    }

    if (!projectDto.getStartDate().isAfter(projectDto.getDueDate())) {
      String baseName = projectDto.getName();
      String finalName = baseName;
      int counter = 1;
      while (projectRepository.existsByNameIgnoreCase(finalName)) {
        String suffix = " (" + counter + ")";
        if ((baseName.length() + suffix.length()) > 100) {
          finalName = baseName.substring(0, 100 - suffix.length());
          break;
        }
        finalName = baseName + suffix;
        counter++;
      }

      Project project = projectRepository.findById(projectId)
          .orElseThrow(
              () -> new ProjectNotFoundException("Project not found with ID: " + projectId));

      if (projectDto.getCompletionDate() != null && !projectDto.getCompletionDate()
          .equals(project.getCompletionDate())) {
        throw new IllegalArgumentException("Cannot change completion date.");
      }

      if (!projectDto.getProjectManagerId().equals(project.getProjectManager().getId())) {
        throw new IllegalArgumentException("Cannot change project manager.");
      }

      if (!projectDto.getTaskIds().equals(project.getTasks().stream().map(Task::getId).toList())) {
        throw new IllegalArgumentException("Cannot change tasks.");
      }

      if (!projectDto.getTeamId().equals(project.getTeam().getId())) {
        throw new IllegalArgumentException("Cannot change team.");
      }

      if (!projectDto.getCommentIds()
          .equals(project.getComments().stream().map(Comment::getId).toList())) {
        throw new IllegalArgumentException("Cannot change comments.");
      }

      project.setName(finalName);
      project.setDescription(projectDto.getDescription());
      project.setPriority(Priority.valueOf(projectDto.getPriority()));
      project.setStartDate(projectDto.getStartDate());
      project.setDueDate(projectDto.getDueDate());
      project.setStatus(Status.valueOf(projectDto.getStatus()));

      Project updatedProject = projectRepository.save(project);
      return convertToDto(updatedProject);
    } else {
      throw new IllegalArgumentException("Start date cannot be after due date.");
    }

  }

  @Transactional
  public ProjectDto updateProjectManager(Long projectId, Long projectManagerId) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

    if (project.getStatus().equals(Status.COMPLETED) || project.getStatus()
        .equals(Status.CANCELLED)) {
      throw new IllegalStateException(
          "Cannot update project manager for a completed or cancelled project.");
    }

    User projectManager = userRepository.findById(projectManagerId).orElseThrow(
        () -> new UserNotFoundException("User not found with ID: " + projectManagerId));

    if (!projectManager.getActive()) {
      throw new UserNotActiveException("User " + projectManager.getUsername() + " is not active.");
    }

    if (!projectManager.getRole().equals(Role.PROJECTMANAGER)) {
      throw new InvalidRoleException(
          "User is not a project manager: " + projectManager.getUsername());
    }

    if (projectRepository.countByProjectManagerAndStatusNotIn(projectManager,
        List.of(Status.CANCELLED, Status.COMPLETED)) >= 5) {
      throw new IllegalArgumentException(
          "Project Manager cannot manage more than 5 active projects.");
    }

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
        .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));

    if (!team.getActive()) {
      throw new IllegalArgumentException("Team is not active");
    }

    if (project.getTeam() != null) {
      throw new IllegalArgumentException("Project is already assigned to a team.");
    }

    if (List.of(Status.CANCELLED, Status.COMPLETED).contains(project.getStatus())) {
      throw new IllegalArgumentException(
          "Cannot assign a team to a cancelled or completed project.");
    }

    if (projectRepository.countByTeamAndStatusNotIn(team,
        List.of(Status.CANCELLED, Status.COMPLETED)) >= 1) {
      throw new IllegalArgumentException("Team has already been assigned to an active project.");
    }

    project.setTeam(team);
    Project updatedProject = projectRepository.save(project);

    return convertToDto(updatedProject);
  }


  @Transactional
  public ProjectDto reassignTeamToProject(Long projectId, Long teamId) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));

    if (List.of(Status.COMPLETED, Status.CANCELLED).contains(project.getStatus())) {
      throw new IllegalArgumentException(
          "Cannot reassign team for a cancelled or completed project.");
    }

    if (!team.getActive()) {
      throw new IllegalArgumentException("Team is not active.");
    }

    if (projectRepository.countByTeamAndStatusNotIn(team,
        List.of(Status.COMPLETED, Status.CANCELLED)) >= 1) {
      throw new IllegalArgumentException(
          "Team has already been assigned to the maximum number of active projects.");
    }

    Team currTeam = project.getTeam();
    if (currTeam == null) {
      throw new TeamNotFoundException("Team is not assigned to a project.");
    }

    if (currTeam.equals(team)) {
      throw new IllegalArgumentException("Team is already assigned to this project.");
    }

    currTeam.getProjects().remove(project);

    project.setTeam(team);
    team.getProjects().add(project);

    projectRepository.save(project);
    teamRepository.save(currTeam);
    teamRepository.save(team);

    return convertToDto(project);
  }


  @Transactional
  public ProjectDto completeProject(Long projectId) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

    if (project.getStatus().equals(Status.CANCELLED)) {
      throw new IllegalStateException("Cannot complete a cancelled project.");
    }

    if (project.getStatus().equals(Status.COMPLETED)) {
      throw new IllegalStateException("Project is already completed.");
    }

    boolean hasIncompleteTasks = project.getTasks().stream()
        .anyMatch(task -> !task.getStatus().equals(Status.CANCELLED) && !task.getStatus()
            .equals(Status.COMPLETED));

    if (hasIncompleteTasks) {
      throw new IllegalStateException("At least one task is not completed.");
    }

    if (project.getTeam() == null) {
      throw new IllegalStateException("No team assigned to the project.");
    }

    if (project.getTeam().getActive()) {
      project.getTeam().setActive(false);
      teamRepository.save(project.getTeam());
    }

    project.setStatus(Status.COMPLETED);
    project.setCompletionDate(LocalDate.now());

    Project updatedProject = projectRepository.save(project);

    return convertToDto(updatedProject);
  }


  @Transactional
  public ProjectDto removeProject(Long projectId) {
    if (projectId == null) {
      throw new IllegalArgumentException("Project ID cannot be null.");
    }

    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

    if (project.getStatus().equals(Status.CANCELLED)) {
      throw new IllegalStateException("Project is already cancelled.");
    }

    project.setStatus(Status.CANCELLED);

    if (project.getTasks() != null && !project.getTasks().isEmpty()) {
      project.getTasks().forEach(task -> task.setStatus(Status.CANCELLED));
    }

    if (!CollectionUtils.isEmpty(project.getComments())) {
      project.getComments().forEach(comment -> commentService.deleteComment(comment.getId()));
    }

    if (project.getTeam() != null && project.getTeam().getActive()) {
      project.getTeam().setActive(false);
      teamRepository.save(project.getTeam());
    }

    Project updatedProject = projectRepository.save(project);

    return convertToDto(updatedProject);
  }
}
