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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  @GetMapping("/project-managers/{projectManagerId}")
  public ResponseEntity<Page<TeamDto>> getTeamsByProjectManager(
      @PathVariable Long projectManagerId, Pageable pageable) {
    Page<TeamDto> teamsDto = teamService.getTeamsByProjectManagerId(projectManagerId, pageable);
    return ResponseEntity.ok(teamsDto);
  }

  @GetMapping("/members/{memberId}")
  public ResponseEntity<Page<TeamDto>> getTeamsByTeamMember(
      @PathVariable Long memberId, Pageable pageable) {
    Page<TeamDto> teamsDto = teamService.getTeamsByMemberId(memberId, pageable);
    return ResponseEntity.ok(teamsDto);
  }

  @GetMapping("/filter/by-status")
  public ResponseEntity<Page<TeamDto>> getTeamsByIsActive(
      @RequestParam Boolean isActive, Pageable pageable) {
    Page<TeamDto> teamsDto = teamService.getTeamsByIsActive(isActive, pageable);
    return ResponseEntity.ok(teamsDto);
  }

  @GetMapping("/project-managers/{projectManagerId}/filter/by-status")
  public ResponseEntity<Page<TeamDto>> getTeamsByProjectManagerAndStatus(
      @PathVariable Long projectManagerId, @RequestParam Boolean isActive, Pageable pageable) {
    Page<TeamDto> teamsDto = teamService.getTeamByProjectManagerIdAndIsActive(
        projectManagerId, isActive, pageable);
    return ResponseEntity.ok(teamsDto);
  }

  @GetMapping("/search/advanced")
  public ResponseEntity<Page<TeamDto>> findTeamsByAdvancedFilters(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) Boolean isActive,
      @RequestParam(required = false) Long projectManagerId,
      @RequestParam(required = false) Long memberUserId,
      Pageable pageable) {
    Page<TeamDto> teams = teamService.findTeamsByAdvancedFilters(
        name, isActive, projectManagerId, memberUserId, pageable);
    return ResponseEntity.ok(teams);
  }

  @GetMapping("/{teamId}/members/active/count")
  public ResponseEntity<Long> countActiveMembersByTeamId(@PathVariable Long teamId) {
    Long count = teamService.countActiveMembersByTeamId(teamId);
    return ResponseEntity.ok(count);
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

  @PreAuthorize("hasAuthority('ADMIN')")
  @PutMapping("/{teamId}/project-manager")
  public ResponseEntity<TeamDto> changeProjectManager(
      @PathVariable Long teamId, @RequestParam Long projectManagerId) {
    TeamDto teamDto = teamService.changeProjectManager(teamId, projectManagerId);
    return ResponseEntity.ok(teamDto);
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PutMapping("/{teamId}/deactivate")
  public ResponseEntity<TeamDto> deactivateTeam(@PathVariable Long teamId) {
    TeamDto teamDto = teamService.deactivateTeam(teamId);
    return ResponseEntity.ok(teamDto);
  }
}

