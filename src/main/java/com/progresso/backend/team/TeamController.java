package com.progresso.backend.team;

import com.progresso.backend.dto.TeamDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
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

  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping
  public ResponseEntity<Page<TeamDto>> getAllTeams(
      @RequestParam(required = false) Boolean active,
      @RequestParam(required = false) String searchTerm,
      Pageable pageable) {
    Page<TeamDto> teamsDto = teamService.getAllTeamsWithFilters(active, searchTerm, pageable);
    return ResponseEntity.ok(teamsDto);
  }

  @GetMapping("/{teamId}")
  public ResponseEntity<TeamDto> getTeamById(@PathVariable Long teamId) {
    TeamDto teamDto = teamService.getTeamById(teamId);
    return ResponseEntity.ok(teamDto);
  }

  @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROJECTMANAGER')")
  @GetMapping("/availableTeams")
  public ResponseEntity<Page<TeamDto>> getAvailableTeams(
      @RequestParam(required = false) String searchTerm,
      Pageable pageable) {
    Page<TeamDto> teamsDto = teamService.getTeamsWithoutActiveProjects(pageable, searchTerm);
    return ResponseEntity.ok(teamsDto);
  }

  @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROJECTMANAGER')")
  @PostMapping
  public ResponseEntity<TeamDto> createTeam(
      @RequestParam @NotBlank(message = "Name cannot be empty.")
      @Size(max = 100, message = "Name cannot exceed 100 characters.") String teamName) {
    TeamDto newTeamDto = teamService.createTeam(teamName);
    return ResponseEntity.status(HttpStatus.CREATED).body(newTeamDto);
  }

  @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROJECTMANAGER') "
      + "and @teamService.isProjectManagerOfTeamProjects(#teamId, authentication.name)")
  @PutMapping("/{teamId}")
  public ResponseEntity<TeamDto> updateTeam(
      @PathVariable Long teamId,
      @RequestParam @NotBlank(message = "Name cannot be empty.")
      @Size(max = 100, message = "Name cannot exceed 100 characters.") String newName) {
    TeamDto updatedTeamDto = teamService.updateTeam(teamId, newName);
    return ResponseEntity.ok(updatedTeamDto);
  }

  @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROJECTMANAGER') "
      + "and @teamService.isProjectManagerOfTeamProjects(#teamId, authentication.name)")
  @PostMapping("/{teamId}/members")
  public ResponseEntity<TeamDto> addMembersToTeam(
      @PathVariable Long teamId,
      @RequestBody List<Long> userIds) {
    TeamDto teamDto = teamService.addMembersToTeam(teamId, userIds);
    return ResponseEntity.ok(teamDto);
  }

  @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROJECTMANAGER') "
      + "and @teamService.isProjectManagerOfTeamProjects(#teamId, authentication.name)")
  @PostMapping("/{teamId}/remove-members")
  public ResponseEntity<TeamDto> removeMembersFromTeam(
      @PathVariable Long teamId,
      @RequestBody List<Long> userIds) {
    TeamDto teamDto = teamService.removeMembersFromTeam(teamId, userIds);
    return ResponseEntity.ok(teamDto);
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @DeleteMapping("/{teamId}")
  public ResponseEntity<TeamDto> deleteTeam(@PathVariable Long teamId) {
    TeamDto teamDto = teamService.deleteTeam(teamId);
    return ResponseEntity.ok(teamDto);
  }
}