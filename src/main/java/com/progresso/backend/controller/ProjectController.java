package com.progresso.backend.controller;

import com.progresso.backend.dto.ProjectDto;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.service.ProjectService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@Validated
public class ProjectController {

  private final ProjectService projectService;

  @Autowired
  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @GetMapping
  public ResponseEntity<Page<ProjectDto>> getAllProjects(Pageable pageable) {
    Page<ProjectDto> page = projectService.findAllProjects(pageable);
    return ResponseEntity.ok(page);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProjectDto> getProjectById(@PathVariable Long id) {
    ProjectDto project = projectService.findProjectById(id);
    return ResponseEntity.ok(project);
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<Page<ProjectDto>> getProjectsByStatus(@PathVariable String status,
      Pageable pageable) {
    Status projectStatus = Status.valueOf(status);
    Page<ProjectDto> projects = projectService.findProjectsByStatus(projectStatus, pageable);
    return ResponseEntity.ok(projects);
  }

  @GetMapping("/manager/{managerId}")
  public ResponseEntity<Page<ProjectDto>> getProjectsByManager(@PathVariable Long managerId,
      Pageable pageable) {
    Page<ProjectDto> projects = projectService.findProjectsByProjectManager(managerId, pageable);
    return ResponseEntity.ok(projects);
  }

  @GetMapping("/priority/{priority}")
  public ResponseEntity<Page<ProjectDto>> getProjectsByPriority(@PathVariable String priority,
      Pageable pageable) {
    Page<ProjectDto> projects = projectService.findByPriority(priority, pageable);
    return ResponseEntity.ok(projects);
  }

  @GetMapping("/due-before/{date}")
  public ResponseEntity<Page<ProjectDto>> getProjectsDueBefore(@PathVariable String date,
      Pageable pageable) {
    LocalDate dueDate = LocalDate.parse(date);
    Page<ProjectDto> projects = projectService.findProjectsDueBefore(dueDate, pageable);
    return ResponseEntity.ok(projects);
  }

  @GetMapping("/completion-after/{date}")
  public ResponseEntity<Page<ProjectDto>> getProjectsCompletionAfter(@PathVariable String date,
      Pageable pageable) {
    LocalDate dueDate = LocalDate.parse(date);
    Page<ProjectDto> projects = projectService.findByCompletionDateAfter(dueDate, pageable);
    return ResponseEntity.ok(projects);
  }

  @GetMapping("/manager/{managerId}/active")
  public ResponseEntity<Page<ProjectDto>> getActiveProjectsByManager(@PathVariable Long managerId,
      Pageable pageable) {
    Page<ProjectDto> projects = projectService.findActiveByProjectManager(managerId, pageable);
    return ResponseEntity.ok(projects);
  }

  @GetMapping("/task-status/{taskStatus}")
  public ResponseEntity<Page<ProjectDto>> getProjectsByTaskStatus(@PathVariable String taskStatus,
      Pageable pageable) {
    Page<ProjectDto> projects = projectService.findByTaskStatus(taskStatus, pageable);
    return ResponseEntity.ok(projects);
  }

  @GetMapping("/team/{teamId}")
  public ResponseEntity<Page<ProjectDto>> findByTeamId(@PathVariable Long teamId,
      Pageable pageable) {
    Page<ProjectDto> page = projectService.findByTeamId(teamId, pageable);
    return ResponseEntity.ok(page);
  }

  @GetMapping("/team/{teamId}/status/{status}")
  public ResponseEntity<Page<ProjectDto>> findByTeamIdAndStatus(@PathVariable Long teamId,
      @PathVariable String status,
      Pageable pageable) {
    Page<ProjectDto> page = projectService.findByTeamIdAndStatus(teamId, status, pageable);
    return ResponseEntity.ok(page);
  }

  @GetMapping("/team/{teamId}/due-date-before/{dueDate}")
  public ResponseEntity<Page<ProjectDto>> findByTeamIdAndDueDateBefore(@PathVariable Long teamId,
      @PathVariable LocalDate dueDate,
      Pageable pageable) {
    Page<ProjectDto> page = projectService.findByTeamIdAndDueDateBefore(teamId, dueDate, pageable);
    return ResponseEntity.ok(page);
  }

  @GetMapping("/team/{teamId}/active")
  public ResponseEntity<Page<ProjectDto>> findActiveByTeamId(@PathVariable Long teamId,
      Pageable pageable) {
    Page<ProjectDto> page = projectService.findActiveByTeamId(teamId, pageable);
    return ResponseEntity.ok(page);
  }

  @GetMapping("/status/{status}/priority/{priority}")
  public ResponseEntity<Page<ProjectDto>> getProjectsByStatusAndPriority(
      @PathVariable String status,
      @PathVariable String priority,
      Pageable pageable) {

    Page<ProjectDto> projects = projectService.findByStatusAndPriority(status, priority, pageable);

    return ResponseEntity.ok(projects);
  }

  @GetMapping("/start-date-between/{startDate}/{endDate}")
  public ResponseEntity<Page<ProjectDto>> getProjectsByStartDateBetween(
      @PathVariable String startDate,
      @PathVariable String endDate,
      Pageable pageable) {

    LocalDate start = LocalDate.parse(startDate);
    LocalDate end = LocalDate.parse(endDate);

    Page<ProjectDto> projects = projectService.findByStartDateBetween(start, end, pageable);

    return ResponseEntity.ok(projects);
  }

  @GetMapping("/completion-date-before/{completionDate}")
  public ResponseEntity<Page<ProjectDto>> getProjectsByCompletionDateBefore(
      @PathVariable String completionDate,
      Pageable pageable) {

    LocalDate completion = LocalDate.parse(completionDate);

    Page<ProjectDto> projects = projectService.findByCompletionDateBefore(completion, pageable);

    return ResponseEntity.ok(projects);
  }

  @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROJECTMANAGER')")
  @PostMapping
  public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody ProjectDto projectDto) {
    ProjectDto createdProject = projectService.createProject(projectDto);
    return ResponseEntity.ok(createdProject);
  }

  @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROJECTMANAGER') "
      + "and @projectService.isManagerOfProject(#projectId, authentication.name))")
  @PutMapping("/{projectId}")
  public ResponseEntity<ProjectDto> updateProject(
      @PathVariable Long projectId,
      @Valid @RequestBody ProjectDto projectDto) {

    ProjectDto updatedProject = projectService.updateProject(projectId, projectDto);

    return ResponseEntity.ok(updatedProject);
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PutMapping("/{projectId}/remove")
  public ResponseEntity<ProjectDto> removeProject(@PathVariable Long projectId) {

    ProjectDto removedProject = projectService.removeProject(projectId);

    return ResponseEntity.ok(removedProject);
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PutMapping("/{projectId}/update-manager/{projectManagerId}")
  public ResponseEntity<ProjectDto> updateProjectManager(
      @PathVariable Long projectId,
      @PathVariable Long projectManagerId) {

    ProjectDto updatedProject = projectService.updateProjectManager(projectId, projectManagerId);

    return ResponseEntity.ok(updatedProject);
  }

  @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROJECTMANAGER') "
      + "and @projectService.isManagerOfProject(#projectId, authentication.name))")
  @PutMapping("/{projectId}/assign-team/{teamId}")
  public ResponseEntity<ProjectDto> assignTeamToProject(
      @PathVariable Long projectId,
      @PathVariable Long teamId) {

    ProjectDto updatedProject = projectService.assignTeamToProject(projectId, teamId);

    return ResponseEntity.ok(updatedProject);
  }

  @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROJECTMANAGER') "
      + "and @projectService.isManagerOfProject(#projectId, authentication.name))")
  @PutMapping("/{projectId}/reassign-team/{teamId}")
  public ResponseEntity<ProjectDto> reassignTeamToProject(
      @PathVariable Long projectId,
      @PathVariable Long teamId) {

    ProjectDto updatedProject = projectService.reassignTeamToProject(projectId, teamId);

    return ResponseEntity.ok(updatedProject);
  }

  @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROJECTMANAGER') "
      + "and @projectService.isManagerOfProject(#projectId, authentication.name))")
  @PutMapping("/{projectId}/complete")
  public ResponseEntity<ProjectDto> completeProject(@PathVariable Long projectId) {
    ProjectDto updatedProject = projectService.completeProject(projectId);

    return ResponseEntity.ok(updatedProject);
  }
}
