package com.progresso.backend.controller;

import com.progresso.backend.dto.MembersDto;
import com.progresso.backend.dto.TeamMemberDto;
import com.progresso.backend.service.TeamMemberService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams/{teamId}/members")
@Validated
public class TeamMemberController {

  private final TeamMemberService teamMemberService;

  @Autowired
  public TeamMemberController(TeamMemberService teamMemberService) {
    this.teamMemberService = teamMemberService;
  }

  @GetMapping("/active")
  public ResponseEntity<Page<TeamMemberDto>> getIsActiveMembersByTeamId(
      @PathVariable("teamId") Long teamId,
      @RequestParam Boolean isActive,
      Pageable pageable) {
    Page<TeamMemberDto> members = teamMemberService.findActiveMembersByTeamId(teamId, isActive,
        pageable);
    return ResponseEntity.ok(members);
  }

  @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROJECTMANAGER')")
  @PostMapping
  public ResponseEntity<TeamMemberDto> addMembersToTeam(
      @PathVariable("teamId") Long teamId,
      @RequestBody @Valid MembersDto membersDto) {
    TeamMemberDto teamMemberDto = teamMemberService.addMembersToTeam(teamId, membersDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(teamMemberDto);
  }

  @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROJECTMANAGER')")
  @DeleteMapping
  public ResponseEntity<List<TeamMemberDto>> removeMembersFromTeam(
      @PathVariable("teamId") Long teamId,
      @RequestBody @Valid MembersDto membersDto) {
    List<TeamMemberDto> removedMembers = teamMemberService.removeMembersFromTeam(teamId,
        membersDto);
    return ResponseEntity.ok(removedMembers);
  }
}
