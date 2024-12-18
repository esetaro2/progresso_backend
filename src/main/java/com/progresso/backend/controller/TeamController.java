package com.progresso.backend.controller;

import com.progresso.backend.dto.MembersDto;
import com.progresso.backend.dto.TeamDto;
import com.progresso.backend.service.TeamService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/team")
@Validated
public class TeamController {

  private final TeamService teamService;

  @Autowired
  public TeamController(TeamService teamService) {
    this.teamService = teamService;
  }

  @GetMapping
  public ResponseEntity<List<TeamDto>> getTeams() {
    List<TeamDto> teamsDto = teamService.getAllTeams();
    return ResponseEntity.ok(teamsDto);
  }

  @GetMapping("/{id}")
  public ResponseEntity<TeamDto> getTeamById(@PathVariable Long id) {
    TeamDto teamDto = teamService.getTeamById(id);
    return ResponseEntity.ok(teamDto);
  }

  @GetMapping("/name")
  public ResponseEntity<TeamDto> getTeamByName(@RequestParam String name) {
    TeamDto teamDto = teamService.getTeamByName(name);
    return ResponseEntity.ok(teamDto);
  }

  @GetMapping("/projectManager/{id}")
  public ResponseEntity<List<TeamDto>> getTeamByProjectManager(@PathVariable Long id) {
    List<TeamDto> teamsDto = teamService.getTeamsByProjectManagerId(id);
    return ResponseEntity.ok(teamsDto);
  }

  @GetMapping("/teamMember/{id}")
  public ResponseEntity<List<TeamDto>> getTeamByTeamMember(@PathVariable Long id) {
    List<TeamDto> teamsDto = teamService.getTeamsByMemberId(id);
    return ResponseEntity.ok(teamsDto);
  }

  @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROJECTMANAGER')")
  @PostMapping
  public ResponseEntity<TeamDto> createTeam(@Valid @RequestBody TeamDto teamDto) {
    TeamDto newTeamDto = teamService.createTeam(teamDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(newTeamDto);
  }

  @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROJECTMANAGER')")
  @PostMapping("/{id}/members")
  public ResponseEntity<TeamDto> addMembersToTeam(@PathVariable Long id,
      @Valid @RequestBody MembersDto membersDto) {
    TeamDto teamDto = teamService.addMembersToTeam(id, membersDto);
    return ResponseEntity.ok(teamDto);
  }

  @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROJECTMANAGER')")
  @PutMapping("/{id}")
  public ResponseEntity<TeamDto> updateTeam(@PathVariable Long id,
      @Valid @RequestBody TeamDto teamDto) {
    TeamDto updatedTeamDto = teamService.updateTeam(id, teamDto);
    return ResponseEntity.ok(updatedTeamDto);
  }

  @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROJECTMANAGER')")
  @PutMapping("/{id}/removeMembers")
  public ResponseEntity<TeamDto> removeMembersFromTeam(@PathVariable Long id,
      @Valid @RequestBody MembersDto membersDto) {
    TeamDto teamDto = teamService.removeMembersFromTeam(id, membersDto);
    return ResponseEntity.ok(teamDto);
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PutMapping("/{id}/projectManager")
  public ResponseEntity<TeamDto> changeProjectManager(@PathVariable Long id,
      @RequestParam Long projectManagerId) {
    TeamDto teamDto = teamService.changeProjectManager(id, projectManagerId);
    return ResponseEntity.ok(teamDto);
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PutMapping("/{id}/deactivate")
  public ResponseEntity<TeamDto> deactivateTeam(@PathVariable Long id) {
    TeamDto teamDto = teamService.deactivateTeam(id);
    return ResponseEntity.ok(teamDto);
  }
}
