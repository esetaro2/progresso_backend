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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

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
    taskDto.setAssignedUserUsername(
        task.getAssignedUser() != null ? task.getAssignedUser().getUsername() : null);
    return taskDto;
  }

  public Long getProjectIdByTaskId(Long taskId) {
    return taskRepository.findById(taskId)
        .map(task -> task.getProject().getId())
        .orElseThrow(() -> new TaskNotFoundException("Task not found"));
  }

  public TaskDto findById(Long taskId) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));

    return convertToDto(task);
  }

  public Page<TaskDto> findByStatus(Status status, Pageable pageable) {
    Page<Task> tasks = taskRepository.findByStatus(status, pageable);
    if (tasks.isEmpty()) {
      throw new NoDataFoundException("No tasks found with status: " + status);
    }
    return tasks.map(this::convertToDto);
  }

  public Page<TaskDto> findByPriority(String priority, Pageable pageable) {
    if (!EnumUtils.isValidEnum(com.progresso.backend.enumeration.Priority.class, priority)) {
      throw new IllegalArgumentException("Invalid priority: " + priority);
    }
    Page<Task> tasks = taskRepository.findByPriority(
        com.progresso.backend.enumeration.Priority.valueOf(priority), pageable);
    if (tasks.isEmpty()) {
      throw new NoDataFoundException("No tasks found with priority: " + priority);
    }
    return tasks.map(this::convertToDto);
  }

  public Page<TaskDto> findByDueDateBefore(LocalDate dueDate, Pageable pageable) {
    Page<Task> tasks = taskRepository.findByDueDateBefore(dueDate, pageable);
    if (tasks.isEmpty()) {
      throw new NoDataFoundException("No tasks found with due date before: " + dueDate);
    }
    return tasks.map(this::convertToDto);
  }

  public Page<TaskDto> findByCompletionDateAfter(LocalDate completionDate, Pageable pageable) {
    Page<Task> tasks = taskRepository.findByCompletionDateAfter(completionDate, pageable);
    if (tasks.isEmpty()) {
      throw new NoDataFoundException("No tasks found completed after: " + completionDate);
    }
    return tasks.map(this::convertToDto);
  }

  public Page<TaskDto> findByProjectId(Long projectId, Pageable pageable) {
    projectRepository.findById(projectId)
        .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
    Page<Task> tasks = taskRepository.findByProjectId(projectId, pageable);
    if (tasks.isEmpty()) {
      throw new NoDataFoundException("No tasks found for project with ID: " + projectId);
    }
    return tasks.map(this::convertToDto);
  }

  public Page<TaskDto> findByProjectIdAndStatus(Long projectId, Status status, Pageable pageable) {
    projectRepository.findById(projectId)
        .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
    Page<Task> tasks = taskRepository.findByProjectIdAndStatus(projectId, status, pageable);
    if (tasks.isEmpty()) {
      throw new NoDataFoundException(
          "No tasks found for project with ID " + projectId + " and status: " + status);
    }
    return tasks.map(this::convertToDto);
  }

  public Page<TaskDto> findByNameAndProjectId(String name, Long projectId, Pageable pageable) {
    projectRepository.findById(projectId)
        .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
    Page<Task> tasks = taskRepository.findByNameAndProjectId(name, projectId, pageable);
    if (tasks.isEmpty()) {
      throw new NoDataFoundException(
          "No tasks found with name containing: " + name + " for project with ID: " + projectId);
    }
    return tasks.map(this::convertToDto);
  }

  public Page<TaskDto> findCompletedTasksByProjectId(Long projectId, Pageable pageable) {
    projectRepository.findById(projectId)
        .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
    Page<Task> tasks = taskRepository.findCompletedTasksByProjectId(projectId, pageable);
    if (tasks.isEmpty()) {
      throw new NoDataFoundException("No completed tasks found for project with ID: " + projectId);
    }
    return tasks.map(this::convertToDto);
  }

  public Page<TaskDto> findTasksByUserAndStatus(Long userId, Status status, Pageable pageable) {
    userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    Page<Task> tasks = taskRepository.findByAssignedUserIdAndStatus(userId, status, pageable);
    if (tasks.isEmpty()) {
      throw new NoDataFoundException(
          "No tasks found for user with ID: " + userId + " and status: " + status);
    }
    return tasks.map(this::convertToDto);
  }

  public Page<TaskDto> findOverdueTasksByUser(Long userId, Pageable pageable) {
    userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    Page<Task> tasks = taskRepository.findOverdueTasksByUserId(userId, pageable);
    if (tasks.isEmpty()) {
      throw new NoDataFoundException("No overdue tasks found for user with ID: " + userId);
    }
    return tasks.map(this::convertToDto);
  }

  public Page<TaskDto> findTasksByUser(Long userId, Pageable pageable) {
    userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    Page<Task> tasks = taskRepository.findByAssignedUserId(userId, pageable);
    if (tasks.isEmpty()) {
      throw new NoDataFoundException("No tasks found for user with ID: " + userId);
    }
    return tasks.map(this::convertToDto);
  }

  public Page<TaskDto> getTasksByAssignedUserIdAndCompletionDateBefore(Long userId,
      LocalDate completionDate, Pageable pageable) {
    if (completionDate == null) {
      throw new IllegalArgumentException("Completion date cannot be null");
    }

    userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

    Page<Task> tasks = taskRepository.findByAssignedUserIdAndCompletionDateBefore(userId,
        completionDate, pageable);

    if (tasks.isEmpty()) {
      throw new NoDataFoundException(
          "No tasks found for user ID: " + userId + " with completion date before "
              + completionDate);
    }

    return tasks.map(this::convertToDto);
  }

  public Page<TaskDto> getTasksByAssignedUserIdAndStartDateAfter(Long userId, LocalDate startDate,
      Pageable pageable) {
    if (startDate == null) {
      throw new IllegalArgumentException("Start date cannot be null");
    }

    userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

    Page<Task> tasks = taskRepository.findByAssignedUserIdAndStartDateAfter(userId, startDate,
        pageable);

    if (tasks.isEmpty()) {
      throw new IllegalStateException(
          "No tasks found for user ID: " + userId + " starting after " + startDate);
    }

    return tasks.map(this::convertToDto);
  }

  public Page<TaskDto> getTasksByProjectIdAndCompletionDateBefore(Long projectId,
      LocalDate completionDate, Pageable pageable) {
    if (completionDate == null) {
      throw new IllegalArgumentException("Completion date cannot be null");
    }

    projectRepository.findById(projectId)
        .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

    Page<Task> tasks = taskRepository.findByProjectIdAndCompletionDateBefore(projectId,
        completionDate, pageable);

    if (tasks.isEmpty()) {
      throw new IllegalStateException(
          "No tasks found for project ID: " + projectId + " with completion date before "
              + completionDate);
    }

    return tasks.map(this::convertToDto);
  }

  public Page<TaskDto> getTasksByStartDateBetween(LocalDate startDate, LocalDate endDate,
      Pageable pageable) {
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Start date and end date cannot be null");
    }
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date cannot be after end date");
    }

    Page<Task> tasks = taskRepository.findByStartDateBetween(startDate, endDate, pageable);

    if (tasks.isEmpty()) {
      throw new IllegalStateException("No tasks found between " + startDate + " and " + endDate);
    }

    return tasks.map(this::convertToDto);
  }

  @Transactional
  public TaskDto createAndAssignTask(TaskDto taskDto, Long userId) {
    Project project = projectRepository.findById(taskDto.getProjectId())
        .orElseThrow(() -> new ProjectNotFoundException(
            "Project not found with ID: " + taskDto.getProjectId()));

    if (project.getStatus().equals(Status.COMPLETED) || project.getStatus()
        .equals(Status.CANCELLED)) {
      throw new IllegalStateException("Cannot create a task for a completed or cancelled project.");
    }

    if (taskDto.getStartDate().isBefore(LocalDate.now()) || taskDto.getDueDate()
        .isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("Start date and due date must be after the current date.");
    }

    if (taskDto.getStartDate().isAfter(taskDto.getDueDate())) {
      throw new IllegalArgumentException("Start date cannot be after due date.");
    }

    if (taskDto.getStartDate().isBefore(project.getStartDate())) {
      throw new IllegalArgumentException("Start date cannot be before project start.");
    }

    if (taskDto.getDueDate().isAfter(project.getDueDate())) {
      throw new IllegalArgumentException("Due date cannot be after project due.");
    }

    String finalName = taskDto.getName();
    int counter = 1;
    while (taskRepository.existsByProjectIdAndNameAndStatusNot(project.getId(), finalName,
        Status.CANCELLED)) {
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
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

    if (!user.getActive()) {
      throw new UserNotActiveException("User " + user.getUsername() + " is not active.");
    }

    if (!user.getRole().equals(Role.TEAMMEMBER)) {
      throw new InvalidRoleException("This user is not a team member: " + user.getUsername());
    }

    if (!project.getTeam().getTeamMembers().contains(user)) {
      throw new IllegalArgumentException(
          "User " + user.getUsername() + " not found in this team.");
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

    return convertToDto(savedTask);
  }

  @Transactional
  public TaskDto updateTask(Long taskId, TaskDto taskDto) {
    if (taskDto.getStartDate().isBefore(LocalDate.now()) || taskDto.getDueDate()
        .isBefore(LocalDate.now())) {
      throw new IllegalArgumentException(
          "Start date and due date must be after the current date.");
    }

    if (taskDto.getStartDate().isAfter(taskDto.getDueDate())) {
      throw new IllegalArgumentException("Start date cannot be after due date.");
    }

    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));

    Project project = getProject(taskDto, task);

    String baseName = taskDto.getName();
    String finalName = baseName;
    int counter = 1;

    if (!task.getName().equals(baseName)) {
      String finalName1 = finalName;
      while (project.getTasks().stream()
          .anyMatch(t -> !t.getId().equals(task.getId())
              && !t.getStatus().equals(Status.CANCELLED)
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
    task.setStatus(Status.valueOf(taskDto.getStatus()));

    Task updatedTask = taskRepository.save(task);
    return convertToDto(updatedTask);
  }

  private static Project getProject(TaskDto taskDto, Task task) {
    Project project = task.getProject();

    if (taskDto.getStartDate().isBefore(project.getStartDate())) {
      throw new IllegalArgumentException("Task start date cannot be before project start date.");
    }

    if (taskDto.getDueDate().isAfter(project.getDueDate())) {
      throw new IllegalArgumentException("Task due date cannot be after project due date.");
    }

    if (task.getStatus().equals(Status.COMPLETED) || task.getStatus().equals(Status.CANCELLED)) {
      throw new IllegalStateException("Cannot update a completed or cancelled task.");
    }

    if (project.getStatus().equals(Status.COMPLETED) || project.getStatus()
        .equals(Status.CANCELLED)) {
      throw new IllegalStateException(
          "Cannot update a task in a completed or cancelled project.");
    }
    return project;
  }

  @Transactional
  public TaskDto assignTaskToTeamMember(Long taskId, Long userId) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

    if (!user.getActive()) {
      throw new UserNotActiveException("User " + user.getUsername() + " is not active");
    }

    if (!user.getRole().equals(Role.TEAMMEMBER)) {
      throw new InvalidRoleException("This user is not a team member: " + user.getUsername());
    }

    if (!task.getProject().getTeam().getTeamMembers().contains(user)) {
      throw new IllegalArgumentException(
          "User " + user.getUsername() + " not found in this team");
    }

    if (task.getAssignedUser() != null) {
      throw new IllegalStateException(
          "Task is already assigned to user: " + task.getAssignedUser().getUsername());
    }

    if (!task.getProject().getStatus().equals(Status.IN_PROGRESS)) {
      task.getProject().setStatus(Status.IN_PROGRESS);
      projectRepository.save(task.getProject());
    }

    task.setAssignedUser(user);
    task.setStatus(Status.IN_PROGRESS);
    taskRepository.save(task);

    user.getAssignedTasks().add(task);
    userRepository.save(user);

    return convertToDto(task);
  }

  @Transactional
  public TaskDto reassignTaskToTeamMember(Long taskId, Long userId) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));

    User newUser = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

    if (!newUser.getActive()) {
      throw new UserNotActiveException("User " + newUser.getUsername() + " is not active");
    }

    if (!task.getProject().getTeam().getTeamMembers().contains(newUser)) {
      throw new UserNotFoundException("User " + newUser.getUsername() + " not found in this team");
    }

    if (task.getAssignedUser() == null) {
      throw new UserNotFoundException("Task is not assigned to any user");
    }

    if (task.getAssignedUser().equals(newUser)) {
      throw new IllegalArgumentException(
          "Task is already assigned to user: " + newUser.getUsername());
    }

    if (!newUser.getRole().equals(Role.TEAMMEMBER)) {
      throw new InvalidRoleException("This user is not a team member: " + newUser.getUsername());
    }

    User oldUser = task.getAssignedUser();
    oldUser.getAssignedTasks().remove(task);

    task.setAssignedUser(newUser);
    newUser.getAssignedTasks().add(task);

    taskRepository.save(task);
    userRepository.save(oldUser);
    userRepository.save(newUser);

    return convertToDto(task);
  }

  @Transactional
  public TaskDto completeTask(Long taskId) {
    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));

    if (task.getStatus() == Status.COMPLETED) {
      throw new IllegalStateException("Task is already completed");
    }

    if (LocalDate.now().isBefore(task.getStartDate())) {
      throw new IllegalArgumentException("Cannot complete the task before the start date");
    }

    if (task.getProject().getStatus().equals(Status.COMPLETED) || task.getProject().getStatus()
        .equals(Status.CANCELLED)) {
      throw new IllegalStateException(
          "Cannot complete the task in a completed or cancelled project");
    }

    task.setStatus(Status.COMPLETED);
    task.setCompletionDate(LocalDate.now());

    Task completedTask = taskRepository.save(task);

    return convertToDto(completedTask);
  }

  @Transactional
  public void removeTaskFromProject(Long projectId, Long taskId) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(
            () -> new ProjectNotFoundException("Project not found with ID: " + projectId));

    Task task = project.getTasks().stream().filter(t -> t.getId().equals(taskId)).findFirst()
        .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));

    User user = task.getAssignedUser();

    if (user != null) {
      user.getAssignedTasks().remove(task);
      userRepository.save(user);
    }

    project.getTasks().remove(task);
    projectRepository.save(project);

    task.setStatus(Status.CANCELLED);
    taskRepository.save(task);
  }
}
