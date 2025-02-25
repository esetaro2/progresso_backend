package com.progresso.backend.service;

import com.progresso.backend.dto.TaskDto;
import com.progresso.backend.enumeration.Priority;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.NoDataFoundException;
import com.progresso.backend.exception.ProjectNotFoundException;
import com.progresso.backend.exception.TaskNotFoundException;
import com.progresso.backend.exception.UserNotActiveException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.Project;
import com.progresso.backend.model.Task;
import com.progresso.backend.model.User;
import com.progresso.backend.repository.ProjectRepository;
import com.progresso.backend.repository.TaskRepository;
import com.progresso.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

  private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

  private final TaskRepository taskRepository;
  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;

  @Autowired
  public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository,
      UserRepository userRepository) {
    this.taskRepository = taskRepository;
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
  }

  private TaskDto convertToDto(Task task) {
    TaskDto taskDto = new TaskDto();
    taskDto.setId(task.getId());
    taskDto.setName(task.getName());
    taskDto.setDescription(task.getDescription());
    taskDto.setPriority(task.getPriority().toString());
    taskDto.setStartDate(task.getStartDate());
    taskDto.setDueDate(task.getDueDate());
    taskDto.setCompletionDate(task.getCompletionDate());
    taskDto.setStatus(task.getStatus().toString());
    taskDto.setProjectId(task.getProject().getId());
    taskDto.setAssignedUserId(
        task.getAssignedUser() != null ? task.getAssignedUser().getId() : null);
    taskDto.setAssignedUserUsername(
        task.getAssignedUser() != null ? task.getAssignedUser().getUsername() : null);
    return taskDto;
  }

  public Long getProjectIdByTaskId(Long taskId) {
    if (taskId == null) {
      logger.error("getProjectIdByTaskId: Task id cannot be null.");
      throw new IllegalArgumentException("Task id cannot be null.");
    }

    return taskRepository.findById(taskId)
        .map(task -> {
          logger.info("getProjectIdByTaskId: Found project ID {} for task ID {}",
              task.getProject().getId(), taskId);
          return task.getProject().getId();
        })
        .orElseThrow(() -> {
          logger.error("getProjectIdByTaskId: Task not found with ID: {}", taskId);
          return new TaskNotFoundException("Task not found.");
        });
  }

  public TaskDto findById(Long taskId) {
    if (taskId == null) {
      logger.error("findById: Task id cannot be null.");
      throw new IllegalArgumentException("Task id cannot be null.");
    }

    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> {
          logger.error("findById: Task not found with ID: {}", taskId);
          return new TaskNotFoundException("Task not found.");
        });

    logger.info("findById: Task found with ID: {}", taskId);
    return convertToDto(task);
  }

  public Page<TaskDto> findByProjectIdAndStatusAndPriority(Long projectId, String status,
      String priority, Pageable pageable) {
    if (projectId == null) {
      logger.error("findByProjectIdAndStatusAndPriority: Project id cannot be null.");
      throw new IllegalArgumentException("Project id cannot be null.");
    }

    Status statusEnum = (status != null && EnumUtils.isValidEnum(Status.class, status))
        ? Status.valueOf(status)
        : null;

    Priority priorityEnum =
        (priority != null && EnumUtils.isValidEnum(Priority.class, priority)) ? Priority.valueOf(
            priority) : null;

    projectRepository.findById(projectId)
        .orElseThrow(() -> {
          logger.error("findByProjectIdAndStatusAndPriority: Project not found with ID: {}",
              projectId);
          return new IllegalArgumentException("Project not found.");
        });

    Page<Task> tasks = taskRepository.findByProjectIdAndStatusAndPriority(projectId, statusEnum,
        priorityEnum, pageable);

    if (tasks.isEmpty()) {
      logger.warn("findByProjectIdAndStatusAndPriority: No tasks found for project with ID: {}",
          projectId);
      throw new NoDataFoundException("No tasks found for this project.");
    }

    logger.info("findByProjectIdAndStatusAndPriority: Retrieved {} tasks for project with ID: {}",
        tasks.getTotalElements(), projectId);
    return tasks.map(this::convertToDto);
  }

  public Page<TaskDto> findTasksByUser(Long userId, Pageable pageable) {
    if (userId == null) {
      logger.error("findTasksByUser: User id cannot be null.");
      throw new IllegalArgumentException("User id cannot be null.");
    }

    userRepository.findById(userId)
        .orElseThrow(() -> {
          logger.error("findTasksByUser: User not found with ID: {}", userId);
          return new UserNotFoundException("User not found.");
        });

    Page<Task> tasks = taskRepository.findByAssignedUserId(userId, pageable);
    if (tasks.isEmpty()) {
      logger.warn("findTasksByUser: No tasks found for user with ID: {}", userId);
      throw new NoDataFoundException("No tasks found for this user.");
    }

    logger.info("findTasksByUser: Retrieved {} tasks for user with ID: {}",
        tasks.getTotalElements(), userId);
    return tasks.map(this::convertToDto);
  }

  @SuppressWarnings("checkstyle:LineLength")
  @Transactional
  public TaskDto createAndAssignTask(TaskDto taskDto, Long userId) {
    if (userId == null) {
      logger.error("createAndAssignTask: User id cannot be null.");
      throw new IllegalArgumentException("User id cannot be null.");
    }

    Project project = projectRepository.findById(taskDto.getProjectId())
        .orElseThrow(() -> {
          logger.error("createAndAssignTask: Project not found with ID: {}",
              taskDto.getProjectId());
          return new ProjectNotFoundException("Project not found.");
        });

    if (project.getStatus().equals(Status.COMPLETED) || project.getStatus()
        .equals(Status.CANCELLED)) {
      logger.error(
          "createAndAssignTask: Cannot create a task for a completed or cancelled project. Project ID: {}",
          taskDto.getProjectId());
      throw new IllegalStateException("Cannot create a task for a completed or cancelled project.");
    }

    if (project.getTeam() != null && !project.getTeam().getActive()) {
      logger.error(
          "createAndAssignTask: Cannot assign task for members in an inactive team. Project ID: {}, Team ID: {}",
          project.getId(), project.getTeam().getId());
      throw new IllegalStateException("Cannot assign task for members in an inactive team.");
    }

    if (taskDto.getStartDate().isBefore(LocalDate.now())) {
      logger.error("createAndAssignTask: Start date must be today or in the future. StartDate: {}",
          taskDto.getStartDate());
      throw new IllegalArgumentException("Start date must be today or in the future.");
    }

    if (taskDto.getStartDate().isAfter(taskDto.getDueDate())) {
      logger.error(
          "createAndAssignTask: Start date cannot be after due date. StartDate: {}, DueDate: {}",
          taskDto.getStartDate(), taskDto.getDueDate());
      throw new IllegalArgumentException("Start date cannot be after due date.");
    }

    if (taskDto.getStartDate().isBefore(project.getStartDate())) {
      logger.error(
          "createAndAssignTask: Start date cannot be before project start date. ProjectStartDate: {}, TaskStartDate: {}",
          project.getStartDate(), taskDto.getStartDate());
      throw new IllegalArgumentException("Start date cannot be before project start.");
    }

    if (taskDto.getStartDate().isAfter(project.getDueDate())) {
      logger.error(
          "createAndAssignTask: Start date cannot be after project due date. ProjectDueDate: {}, TaskStartDate: {}",
          project.getDueDate(), taskDto.getStartDate());
      throw new IllegalArgumentException("Start date cannot be after project due date.");
    }

    String finalName = taskDto.getName();
    int counter = 1;
    while (taskRepository.existsByProjectIdAndName(project.getId(), finalName)) {
      String suffix = " (" + counter + ")";
      if ((finalName.length() + suffix.length()) > 100) {
        finalName = finalName.substring(0, 100 - suffix.length());
        break;
      }
      finalName = finalName + suffix;
      counter++;
    }

    Task task = new Task();
    task.setName(finalName);
    task.setDescription(taskDto.getDescription());
    task.setPriority(Priority.valueOf(taskDto.getPriority()));
    task.setStartDate(taskDto.getStartDate());
    task.setDueDate(taskDto.getDueDate());
    task.setProject(project);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          logger.error("createAndAssignTask: User not found with ID: {}", userId);
          return new UserNotFoundException("User not found.");
        });

    if (!user.getActive()) {
      logger.error("createAndAssignTask: User {} is not active.", user.getUsername());
      throw new UserNotActiveException("User " + user.getUsername() + " is not active.");
    }

    if (!user.getRole().equals(Role.TEAMMEMBER)) {
      logger.error("createAndAssignTask: This user is not a team member: {}.", user.getUsername());
      throw new InvalidRoleException("This user is not a team member: " + user.getUsername());
    }

    if (project.getTeam() != null && !project.getTeam().getTeamMembers().contains(user)) {
      logger.error("createAndAssignTask: User {} not found in this team.", user.getUsername());
      throw new IllegalArgumentException("User " + user.getUsername() + " not found in this team.");
    }

    task.setAssignedUser(user);
    task.setStatus(Status.IN_PROGRESS);

    Task savedTask = taskRepository.save(task);

    user.getAssignedTasks().add(savedTask);
    userRepository.save(user);

    if (!project.getStatus().equals(Status.IN_PROGRESS)) {
      project.setStatus(Status.IN_PROGRESS);
      projectRepository.save(project);
    }

    logger.info("createAndAssignTask: Created and assigned task {} to user {} for project {}",
        savedTask.getId(), user.getUsername(), project.getId());
    return convertToDto(savedTask);
  }

  @Transactional
  public TaskDto updateTask(Long taskId, TaskDto taskDto) {
    if (taskId == null) {
      logger.error("updateTask: Task id cannot be null.");
      throw new IllegalArgumentException("Task id cannot be null.");
    }

    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> {
          logger.error("updateTask: Task not found with ID: {}", taskId);
          return new TaskNotFoundException("Task not found.");
        });

    if (!taskDto.getStartDate().equals(task.getStartDate())) {
      logger.error("updateTask: Cannot change start date for tasks. Task ID: {}", taskId);
      throw new IllegalArgumentException("Cannot change start date for an ongoing task.");
    }

    if (taskDto.getStartDate().isAfter(taskDto.getDueDate())) {
      logger.error("updateTask: Start date cannot be after due date. StartDate: {}, DueDate: {}",
          taskDto.getStartDate(), taskDto.getDueDate());
      throw new IllegalArgumentException("Start date cannot be after due date.");
    }

    if (taskDto.getCompletionDate() != null && !taskDto.getCompletionDate()
        .equals(task.getCompletionDate())) {
      logger.error("updateTask: Cannot change completion date. Task ID: {}", taskId);
      throw new IllegalArgumentException("Cannot change completion date.");
    }

    if (taskDto.getStatus() != null && !taskDto.getStatus().equals(task.getStatus().name())) {
      logger.error("updateTask: Cannot change task status. Task ID: {}", taskId);
      throw new IllegalArgumentException("Cannot change task status.");
    }

    if (taskDto.getAssignedUserId() != null && !taskDto.getAssignedUserId()
        .equals(task.getAssignedUser().getId())) {
      logger.error("updateTask: Cannot change assigned user. Task ID: {}", taskId);
      throw new IllegalArgumentException("Cannot change assigned user.");
    }

    Project project = getProjectAndValidateDatesAndStatuses(taskDto, task);

    String baseName = taskDto.getName();
    String finalName = baseName;
    int counter = 1;

    if (!task.getName().equals(baseName)) {
      String finalName1 = finalName;
      while (project.getTasks().stream()
          .anyMatch(t -> !t.getId().equals(task.getId())
              && t.getName().equals(finalName1))) {
        finalName = baseName + " (" + counter + ")";
        counter++;
        if ((baseName.length() + (" (" + counter + ")").length()) > 100) {
          finalName =
              baseName.substring(0, 100 - (" (" + counter + ")").length()) + " (" + counter + ")";
          break;
        }
      }
    }

    task.setName(finalName);
    task.setDescription(taskDto.getDescription());
    task.setPriority(Priority.valueOf(taskDto.getPriority()));
    task.setStartDate(taskDto.getStartDate());
    task.setDueDate(taskDto.getDueDate());

    Task updatedTask = taskRepository.save(task);
    logger.info("updateTask: Updated task with ID: {}", taskId);
    return convertToDto(updatedTask);
  }

  @SuppressWarnings("checkstyle:LineLength")
  private static Project getProjectAndValidateDatesAndStatuses(TaskDto taskDto, Task task) {
    Project project = task.getProject();

    if (taskDto.getDueDate().isAfter(project.getDueDate())) {
      logger.error(
          "getProjectAndValidateDatesAndStatuses: Due date cannot be after project due date. TaskDueDate: {}, ProjectDueDate: {}",
          taskDto.getDueDate(), project.getDueDate());
      throw new IllegalArgumentException("Due date cannot be after project due date.");
    }

    if (task.getStatus().equals(Status.COMPLETED)) {
      logger.error(
          "getProjectAndValidateDatesAndStatuses: Cannot update a completed task. Task ID: {}",
          task.getId());
      throw new IllegalStateException("Cannot update a completed task.");
    }

    if (project.getStatus().equals(Status.COMPLETED) || project.getStatus()
        .equals(Status.CANCELLED)) {
      logger.error(
          "getProjectAndValidateDatesAndStatuses: Cannot update a task in a completed or cancelled project. Project ID: {}",
          project.getId());
      throw new IllegalStateException("Cannot update a task in a completed or cancelled project.");
    }
    return project;
  }

  @SuppressWarnings("checkstyle:LineLength")
  @Transactional
  public TaskDto reassignTaskToTeamMember(Long taskId, Long userId) {
    if (taskId == null) {
      logger.error("reassignTaskToTeamMember: Task id cannot be null.");
      throw new IllegalArgumentException("Task id cannot be null.");
    }

    if (userId == null) {
      logger.error("reassignTaskToTeamMember: User id cannot be null.");
      throw new IllegalArgumentException("User id cannot be null.");
    }

    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> {
          logger.error("reassignTaskToTeamMember: Task not found with ID: {}", taskId);
          return new TaskNotFoundException("Task not found.");
        });

    if (task.getStatus().equals(Status.COMPLETED)) {
      logger.error("reassignTaskToTeamMember: Cannot reassign a completed task. Task ID: {}",
          taskId);
      throw new IllegalStateException("Cannot reassign a completed task.");
    }

    if (task.getProject().getStatus().equals(Status.CANCELLED) || task.getProject().getStatus()
        .equals(Status.COMPLETED)) {
      logger.error(
          "reassignTaskToTeamMember: Cannot reassign a task in a completed or cancelled project. Project ID: {}",
          task.getProject().getId());
      throw new IllegalStateException(
          "Cannot reassign a task in a completed or cancelled project.");
    }

    if (task.getProject().getTeam() != null && !task.getProject().getTeam().getActive()) {
      logger.error(
          "reassignTaskToTeamMember: Cannot reassign task because the team is inactive. Task ID: {}, Project ID: {}, Team ID: {}",
          task.getId(), task.getProject().getId(), task.getProject().getTeam().getId());
      throw new IllegalStateException("Cannot reassign task because the team is inactive.");
    }

    User newUser = userRepository.findById(userId)
        .orElseThrow(() -> {
          logger.error("reassignTaskToTeamMember: User not found with ID: {}", userId);
          return new UserNotFoundException("User not found.");
        });

    if (!newUser.getActive()) {
      logger.error("reassignTaskToTeamMember: User {} is not active.", newUser.getUsername());
      throw new UserNotActiveException("User " + newUser.getUsername() + " is not active.");
    }

    if (!task.getProject().getTeam().getTeamMembers().contains(newUser)) {
      logger.error("reassignTaskToTeamMember: User {} not found in this team.",
          newUser.getUsername());
      throw new UserNotFoundException("User " + newUser.getUsername() + " not found in this team.");
    }

    if (!newUser.getRole().equals(Role.TEAMMEMBER)) {
      logger.error("reassignTaskToTeamMember: This user is not a team member: {}.",
          newUser.getUsername());
      throw new InvalidRoleException(
          "This user is not a team member: " + newUser.getUsername());
    }

    User oldUser = task.getAssignedUser();

    if (oldUser != null && task.getStatus().equals(Status.IN_PROGRESS)) {
      oldUser.getAssignedTasks().remove(task);
      userRepository.save(oldUser);
    }

    task.setAssignedUser(newUser);
    newUser.getAssignedTasks().add(task);

    taskRepository.save(task);
    userRepository.save(newUser);

    logger.info("reassignTaskToTeamMember: Reassigned task {} to user {}", taskId,
        newUser.getUsername());
    return convertToDto(task);
  }

  @SuppressWarnings("checkstyle:LineLength")
  @Transactional
  public TaskDto completeTask(Long taskId) {
    if (taskId == null) {
      logger.error("completeTask: Task id cannot be null.");
      throw new IllegalArgumentException("Task id cannot be null.");
    }

    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> {
          logger.error("completeTask: Task not found with ID: {}", taskId);
          return new TaskNotFoundException("Task not found.");
        });

    if (task.getStatus().equals(Status.COMPLETED)) {
      logger.error("completeTask: This task is already completed. Task ID: {}", taskId);
      throw new IllegalStateException("This task is already completed.");
    }

    if (LocalDate.now().isBefore(task.getStartDate())) {
      logger.error("completeTask: Cannot complete the task before the start date. Task ID: {}",
          taskId);
      throw new IllegalArgumentException("Cannot complete the task before the start date");
    }

    if (task.getProject().getStatus().equals(Status.COMPLETED) || task.getProject().getStatus()
        .equals(Status.CANCELLED)) {
      logger.error(
          "completeTask: Cannot complete a task in a completed or cancelled project. Project ID: {}",
          task.getProject().getId());
      throw new IllegalStateException(
          "Cannot complete a task in a completed or cancelled project.");
    }

    task.setStatus(Status.COMPLETED);
    task.setCompletionDate(LocalDate.now());

    Task completedTask = taskRepository.save(task);

    logger.info("completeTask: Task {} has been completed.", taskId);
    return convertToDto(completedTask);
  }

  @SuppressWarnings("checkstyle:LineLength")
  @Transactional
  public void removeTaskFromProject(Long projectId, Long taskId) {
    if (projectId == null) {
      logger.error("removeTaskFromProject: Project id cannot be null.");
      throw new IllegalArgumentException("Project id cannot be null.");
    }

    if (taskId == null) {
      logger.error("removeTaskFromProject: Task id cannot be null.");
      throw new IllegalArgumentException("Task id cannot be null.");
    }

    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> {
          logger.error("removeTaskFromProject: Project not found with ID: {}", projectId);
          return new ProjectNotFoundException("Project not found.");
        });

    if (project.getStatus().equals(Status.CANCELLED) || project.getStatus()
        .equals(Status.COMPLETED)) {
      logger.error(
          "removeTaskFromProject: Cannot remove a task from a completed or cancelled project. Project ID: {}",
          projectId);
      throw new IllegalStateException(
          "Cannot remove a task from a completed or cancelled project.");
    }

    Task task = project.getTasks().stream().filter(t -> t.getId().equals(taskId)).findFirst()
        .orElseThrow(() -> {
          logger.error("removeTaskFromProject: Task not found with ID: {}", taskId);
          return new TaskNotFoundException("Task not found.");
        });

    if (task.getStatus().equals(Status.COMPLETED)) {
      logger.error("removeTaskFromProject: Cannot remove a completed task. Task ID: {}", taskId);
      throw new IllegalStateException("Cannot remove a completed task.");
    }

    User user = task.getAssignedUser();

    if (user != null) {
      user.getAssignedTasks().remove(task);
      userRepository.save(user);
    }

    project.getTasks().remove(task);
    projectRepository.save(project);

    taskRepository.delete(task);

    logger.info("removeTaskFromProject: Task with ID: {} has been removed from project with ID: {}",
        taskId, projectId);
  }
}
