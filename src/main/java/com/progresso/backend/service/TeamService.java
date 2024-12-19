package com.progresso.backend.service;

import com.progresso.backend.dto.TeamDto;
import com.progresso.backend.enumeration.RoleType;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.MaxActiveTeamsExceededException;
import com.progresso.backend.exception.NoDataFoundException;
import com.progresso.backend.exception.TeamNameAlreadyExistsException;
import com.progresso.backend.exception.TeamNotFoundException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.Team;
import com.progresso.backend.model.TeamMember;
import com.progresso.backend.model.User;
import com.progresso.backend.repository.TeamMemberRepository;
import com.progresso.backend.repository.TeamRepository;
import com.progresso.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TeamService {

  private final TeamRepository teamRepository;
  private final TeamMemberRepository teamMemberRepository;
  private final UserRepository userRepository;

  @Autowired
  public TeamService(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository,
      UserRepository userRepository) {
    this.teamRepository = teamRepository;
    this.teamMemberRepository = teamMemberRepository;
    this.userRepository = userRepository;
  }

  private TeamDto convertToDto(Team team) {
    TeamDto teamDto = new TeamDto();
    teamDto.setId(team.getId());
    teamDto.setName(team.getName());
    teamDto.setIsActive(team.getIsActive());
    teamDto.setProjectManagerId(team.getProjectManager().getId());

    teamDto.setMemberIds(
        (team.getMembers() == null || team.getMembers().isEmpty())
            ? new ArrayList<>()
            : team.getMembers().stream()
                .map(TeamMember::getUser)
                .map(User::getId)
                .toList()
    );

    return teamDto;
  }


  public TeamDto getTeamByName(String name) {
    Team team = teamRepository.findByNameIgnoreCase(name)
        .orElseThrow(() -> new TeamNotFoundException("Invalid team name: " + name));

    return convertToDto(team);
  }

  public Page<TeamDto> getTeamsByProjectManagerId(Long projectManagerId, Pageable pageable) {
    User projectManager = userRepository.findById(projectManagerId)
        .orElseThrow(() -> new UserNotFoundException("Invalid user id: " + projectManagerId));

    if (!projectManager.getRole().equals(RoleType.PROJECTMANAGER)) {
      throw new InvalidRoleException("Invalid role: " + projectManager.getRole());
    }
    Page<Team> teams = teamRepository.findByProjectManagerId(projectManagerId, pageable);
    if (!teams.hasContent()) {
      throw new NoDataFoundException(
          "No teams found with project manager id: " + projectManagerId);
    }

    return teams.map(this::convertToDto);
  }

  public Page<TeamDto> getTeamsByMemberId(Long memberId, Pageable pageable) {
    User member = userRepository.findById(memberId)
        .orElseThrow(() -> new UserNotFoundException("Invalid user id: " + memberId));

    if (!member.getRole().equals(RoleType.TEAMMEMBER)) {
      throw new InvalidRoleException("Invalid role: " + member.getRole());
    }

    Page<Team> teams = teamRepository.findByMembersUserId(memberId, pageable);
    if (!teams.hasContent()) {
      throw new NoDataFoundException("No teams found with member id: " + memberId);
    }

    return teams.map(this::convertToDto);
  }


  public TeamDto getTeamById(Long id) {
    return teamRepository.findById(id)
        .map(this::convertToDto)
        .orElseThrow(() -> new TeamNotFoundException("Invalid team id: " + id));
  }

  public Page<TeamDto> getAllTeams(Pageable pageable) {
    Page<TeamDto> teamsDto = teamRepository.findAllTeams(pageable).map(this::convertToDto);
    if (!teamsDto.hasContent()) {
      throw new NoDataFoundException("No teams found.");
    }

    return teamsDto;
  }

  public Page<TeamDto> getTeamsByIsActive(Boolean isActive, Pageable pageable) {
    Page<TeamDto> teamsDto = teamRepository.findTeamsByIsActive(isActive, pageable)
        .map(this::convertToDto);
    if (!teamsDto.hasContent()) {
      throw new NoDataFoundException("No teams found.");
    }

    return teamsDto;
  }

  public Page<TeamDto> getTeamByProjectManagerIdAndIsActive(Long projectManagerId, Boolean isActive,
      Pageable pageable) {
    User projectManager = userRepository.findById(projectManagerId)
        .orElseThrow(() -> new UserNotFoundException("Invalid user id: " + projectManagerId));

    if (!projectManager.getRole().equals(RoleType.PROJECTMANAGER)) {
      throw new InvalidRoleException("Invalid role: " + projectManager.getRole());
    }

    Page<TeamDto> teamsDto = teamRepository.findByProjectManagerIdAndIsActive(projectManagerId,
        isActive, pageable).map(this::convertToDto);

    if (!teamsDto.hasContent()) {
      throw new NoDataFoundException("No teams found with project manager id: " + projectManagerId);
    }

    return teamsDto;
  }

  public Page<TeamDto> findTeamsByAdvancedFilters(
      String name,
      Boolean isActive,
      Long projectManagerId,
      Long memberUserId,
      Pageable pageable) {

    if (projectManagerId != null) {
      User projectManager = userRepository.findById(projectManagerId)
          .orElseThrow(
              () -> new UserNotFoundException("Invalid project manager ID: " + projectManagerId));

      if (!projectManager.getRole().equals(RoleType.PROJECTMANAGER)) {
        throw new InvalidRoleException("User ID " + projectManagerId + " is not a Project Manager");
      }
    }

    if (memberUserId != null) {
      User member = userRepository.findById(memberUserId)
          .orElseThrow(() -> new UserNotFoundException("Invalid member ID: " + memberUserId));

      if (!member.getRole().equals(RoleType.TEAMMEMBER)) {
        throw new InvalidRoleException("User ID " + memberUserId + " is not a Team Member");
      }
    }

    Page<Team> teams = teamRepository.findTeamsByAdvancedFilters(
        name != null ? name.toLowerCase() : null,
        isActive,
        projectManagerId,
        memberUserId,
        pageable
    );

    if (!teams.hasContent()) {
      throw new NoDataFoundException("No teams found with the given filters");
    }

    return teams.map(this::convertToDto);
  }


  public Long countActiveMembersByTeamId(Long teamId) {
    teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Invalid team id: " + teamId));

    return teamRepository.countActiveMembersByTeamId(teamId);
  }

  @Transactional
  public TeamDto createTeam(TeamDto teamDto) {
    if (teamRepository.existsByNameIgnoreCase(teamDto.getName())) {
      throw new TeamNameAlreadyExistsException(
          "A team with the name " + teamDto.getName() + " already exists.");
    }

    User projectManager = userRepository.findById(teamDto.getProjectManagerId()).orElseThrow(
        () -> new UserNotFoundException(
            "Invalid project manager id: " + teamDto.getProjectManagerId()));

    if (!projectManager.getRole().equals(RoleType.PROJECTMANAGER)) {
      throw new InvalidRoleException(
          "The user is not a project manager: " + projectManager.getUsername());
    }

    if (projectManager.getManagedTeams().stream()
        .filter(Team::getIsActive).limit(6).count() == 6) {
      throw new MaxActiveTeamsExceededException(
          "The project manager cannot be active in more than 5 teams: "
              + projectManager.getUsername());
    }

    Team team = teamDto.toEntity(projectManager);
    team = teamRepository.save(team);

    return convertToDto(team);
  }

  @Transactional
  public TeamDto updateTeam(Long id, TeamDto teamDto) {
    Team team = teamRepository.findById(id)
        .orElseThrow(() -> new TeamNotFoundException("Invalid team id: " + id));

    if (!team.getIsActive()) {
      throw new IllegalArgumentException("The team is not active: " + team.getName());
    }

    if (teamRepository.existsByNameIgnoreCaseAndIdNot(teamDto.getName(), id)) {
      throw new TeamNameAlreadyExistsException(
          "Team with name " + teamDto.getName() + " already exists.");
    }

    if (!teamDto.getProjectManagerId().equals(team.getProjectManager().getId())) {
      throw new IllegalArgumentException("Cannot change the project manager id here.");
    }

    if (!teamDto.getIsActive().equals(team.getIsActive())) {
      throw new IllegalArgumentException("Cannot change the team state here.");
    }

    if (!teamDto.getMemberIds()
        .equals(team.getMembers().stream().map(TeamMember::getUser).map(User::getId).toList())) {
      throw new IllegalArgumentException("Cannot change the team members here.");
    }

    team.setName(teamDto.getName());
    team = teamRepository.save(team);

    return convertToDto(team);
  }

  @Transactional
  public TeamDto changeProjectManager(Long id, Long projectManagerId) {
    Team team = teamRepository.findById(id)
        .orElseThrow(() -> new TeamNotFoundException("Invalid team id: " + id));

    User newProjectManager = userRepository.findById(projectManagerId).orElseThrow(
        () -> new UserNotFoundException("Invalid project manager id: " + projectManagerId));
    if (!newProjectManager.getRole().equals(RoleType.PROJECTMANAGER)) {
      throw new InvalidRoleException(
          "The user is not a project manager: " + newProjectManager.getUsername());
    }

    if (newProjectManager.getManagedTeams().stream()
        .filter(Team::getIsActive).limit(6).count() == 6) {
      throw new MaxActiveTeamsExceededException(
          "The project manager cannot be active in more than 5 teams: "
              + newProjectManager.getUsername());
    }

    if (team.getProjectManager().getId().equals(newProjectManager.getId())) {
      throw new IllegalArgumentException(
          "The selected user is already the current project manager.");
    }

    team.setProjectManager(newProjectManager);
    team = teamRepository.save(team);

    return convertToDto(team);
  }

  @Transactional
  public TeamDto deactivateTeam(Long teamId) {
    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found"));

    team.setIsActive(false);
    teamRepository.save(team);

    List<TeamMember> activeMembers = teamMemberRepository.findByTeamAndIsActiveTrue(team);
    for (TeamMember member : activeMembers) {
      member.setIsActive(false);
      member.setRemoveDate(LocalDate.now());
    }

    teamMemberRepository.saveAll(activeMembers);

    return convertToDto(team);
  }
}
