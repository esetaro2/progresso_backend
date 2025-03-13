package com.progresso.backend.task;

import com.progresso.backend.dto.TaskDto;
import jakarta.validation.Valid;
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

  @PreAuthorize("hasAuthority('ADMIN') or "
      + "(hasAuthority('PROJECTMANAGER') "
      + "and @projectService.isManagerOfProject(#projectId, authentication.name)) or "
      + "(hasAuthority('TEAMMEMBER') "
      + "and @projectService.isTeamMemberOfProject(#projectId, authentication.name))")
  @GetMapping("/project/{projectId}")
  public ResponseEntity<Page<TaskDto>> getTasksByProjectIdAndFilters(@PathVariable Long projectId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String priority,
      Pageable pageable) {
    Page<TaskDto> tasks = taskService.findByProjectIdAndStatusAndPriority(projectId, status,
        priority, pageable);
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
  public ResponseEntity<TaskDto> removeTaskFromProject(@PathVariable Long projectId,
      @PathVariable Long taskId) {
    TaskDto taskDto = taskService.removeTaskFromProject(projectId, taskId, false);
    return ResponseEntity.ok(taskDto);
  }
}
