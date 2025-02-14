package com.progresso.backend.controller;

import com.progresso.backend.dto.ProjectDto;
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
import org.springframework.web.bind.annotation.RequestParam;
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

  @GetMapping("/{projectId}/completion")
  public ResponseEntity<Long> getProjectCompletionPercentage(@PathVariable Long projectId) {
    long percentage = projectService.getProjectCompletionPercentage(projectId);
    return ResponseEntity.ok(percentage);
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping
  public ResponseEntity<Page<ProjectDto>> getAllProjectsByFilters(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String priority,
      @RequestParam(required = false) String name,
      Pageable pageable
  ) {
    Page<ProjectDto> projects = projectService.findAllProjectsWithFilters(status, priority, name,
        pageable);

    return ResponseEntity.ok(projects);
  }

  @PreAuthorize("hasAuthority('ADMIN') OR "
      + "(hasAuthority('PROJECTMANAGER') and #managerUsername == authentication.name)")
  @GetMapping("/manager/{managerUsername}")
  public ResponseEntity<Page<ProjectDto>> getProjectsByManagerAndFilters(
      @PathVariable String managerUsername,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String priority,
      @RequestParam(required = false) String name,
      Pageable pageable) {
    Page<ProjectDto> projects = projectService.findProjectsByProjectManagerUsernameAndFilters(
        managerUsername,
        status, priority, name, pageable);
    return ResponseEntity.ok(projects);
  }

  @PreAuthorize("hasAuthority('ADMIN') OR "
      + "(hasAuthority('TEAMMEMBER') and #teamMemberUsername == authentication.name)")
  @GetMapping("/teamMember/{teamMemberUsername}")
  public ResponseEntity<Page<ProjectDto>> getProjectsByTeamMemberUsernameAndFilters(
      @PathVariable String teamMemberUsername,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String priority,
      @RequestParam(required = false) String name,
      Pageable pageable) {
    Page<ProjectDto> projects = projectService.findProjectsByTeamMemberUsernameAndFilters(
        teamMemberUsername, status, priority, name, pageable);
    return ResponseEntity.ok(projects);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProjectDto> getProjectById(@PathVariable Long id) {
    ProjectDto project = projectService.findProjectById(id);
    return ResponseEntity.ok(project);
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

  @PreAuthorize("hasAuthority('ADMIN')")
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

  @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('PROJECTMANAGER') "
      + "and @projectService.isManagerOfProject(#projectId, authentication.name))")
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

  @PreAuthorize("hasAuthority('ADMIN')")
  @PutMapping("/{projectId}/assign-team/{teamId}")
  public ResponseEntity<ProjectDto> assignTeamToProject(
      @PathVariable Long projectId,
      @PathVariable Long teamId) {

    ProjectDto updatedProject = projectService.assignTeamToProject(projectId, teamId);

    return ResponseEntity.ok(updatedProject);
  }

  @PreAuthorize("hasAuthority('ADMIN')")
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
