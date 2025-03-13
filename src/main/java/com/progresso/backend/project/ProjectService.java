package com.progresso.backend.project;

import com.progresso.backend.dto.ProjectDto;
import com.progresso.backend.entity.Comment;
import com.progresso.backend.entity.Project;
import com.progresso.backend.entity.Task;
import com.progresso.backend.entity.Team;
import com.progresso.backend.entity.User;
import com.progresso.backend.enumeration.Priority;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.NoDataFoundException;
import com.progresso.backend.exception.ProjectNotFoundException;
import com.progresso.backend.exception.TeamNotFoundException;
import com.progresso.backend.exception.UserNotActiveException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.task.TaskRepository;
import com.progresso.backend.task.TaskService;
import com.progresso.backend.team.TeamRepository;
import com.progresso.backend.user.UserRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class ProjectService {

  private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final TeamRepository teamRepository;
  private final TaskRepository taskRepository;
  private final TaskService taskService;

  @Autowired
  public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
      TeamRepository teamRepository, TaskRepository taskRepository, TaskService taskService) {
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
    this.teamRepository = teamRepository;
    this.taskRepository = taskRepository;
    this.taskService = taskService;
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
    dto.setCompletionPercentage(
        !CollectionUtils.isEmpty(project.getTasks()) ? getProjectCompletionPercentage(
            project.getId()) : null);
    dto.setStatus(project.getStatus().toString());
    dto.setProjectManagerId(project.getProjectManager().getId());
    dto.setProjectManagerFirstName(project.getProjectManager().getFirstName());
    dto.setProjectManagerLastName(project.getProjectManager().getLastName());
    dto.setProjectManagerUsername(project.getProjectManager().getUsername());
    dto.setTaskIds(
        !CollectionUtils.isEmpty(project.getTasks()) ? project.getTasks().stream()
            .map(Task::getId).toList() : new ArrayList<>());
    dto.setTeamId(project.getTeam() != null ? project.getTeam().getId() : null);
    dto.setTeamName(project.getTeam() != null ? project.getTeam().getName() : null);
    dto.setCommentIds(
        !CollectionUtils.isEmpty(project.getComments()) ? project.getComments().stream().map(
            Comment::getId).toList() : new ArrayList<>());
    return dto;
  }

  public Priority updateProjectPriority(Project project) {
    if (project.getStatus().equals(Status.COMPLETED) || project.getStatus()
        .equals(Status.CANCELLED)) {
      return project.getPriority();
    }

    LocalDate currentDate = LocalDate.now();
    LocalDate startDate = project.getStartDate();
    LocalDate dueDate = project.getDueDate();

    if (startDate == null || dueDate == null) {
      logger.error("updateProjectPriority: Start date and due date must not be null.");
      throw new IllegalArgumentException("Start date and due date must not be null.");
    }

    Priority priority = Priority.LOW;

    if (currentDate.isAfter(startDate)) {
      long daysRemaining = ChronoUnit.DAYS.between(currentDate, dueDate);

      if (daysRemaining <= 7) {
        priority = Priority.HIGH;
      } else if (daysRemaining <= 30) {
        priority = Priority.MEDIUM;
      }
    }

    logger.info("updateProjectPriority: Project ID: {} with status {} priority updated to {}",
        project.getId(), project.getStatus(),
        priority);
    return priority;
  }

  private Page<ProjectDto> getProjectsDto(Page<Project> projectsPage) {
    projectsPage.getContent()
        .forEach(project -> project.setPriority(updateProjectPriority(project)));

    Page<ProjectDto> page = projectsPage.map(this::convertToDto);

    if (page.isEmpty()) {
      throw new NoDataFoundException("No projects found.");
    }

    return page;
  }

  public boolean isTeamMemberOfProject(Long projectId, String username) {
    if (projectId == null) {
      logger.error("isTeamMemberOfProject: Project id cannot be null.");
      throw new IllegalArgumentException("Project id cannot be null.");
    }

    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> {
          logger.error("isTeamMemberOfProject: Project not found with ID: {}", projectId);
          return new ProjectNotFoundException("Project not found.");
        });

    boolean isMember = project.getTeam() != null && project.getTeam().getTeamMembers().stream()
        .anyMatch(user -> user.getUsername().equals(username));

    logger.info("isTeamMemberOfProject: User {} is {}a member of project with ID: {}", username,
        isMember ? "" : "not ", projectId);
    return isMember;
  }

  public boolean isManagerOfProject(Long projectId, String username) {
    if (projectId == null) {
      logger.error("isManagerOfProject: Project id cannot be null.");
      throw new IllegalArgumentException("Project id cannot be null.");
    }

    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> {
          logger.error("isManagerOfProject: Project not found with ID: {}", projectId);
          return new ProjectNotFoundException("Project not found.");
        });
    User projectManager = project.getProjectManager();

    boolean isManager = projectManager.getUsername().equals(username);

    logger.info("isManagerOfProject: User {} is {}the manager of project with ID: {}", username,
        isManager ? "" : "not ", projectId);
    return isManager;
  }

  public long getProjectCompletionPercentage(Long projectId) {
    if (projectId == null) {
      logger.error("getProjectCompletionPercentage: Project id cannot be null.");
      throw new IllegalArgumentException("Project id cannot be null.");
    }

    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> {
          logger.error("getProjectCompletionPercentage: Project not found with ID: {}", projectId);
          return new ProjectNotFoundException("Project not found.");
        });

    long totalTasks = project.getTasks().stream()
        .filter(task -> !task.getStatus().equals(Status.CANCELLED)).count();
    int completedTasks = 0;

    for (Task task : project.getTasks()) {
      if (Status.COMPLETED.equals(task.getStatus())) {
        completedTasks++;
      }
    }

    if (totalTasks == 0) {
      logger.warn("getProjectCompletionPercentage: No tasks found for project with ID: {}",
          projectId);
      return 0;
    }

    long completionPercentage = (completedTasks * 100L) / totalTasks;
    logger.info("getProjectCompletionPercentage: Project ID: {} is {}% complete.", projectId,
        completionPercentage);
    return completionPercentage;
  }

  @SuppressWarnings("checkstyle:LineLength")
  public Page<ProjectDto> findAllProjectsWithFilters(String status, String priority, String name,
      Pageable pageable) {

    Status statusEnum = (status != null && EnumUtils.isValidEnum(Status.class, status))
        ? Status.valueOf(status)
        : null;

    Priority priorityEnum =
        (priority != null && EnumUtils.isValidEnum(Priority.class, priority)) ? Priority.valueOf(
            priority) : null;

    String processedSearchTerm =
        (name != null && !name.trim().isEmpty()) ? name.trim() : null;

    Page<Project> projectsPage = projectRepository.findAllWithFilters(statusEnum, priorityEnum,
        processedSearchTerm, pageable);

    if (projectsPage.isEmpty()) {
      logger.warn(
          "findAllProjectsWithFilters: No projects found with the given filters. Status: {}, Priority: {}, Name: {}",
          statusEnum, priorityEnum, processedSearchTerm);
      throw new NoDataFoundException("No projects found.");
    } else {
      logger.info(
          "findAllProjectsWithFilters: Retrieved {} projects with the given filters. Status: {}, Priority: {}, Name: {}",
          projectsPage.getTotalElements(), statusEnum, priorityEnum, processedSearchTerm);
    }

    return getProjectsDto(projectsPage);
  }

  @SuppressWarnings("checkstyle:LineLength")
  public Page<ProjectDto> findProjectsByProjectManagerUsernameAndFilters(String managerUsername,
      String status, String priority, String name, Pageable pageable) {
    if (managerUsername == null || managerUsername.isEmpty()) {
      logger.error(
          "findProjectsByProjectManagerUsernameAndFilters: Project Manager's username cannot be null or empty.");
      throw new IllegalArgumentException("Project Manager's username cannot be null or empty.");
    }

    Status statusEnum = (status != null && EnumUtils.isValidEnum(Status.class, status))
        ? Status.valueOf(status)
        : null;

    Priority priorityEnum = (priority != null && EnumUtils.isValidEnum(Priority.class, priority))
        ? Priority.valueOf(priority)
        : null;

    String processedSearchTerm =
        (name != null && !name.trim().isEmpty()) ? name.trim() : null;

    Page<Project> projectsPage = projectRepository.findByProjectManagerUsernameAndFilters(
        managerUsername, statusEnum, priorityEnum, processedSearchTerm, pageable);

    if (projectsPage.isEmpty()) {
      logger.warn(
          "findProjectsByProjectManagerUsernameAndFilters: No projects found with the given filters for project manager: {}. Status: {}, Priority: {}, Name: {}",
          managerUsername, statusEnum, priorityEnum, processedSearchTerm);
      throw new NoDataFoundException(
          "No projects found.");
    } else {
      logger.info(
          "findProjectsByProjectManagerUsernameAndFilters: Retrieved {} projects with the given filters for project manager: {}. Status: {}, Priority: {}, Name: {}",
          projectsPage.getTotalElements(), managerUsername, statusEnum, priorityEnum,
          processedSearchTerm);
    }

    return getProjectsDto(projectsPage);
  }

  @SuppressWarnings("checkstyle:LineLength")
  public Page<ProjectDto> findProjectsByTeamMemberUsernameAndFilters(String teamMemberUsername,
      String status, String priority, String name, Pageable pageable) {

    if (teamMemberUsername == null || teamMemberUsername.isEmpty()) {
      logger.error(
          "findProjectsByTeamMemberUsernameAndFilters: Team member username cannot be null or empty.");
      throw new IllegalArgumentException("Team member username cannot be null or empty.");
    }

    Status statusEnum = (status != null && EnumUtils.isValidEnum(Status.class, status))
        ? Status.valueOf(status)
        : null;

    Priority priorityEnum = (priority != null && EnumUtils.isValidEnum(Priority.class, priority))
        ? Priority.valueOf(priority)
        : null;

    String processedSearchTerm =
        (name != null && !name.trim().isEmpty()) ? name.trim() : null;

    Page<Project> projectsPage = projectRepository.findByTeamMemberUsernameAndFilters(
        teamMemberUsername, statusEnum, priorityEnum, processedSearchTerm, pageable);

    if (projectsPage.isEmpty()) {
      logger.warn(
          "findProjectsByTeamMemberUsernameAndFilters: No projects found with the given filters for team member: {}. Status: {}, Priority: {}, Name: {}",
          teamMemberUsername, statusEnum, priorityEnum, processedSearchTerm);
      throw new NoDataFoundException(
          "No projects found.");
    } else {
      logger.info(
          "findProjectsByTeamMemberUsernameAndFilters: Retrieved {} projects with the given filters for team member: {}. Status: {}, Priority: {}, Name: {}",
          projectsPage.getTotalElements(), teamMemberUsername, statusEnum, priorityEnum,
          processedSearchTerm);
    }

    return getProjectsDto(projectsPage);
  }

  @SuppressWarnings("checkstyle:LineLength")
  public Page<ProjectDto> findActiveProjectsByTeamMemberUsername(String teamMemberUsername,
      Pageable pageable) {

    if (teamMemberUsername == null || teamMemberUsername.isEmpty()) {
      logger.error(
          "findActiveProjectsByTeamMemberUsername: Team member username cannot be null or empty.");
      throw new IllegalArgumentException("Team member username cannot be null or empty.");
    }

    Page<Project> projectsPage = projectRepository.findActiveProjectsByTeamMemberUsername(
        teamMemberUsername, pageable);

    if (projectsPage.isEmpty()) {
      logger.warn(
          "findActiveProjectsByTeamMemberUsername: No active projects found for team member: {}.",
          teamMemberUsername);
      throw new NoDataFoundException(
          "No projects found.");
    } else {
      logger.info(
          "findActiveProjectsByTeamMemberUsername: Retrieved {} active projects for team member: {}.",
          projectsPage.getTotalElements(), teamMemberUsername);
    }

    return getProjectsDto(projectsPage);
  }

  public ProjectDto findProjectById(Long id) {
    if (id == null) {
      logger.error("findProjectById: Id cannot be null.");
      throw new IllegalArgumentException("Id cannot be null.");
    }

    Project project = projectRepository.findById(id)
        .orElseThrow(() -> {
          logger.error("findProjectById: Project not found with ID: {}", id);
          return new ProjectNotFoundException("Project not found.");
        });

    project.setPriority(updateProjectPriority(project));
    projectRepository.save(project);

    logger.info("findProjectById: Retrieved project with ID: {}", id);
    return convertToDto(project);
  }

  @Transactional
  public ProjectDto createProject(ProjectDto projectDto) {
    if (projectDto.getStartDate().isBefore(LocalDate.now())) {
      logger.error("createProject: Start date must be today or in the future. StartDate: {}",
          projectDto.getStartDate());
      throw new IllegalArgumentException("Start date must be today or in the future.");
    }

    if (projectDto.getStartDate().isAfter(projectDto.getDueDate())) {
      logger.error("createProject: Start date cannot be after due date. StartDate: {}, DueDate: {}",
          projectDto.getStartDate(), projectDto.getDueDate());
      throw new IllegalArgumentException("Start date cannot be after due date.");
    }

    String baseName = projectDto.getName();
    String finalName = baseName;
    int counter = 1;
    while (projectRepository.existsByNameIgnoreCase(finalName)) {
      String suffix = " (" + counter + ")";
      if ((baseName.length() + suffix.length()) > 100) {
        finalName = baseName.substring(0, 100 - suffix.length()) + suffix;
      } else {
        finalName = baseName + suffix;
      }
      counter++;
    }

    User projectManager = userRepository.findById(projectDto.getProjectManagerId()).orElseThrow(
        () -> {
          logger.error("createProject: User not found with ID: {}",
              projectDto.getProjectManagerId());
          return new UserNotFoundException("User not found.");
        });

    if (!projectManager.getRole().equals(Role.PROJECTMANAGER)) {
      logger.error("createProject: User {} is not a project manager.",
          projectManager.getUsername());
      throw new InvalidRoleException(
          "User is not a project manager: " + projectManager.getUsername());
    }

    if (!projectManager.getActive()) {
      logger.error("createProject: User {} is not active.", projectManager.getUsername());
      throw new UserNotActiveException("User " + projectManager.getUsername() + " is not active.");
    }

    long activeProjectsCount = projectRepository.countByProjectManagerAndStatusNotIn(
        projectManager, List.of(Status.CANCELLED, Status.COMPLETED));
    if (activeProjectsCount >= 5) {
      logger.error("createProject: Project Manager {} cannot manage more than 5 active projects.",
          projectManager.getUsername());
      throw new IllegalArgumentException(
          "A project Manager cannot manage more than 5 active projects.");
    }

    Project project = new Project();
    project.setName(finalName);
    project.setDescription(projectDto.getDescription());
    project.setProjectManager(projectManager);
    project.setStartDate(projectDto.getStartDate());
    project.setDueDate(projectDto.getDueDate());
    project.setStatus(Status.NOT_STARTED);
    project.setPriority(Priority.LOW);

    Project savedProject = projectRepository.save(project);
    logger.info("createProject: Created project with name: {}", finalName);
    return convertToDto(savedProject);
  }

  @SuppressWarnings("checkstyle:LineLength")
  @Transactional
  public ProjectDto updateProject(Long projectId, ProjectDto projectDto) {
    if (projectId == null) {
      logger.error("updateProject: Project id cannot be null.");
      throw new IllegalArgumentException("Project id cannot be null.");
    }

    if (!projectDto.getStartDate().isAfter(projectDto.getDueDate())) {

      Project project = projectRepository.findById(projectId)
          .orElseThrow(() -> {
            logger.error("updateProject: Project not found with ID: {}", projectId);
            return new ProjectNotFoundException("Project not found.");
          });

      if (project.getStatus().equals(Status.COMPLETED) || project.getStatus()
          .equals(Status.CANCELLED)) {
        logger.error(
            "updateProject: Cannot update a completed or cancelled project. Project ID: {}",
            projectId);
        throw new IllegalStateException("Cannot update a completed or cancelled project.");
      }

      if (project.getStatus().equals(Status.IN_PROGRESS) && !projectDto.getStartDate()
          .equals(project.getStartDate())) {
        logger.error(
            "updateProject: Cannot change start date for an ongoing project. Project ID: {}",
            projectId);
        throw new IllegalArgumentException("Cannot change start date for an ongoing project.");
      }

      if (!projectDto.getStartDate().equals(project.getStartDate()) && projectDto.getStartDate()
          .isBefore(LocalDate.now())) {
        logger.error(
            "updateProject: Start date must be today or in the future. Project ID: {}, StartDate: {}",
            projectId, projectDto.getStartDate());
        throw new IllegalArgumentException("Start date must be today or in the future.");
      }

      String baseName = projectDto.getName();
      String finalName = baseName;
      int counter = 1;
      if (!project.getName().equals(baseName)) {
        while (projectRepository.existsByNameIgnoreCase(finalName)) {
          String suffix = " (" + counter + ")";
          if ((baseName.length() + suffix.length()) > 100) {
            finalName = baseName.substring(0, 100 - suffix.length()) + suffix;
          } else {
            finalName = baseName + suffix;
          }
          counter++;
        }
      }

      if (projectDto.getPriority() != null && !projectDto.getPriority()
          .equals(project.getPriority().name())) {
        logger.error("updateProject: Cannot change project priority. Project ID: {}", projectId);
        throw new IllegalArgumentException("Cannot change project priority.");
      }

      if (projectDto.getCompletionDate() != null && !projectDto.getCompletionDate()
          .equals(project.getCompletionDate())) {
        logger.error("updateProject: Cannot change completion date. Project ID: {}", projectId);
        throw new IllegalArgumentException("Cannot change completion date.");
      }

      if (projectDto.getStatus() != null && !projectDto.getStatus()
          .equals(project.getStatus().name())) {
        logger.error("updateProject: Cannot change project status. Project ID: {}", projectId);
        throw new IllegalArgumentException("Cannot change project status.");
      }

      if (projectDto.getTaskIds() != null && !projectDto.getTaskIds()
          .equals(project.getTasks().stream().map(Task::getId).toList())) {
        logger.error("updateProject: Cannot change tasks. Project ID: {}", projectId);
        throw new IllegalArgumentException("Cannot change tasks.");
      }

      if (projectDto.getTeamId() != null && !projectDto.getTeamId()
          .equals(project.getTeam().getId())) {
        logger.error("updateProject: Cannot change team. Project ID: {}", projectId);
        throw new IllegalArgumentException("Cannot change team.");
      }

      if (projectDto.getCommentIds() != null && !projectDto.getCommentIds()
          .equals(project.getComments().stream().map(Comment::getId).toList())) {
        logger.error("updateProject: Cannot change comments. Project ID: {}", projectId);
        throw new IllegalArgumentException("Cannot change comments.");
      }

      project.setName(finalName);
      project.setDescription(projectDto.getDescription());
      project.setStartDate(projectDto.getStartDate());
      project.setDueDate(projectDto.getDueDate());

      Project updatedProject = projectRepository.save(project);
      logger.info("updateProject: Updated project with ID: {}", projectId);
      return convertToDto(updatedProject);
    } else {
      logger.error(
          "updateProject: Start date cannot be after due date. Project ID: {}, StartDate: {}, DueDate: {}",
          projectId, projectDto.getStartDate(), projectDto.getDueDate());
      throw new IllegalArgumentException("Start date cannot be after due date.");
    }
  }

  @SuppressWarnings("checkstyle:LineLength")
  @Transactional
  public ProjectDto updateProjectManager(Long projectId, Long projectManagerId) {
    if (projectId == null) {
      logger.error("updateProjectManager: Project id cannot be null.");
      throw new IllegalArgumentException("Project id cannot be null.");
    }

    if (projectManagerId == null) {
      logger.error("updateProjectManager: Project manager id cannot be null.");
      throw new IllegalArgumentException("Project manager id cannot be null.");
    }

    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> {
          logger.error("updateProjectManager: Project not found with ID: {}", projectId);
          return new ProjectNotFoundException("Project not found.");
        });

    if (project.getStatus().equals(Status.COMPLETED) || project.getStatus()
        .equals(Status.CANCELLED)) {
      logger.error(
          "updateProjectManager: Cannot update project manager for a completed or cancelled project. Project ID: {}",
          projectId);
      throw new IllegalStateException(
          "Cannot update project manager for a completed or cancelled project.");
    }

    User projectManager = userRepository.findById(projectManagerId).orElseThrow(() -> {
      logger.error("updateProjectManager: User not found with ID: {}", projectManagerId);
      return new UserNotFoundException("User not found.");
    });

    if (!projectManager.getActive()) {
      logger.error("updateProjectManager: User {} is not active.", projectManager.getUsername());
      throw new UserNotActiveException("User " + projectManager.getUsername() + " is not active.");
    }

    if (!projectManager.getRole().equals(Role.PROJECTMANAGER)) {
      logger.error("updateProjectManager: User {} is not a project manager.",
          projectManager.getUsername());
      throw new InvalidRoleException(
          "User is not a project manager: " + projectManager.getUsername());
    }

    if (projectRepository.countByProjectManagerAndStatusNotIn(projectManager,
        List.of(Status.CANCELLED, Status.COMPLETED)) >= 5) {
      logger.error(
          "updateProjectManager: Project manager {} cannot manage more than 5 active projects.",
          projectManager.getUsername());
      throw new IllegalArgumentException(
          "A project manager cannot manage more than 5 active projects.");
    }

    project.setProjectManager(projectManager);
    Project updatedProject = projectRepository.save(project);

    logger.info("updateProjectManager: Updated project manager for project with ID: {}", projectId);
    return convertToDto(updatedProject);
  }

  @SuppressWarnings("checkstyle:LineLength")
  @Transactional
  public ProjectDto assignTeamToProject(Long projectId, Long teamId) {
    if (projectId == null) {
      logger.error("assignTeamToProject: Project id cannot be null.");
      throw new IllegalArgumentException("Project id cannot be null.");
    }

    if (teamId == null) {
      logger.error("assignTeamToProject: Team id cannot be null.");
      throw new IllegalArgumentException("Team id cannot be null.");
    }

    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> {
          logger.error("assignTeamToProject: Project not found with ID: {}", projectId);
          return new ProjectNotFoundException("Project not found.");
        });

    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> {
          logger.error("assignTeamToProject: Team not found with ID: {}", teamId);
          return new TeamNotFoundException("Team not found.");
        });

    if (!team.getActive()) {
      logger.error("assignTeamToProject: Team with ID: {} is not active.", teamId);
      throw new IllegalArgumentException("This team is not active.");
    }

    if (project.getTeam() != null) {
      logger.error("assignTeamToProject: Project with ID: {} is already assigned to a team.",
          projectId);
      throw new IllegalArgumentException("This project is already assigned to a team.");
    }

    if (List.of(Status.CANCELLED, Status.COMPLETED).contains(project.getStatus())) {
      logger.error(
          "assignTeamToProject: Cannot assign a team to a cancelled or completed project. Project ID: {}",
          projectId);
      throw new IllegalArgumentException(
          "Cannot assign a team to a cancelled or completed project.");
    }

    if (projectRepository.countByTeamAndStatusNotIn(team,
        List.of(Status.CANCELLED, Status.COMPLETED)) >= 1) {
      logger.error(
          "assignTeamToProject: Team with ID: {} has already been assigned to an active project.",
          teamId);
      throw new IllegalArgumentException(
          "This team has already been assigned to an active project.");
    }

    project.setTeam(team);
    Project updatedProject = projectRepository.save(project);

    logger.info("assignTeamToProject: Assigned team with ID: {} to project with ID: {}", teamId,
        projectId);
    return convertToDto(updatedProject);
  }

  @SuppressWarnings("checkstyle:LineLength")
  @Transactional
  public ProjectDto reassignTeamToProject(Long projectId, Long teamId) {
    if (projectId == null) {
      logger.error("reassignTeamToProject: Project id cannot be null.");
      throw new IllegalArgumentException("Project id cannot be null.");
    }

    if (teamId == null) {
      logger.error("reassignTeamToProject: Team id cannot be null.");
      throw new IllegalArgumentException("Team id cannot be null.");
    }

    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> {
          logger.error("reassignTeamToProject: Project not found with ID: {}", projectId);
          return new ProjectNotFoundException("Project not found.");
        });

    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> {
          logger.error("reassignTeamToProject: Team not found with ID: {}", teamId);
          return new TeamNotFoundException("Team not found.");
        });

    if (List.of(Status.COMPLETED, Status.CANCELLED).contains(project.getStatus())) {
      logger.error(
          "reassignTeamToProject: Cannot reassign team for a cancelled or completed project. Project ID: {}",
          projectId);
      throw new IllegalArgumentException(
          "Cannot reassign team for a cancelled or completed project.");
    }

    if (!team.getActive()) {
      logger.error("reassignTeamToProject: Team with ID: {} is not active.", teamId);
      throw new IllegalArgumentException("This team is not active.");
    }

    if (projectRepository.countByTeamAndStatusNotIn(team,
        List.of(Status.COMPLETED, Status.CANCELLED)) >= 1) {
      logger.error(
          "reassignTeamToProject: Team with ID: {} has already been assigned to the maximum number of active projects.",
          teamId);
      throw new IllegalArgumentException(
          "This team has already been assigned to the maximum number of active projects.");
    }

    Team currTeam = project.getTeam();
    if (currTeam == null) {
      logger.error("reassignTeamToProject: No team is currently assigned to project with ID: {}",
          projectId);
      throw new TeamNotFoundException("This team is not assigned to a project.");
    }

    currTeam.getTeamMembers().forEach(tm -> {
      List<Task> tasksToUpdate = tm.getAssignedTasks().stream()
          .filter(task -> task.getStatus().equals(Status.IN_PROGRESS))
          .toList();

      tasksToUpdate.forEach(task -> {
        task.setAssignedUser(null);
        taskRepository.save(task);
      });

      userRepository.save(tm);
    });

    currTeam.getProjects().remove(project);

    project.setTeam(team);
    team.getProjects().add(project);

    projectRepository.save(project);
    teamRepository.save(currTeam);
    teamRepository.save(team);

    logger.info("reassignTeamToProject: Reassigned team with ID: {} to project with ID: {}", teamId,
        projectId);
    return convertToDto(project);
  }

  @SuppressWarnings("checkstyle:LineLength")
  @Transactional
  public ProjectDto completeProject(Long projectId) {
    if (projectId == null) {
      logger.error("completeProject: Project id cannot be null.");
      throw new IllegalArgumentException("Project id cannot be null.");
    }

    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> {
          logger.error("completeProject: Project not found with ID: {}", projectId);
          return new ProjectNotFoundException("Project not found.");
        });

    if (List.of(Status.COMPLETED, Status.CANCELLED).contains(project.getStatus())) {
      logger.error(
          "completeProject: Cannot complete a cancelled or completed project. Project ID: {}",
          projectId);
      throw new IllegalArgumentException("Cannot complete a cancelled or completed project.");
    }

    boolean hasIncompleteTasks = project.getTasks().stream()
        .anyMatch(task -> task.getStatus().equals(Status.IN_PROGRESS));

    if (hasIncompleteTasks) {
      logger.error("completeProject: At least one task is not completed. Project ID: {}",
          projectId);
      throw new IllegalStateException(
          "At least one task is not completed. Please complete or delete the incomplete tasks before proceeding.");
    }

    project.setStatus(Status.COMPLETED);
    project.setPriority(Priority.LOW);
    project.setCompletionDate(LocalDate.now());

    Project updatedProject = projectRepository.save(project);

    logger.info("completeProject: Project with ID: {} has been completed successfully.", projectId);
    return convertToDto(updatedProject);
  }

  @Transactional
  public ProjectDto removeProject(Long projectId) {
    if (projectId == null) {
      logger.error("removeProject: Project ID cannot be null.");
      throw new IllegalArgumentException("Project ID cannot be null.");
    }

    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> {
          logger.error("removeProject: Project not found with ID: {}", projectId);
          return new ProjectNotFoundException("Project not found.");
        });

    if (project.getStatus().equals(Status.CANCELLED)) {
      logger.error("removeProject: Cannot remove a cancelled project. Project ID: {}",
          projectId);
      throw new IllegalArgumentException("Cannot remove a cancelled project.");
    }

    if (project.getTasks() != null && !project.getTasks().isEmpty()) {
      project.getTasks()
          .forEach(task -> taskService.removeTaskFromProject(projectId, task.getId(), true));
    }

    project.setStatus(Status.CANCELLED);

    project.setPriority(Priority.LOW);
    Project updatedProject = projectRepository.save(project);

    logger.info("removeProject: Project with ID: {} has been cancelled and removed.", projectId);
    return convertToDto(updatedProject);
  }
}
