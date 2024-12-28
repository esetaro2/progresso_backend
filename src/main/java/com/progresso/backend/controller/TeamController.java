package com.progresso.backend.controller;

import com.progresso.backend.dto.TeamDto;
import com.progresso.backend.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
@Validated
public class TeamController {

  private final TeamService teamService;

  @Autowired
  public TeamController(TeamService teamService) {
    this.teamService = teamService;
  }

  @GetMapping
  public ResponseEntity<Page<TeamDto>> getAllTeams(Pageable pageable) {
    Page<TeamDto> teamsDto = teamService.getAllTeams(pageable);
    return ResponseEntity.ok(teamsDto);
  }

  @GetMapping("/{teamId}")
  public ResponseEntity<TeamDto> getTeamById(@PathVariable Long teamId) {
    TeamDto teamDto = teamService.getTeamById(teamId);
    return ResponseEntity.ok(teamDto);
  }

  @GetMapping("/search/by-name/{name}")
  public ResponseEntity<TeamDto> getTeamByName(@PathVariable String name) {
    TeamDto teamDto = teamService.getTeamByName(name);
    return ResponseEntity.ok(teamDto);
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<TeamDto> getTeamByMemberId(@PathVariable Long userId) {
    TeamDto teamDto = teamService.getTeamByMemberId(userId);
    return ResponseEntity.ok(teamDto);
  }

  @GetMapping("/with-projects")
  public ResponseEntity<Page<TeamDto>> getTeamsWithProjects(Pageable pageable) {
    Page<TeamDto> teamsDto = teamService.getTeamsWithProjects(pageable);
    return ResponseEntity.ok(teamsDto);
  }

  @GetMapping("/min-members/{size}")
  public ResponseEntity<Page<TeamDto>> getTeamsWithMinMembers(@PathVariable int size,
      Pageable pageable) {
    Page<TeamDto> teamsDto = teamService.getTeamsWithMinMembers(size, pageable);
    return ResponseEntity.ok(teamsDto);
  }

  @GetMapping("/without-members")
  public ResponseEntity<Page<TeamDto>> getTeamsWithoutMembers(Pageable pageable) {
    Page<TeamDto> teamsDto = teamService.getTeamsWithoutMembers(pageable);
    return ResponseEntity.ok(teamsDto);
  }

  @GetMapping("/by-project/{projectId}")
  public ResponseEntity<Page<TeamDto>> getTeamsByProjectId(@PathVariable Long projectId,
      Pageable pageable) {
    Page<TeamDto> teamsDto = teamService.getTeamsByProjectId(projectId, pageable);
    return ResponseEntity.ok(teamsDto);
  }

  @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROJECTMANAGER')")
  @PostMapping
  public ResponseEntity<TeamDto> createTeam(@Valid @RequestBody TeamDto teamDto) {
    TeamDto newTeamDto = teamService.createTeam(teamDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(newTeamDto);
  }

  @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROJECTMANAGER')")
  @PutMapping("/{teamId}")
  public ResponseEntity<TeamDto> updateTeam(
      @PathVariable Long teamId, @Valid @RequestBody TeamDto teamDto) {
    TeamDto updatedTeamDto = teamService.updateTeam(teamId, teamDto);
    return ResponseEntity.ok(updatedTeamDto);
  }

  @PostMapping("/{teamId}/add-member/{userId}")
  public ResponseEntity<TeamDto> addMemberToTeam(@PathVariable Long teamId,
      @PathVariable Long userId) {
    TeamDto teamDto = teamService.addMemberToTeam(teamId, userId);
    return ResponseEntity.ok(teamDto);
  }

  @DeleteMapping("/{teamId}/remove-member/{userId}")
  public ResponseEntity<TeamDto> removeMemberFromTeam(@PathVariable Long teamId,
      @PathVariable Long userId) {
    TeamDto teamDto = teamService.removeMemberFromTeam(teamId, userId);
    return ResponseEntity.ok(teamDto); // Risultato 200 OK
  }
}

