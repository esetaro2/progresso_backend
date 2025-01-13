package com.progresso.backend.service;

import com.progresso.backend.dto.TaskDto;
import com.progresso.backend.enumeration.Priority;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.NoDataFoundException;
import com.progresso.backend.exception.ProjectNotFoundException;
import com.progresso.backend.exception.TaskNameAlreadyExistsException;
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
    taskDto.setAssignedUserId(
        task.getAssignedUser() != null ? task.getAssignedUser().getId() : null);
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
  public TaskDto createTask(TaskDto taskDto) {
    if (taskDto.getStartDate().isBefore(LocalDate.now()) || taskDto.getDueDate()
        .isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("Start date and due date must be after the current date");
    }
    if (taskDto.getStartDate().isAfter(taskDto.getDueDate())) {
      throw new IllegalArgumentException("Start date cannot be after due date");
    }

    Project project = projectRepository.findById(taskDto.getProjectId())
        .orElseThrow(() -> new ProjectNotFoundException(
            "Project not found with ID: " + taskDto.getProjectId()));

    Task task = new Task();
    task.setName(taskDto.getName());
    task.setDescription(taskDto.getDescription());
    task.setPriority(Priority.valueOf(taskDto.getPriority()));
    task.setStartDate(taskDto.getStartDate());
    task.setDueDate(taskDto.getDueDate());
    task.setStatus(Status.NOT_STARTED);
    task.setProject(project);

    if (project.getTasks().stream().map(Task::getName).toList().contains(taskDto.getName())) {
      throw new TaskNameAlreadyExistsException(
          "Task with name " + taskDto.getName() + " already exists in this project");
    }

    Task savedTask = taskRepository.save(task);
    return convertToDto(savedTask);
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
      throw new IllegalArgumentException("User " + user.getUsername() + " not found in this team");
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
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

    if (!user.getActive()) {
      throw new UserNotActiveException("User " + user.getUsername() + " is not active");
    }

    if (!task.getProject().getTeam().getTeamMembers().contains(user)) {
      throw new UserNotFoundException(
          "User " + user.getUsername() + " not found this team");
    }

    if (task.getAssignedUser() == null) {
      throw new UserNotFoundException("Task is not assigned to any user");
    }

    if (task.getAssignedUser().equals(user)) {
      throw new IllegalArgumentException("Task is already assigned to user: " + user.getUsername());
    }

    if (!user.getRole().equals(Role.TEAMMEMBER)) {
      throw new InvalidRoleException("This user is not a team member: " + user.getUsername());
    }

    task.setAssignedUser(user);
    taskRepository.save(task);

    task.getAssignedUser().getAssignedTasks().remove(task);
    user.getAssignedTasks().add(task);

    userRepository.save(task.getAssignedUser());
    userRepository.save(user);

    return convertToDto(task);
  }

  @Transactional
  public TaskDto updateTask(Long taskId, TaskDto taskDto) {
    if (taskDto.getStartDate().isBefore(LocalDate.now()) || taskDto.getDueDate()
        .isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("Start date and due date must be after the current date");
    }

    if (taskDto.getStartDate().isAfter(taskDto.getDueDate())) {
      throw new IllegalArgumentException("Start date cannot be after due date");
    }

    Task task = taskRepository.findById(taskId)
        .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));

    task.setName(taskDto.getName());
    task.setDescription(taskDto.getDescription());
    task.setPriority(Priority.valueOf(taskDto.getPriority()));
    task.setStartDate(taskDto.getStartDate());
    task.setDueDate(taskDto.getDueDate());
    task.setStatus(Status.valueOf(taskDto.getStatus()));

    Task updatedTask = taskRepository.save(task);
    return convertToDto(updatedTask);
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

    task.setStatus(Status.COMPLETED);
    task.setPriority(Priority.LOW);
    task.setCompletionDate(LocalDate.now());

    Task completedTask = taskRepository.save(task);
    return convertToDto(completedTask);
  }

  @Transactional
  public void removeTaskFromProject(Long projectId, Long taskId) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));
    Task task = project.getTasks().stream().filter(t -> t.getId().equals(taskId)).findFirst()
        .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + taskId));
    User user = task.getAssignedUser();

    if (user != null) {
      user.getAssignedTasks().remove(task);
      userRepository.save(user);
    }

    taskRepository.delete(task);

    project.getTasks().remove(task);
    projectRepository.save(project);
  }
}
