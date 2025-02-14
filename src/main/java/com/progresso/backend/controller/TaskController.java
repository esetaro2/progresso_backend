package com.progresso.backend.controller;

import com.progresso.backend.dto.TaskDto;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.service.TaskService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private final TaskService taskService;

  @Autowired
  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  @GetMapping("/{taskId}")
  public ResponseEntity<TaskDto> getTaskById(@PathVariable Long taskId) {
    TaskDto taskDto = taskService.findById(taskId);
    return ResponseEntity.ok(taskDto);
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<Page<TaskDto>> getTasksByStatus(@PathVariable Status status,
      Pageable pageable) {
    Page<TaskDto> tasks = taskService.findByStatus(status, pageable);
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/priority/{priority}")
  public ResponseEntity<Page<TaskDto>> getTasksByPriority(@PathVariable String priority,
      Pageable pageable) {
    Page<TaskDto> tasks = taskService.findByPriority(priority, pageable);
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/dueDateBefore/{dueDate}")
  public ResponseEntity<Page<TaskDto>> getTasksByDueDateBefore(@PathVariable String dueDate,
      Pageable pageable) {
    Page<TaskDto> tasks = taskService.findByDueDateBefore(LocalDate.parse(dueDate), pageable);
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/completionDateAfter/{completionDate}")
  public ResponseEntity<Page<TaskDto>> getTasksByCompletionDateAfter(
      @PathVariable String completionDate, Pageable pageable) {
    Page<TaskDto> tasks = taskService.findByCompletionDateAfter(LocalDate.parse(completionDate),
        pageable);
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/project/{projectId}")
  public ResponseEntity<Page<TaskDto>> getTasksByProjectId(@PathVariable Long projectId,
      Pageable pageable) {
    Page<TaskDto> tasks = taskService.findByProjectId(projectId, pageable);
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/project/{projectId}/status/{status}")
  public ResponseEntity<Page<TaskDto>> getTasksByProjectIdAndStatus(@PathVariable Long projectId,
      @PathVariable Status status, Pageable pageable) {
    Page<TaskDto> tasks = taskService.findByProjectIdAndStatus(projectId, status, pageable);
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/project/{projectId}/name/{name}")
  public ResponseEntity<Page<TaskDto>> getTasksByNameAndProjectId(@PathVariable String name,
      @PathVariable Long projectId, Pageable pageable) {
    Page<TaskDto> tasks = taskService.findByNameAndProjectId(name, projectId, pageable);
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/project/{projectId}/completed")
  public ResponseEntity<Page<TaskDto>> getCompletedTasksByProjectId(@PathVariable Long projectId,
      Pageable pageable) {
    Page<TaskDto> tasks = taskService.findCompletedTasksByProjectId(projectId, pageable);
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<Page<TaskDto>> getTasksByUser(
      @PathVariable Long userId,
      Pageable pageable) {
    Page<TaskDto> tasks = taskService.findTasksByUser(userId, pageable);
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/user/{userId}/status/{status}")
  public ResponseEntity<Page<TaskDto>> getTasksByUserAndStatus(
      @PathVariable Long userId,
      @PathVariable Status status,
      Pageable pageable) {
    Page<TaskDto> tasks = taskService.findTasksByUserAndStatus(userId, status, pageable);
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/user/{userId}/overdue")
  public ResponseEntity<Page<TaskDto>> getOverdueTasksByUser(
      @PathVariable Long userId,
      Pageable pageable) {
    Page<TaskDto> tasks = taskService.findOverdueTasksByUser(userId, pageable);
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/assigned-user/{userId}/completion-date-before/{completionDate}")
  public ResponseEntity<Page<TaskDto>> getTasksByAssignedUserIdAndCompletionDateBefore(
      @PathVariable Long userId,
      @PathVariable String completionDate,
      Pageable pageable) {
    Page<TaskDto> tasks = taskService.getTasksByAssignedUserIdAndCompletionDateBefore(userId,
        LocalDate.parse(completionDate), pageable);
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/assigned-user/{userId}/start-date-after/{startDate}")
  public ResponseEntity<Page<TaskDto>> getTasksByAssignedUserIdAndStartDateAfter(
      @PathVariable Long userId,
      @PathVariable String startDate,
      Pageable pageable) {
    Page<TaskDto> tasks = taskService.getTasksByAssignedUserIdAndStartDateAfter(userId,
        LocalDate.parse(startDate), pageable);
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/project/{projectId}/completion-date-before/{completionDate}")
  public ResponseEntity<Page<TaskDto>> getTasksByProjectIdAndCompletionDateBefore(
      @PathVariable Long projectId,
      @PathVariable String completionDate,
      Pageable pageable) {
    Page<TaskDto> tasks = taskService.getTasksByProjectIdAndCompletionDateBefore(projectId,
        LocalDate.parse(completionDate), pageable);
    return ResponseEntity.ok(tasks);
  }

  @GetMapping("/start-date-between/{startDate}/{endDate}")
  public ResponseEntity<Page<TaskDto>> getTasksByStartDateBetween(
      @PathVariable String startDate,
      @PathVariable String endDate,
      Pageable pageable) {
    Page<TaskDto> tasks = taskService.getTasksByStartDateBetween(LocalDate.parse(startDate),
        LocalDate.parse(endDate), pageable);
    return ResponseEntity.ok(tasks);
  }

  @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROJECTMANAGER') "
      + "and @projectService.isManagerOfProject(#taskDto.projectId, authentication.name))")
  @PostMapping
  public ResponseEntity<TaskDto> createAndAssignTask(@Valid @RequestBody TaskDto taskDto,
      @RequestParam Long userId) {
    TaskDto createdTask = taskService.createAndAssignTask(taskDto, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
  }

  @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROJECTMANAGER') "
      + "and @projectService.isManagerOfProject(@taskService.getProjectIdByTaskId(#taskId), "
      + "authentication.name))")
  @PostMapping("/{taskId}/assign/{userId}")
  public ResponseEntity<TaskDto> assignTaskToUser(@PathVariable Long taskId,
      @PathVariable Long userId) {
    TaskDto assignedTask = taskService.assignTaskToTeamMember(taskId, userId);
    return ResponseEntity.ok(assignedTask);
  }

  @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROJECTMANAGER') "
      + "and @projectService.isManagerOfProject(@taskService.getProjectIdByTaskId(#taskId), "
      + "authentication.name))")
  @PostMapping("/{taskId}/reassign/{userId}")
  public ResponseEntity<TaskDto> reassignTaskToUser(@PathVariable Long taskId,
      @PathVariable Long userId) {
    TaskDto reassignedTask = taskService.reassignTaskToTeamMember(taskId, userId);
    return ResponseEntity.ok(reassignedTask);
  }

  @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROJECTMANAGER') "
      + "and @projectService.isManagerOfProject(#taskDto.projectId, authentication.name))")
  @PutMapping("/{taskId}")
  public ResponseEntity<TaskDto> updateTask(@PathVariable Long taskId,
      @Valid @RequestBody TaskDto taskDto) {
    TaskDto updatedTask = taskService.updateTask(taskId, taskDto);
    return ResponseEntity.ok(updatedTask);
  }

  @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROJECTMANAGER') "
      + "and @projectService.isManagerOfProject(@taskService.getProjectIdByTaskId(#taskId), "
      + "authentication.name))")
  @PatchMapping("/{taskId}/complete")
  public ResponseEntity<TaskDto> completeTask(@PathVariable Long taskId) {
    TaskDto completedTask = taskService.completeTask(taskId);
    return ResponseEntity.ok(completedTask);
  }

  @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROJECTMANAGER') "
      + "and @projectService.isManagerOfProject(#projectId, authentication.name))")
  @DeleteMapping("/project/{projectId}/task/{taskId}")
  public ResponseEntity<Void> removeTaskFromProject(@PathVariable Long projectId,
      @PathVariable Long taskId) {
    taskService.removeTaskFromProject(projectId, taskId);
    return ResponseEntity.noContent().build();
  }
}
