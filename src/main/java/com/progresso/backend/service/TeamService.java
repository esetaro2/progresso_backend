package com.progresso.backend.service;

import com.progresso.backend.dto.TeamDto;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.NoDataFoundException;
import com.progresso.backend.exception.TeamNameAlreadyExistsException;
import com.progresso.backend.exception.TeamNotFoundException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.Project;
import com.progresso.backend.model.Team;
import com.progresso.backend.model.User;
import com.progresso.backend.repository.TeamRepository;
import com.progresso.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TeamService {

  private final TeamRepository teamRepository;
  private final UserRepository userRepository;

  @Autowired
  public TeamService(TeamRepository teamRepository, UserRepository userRepository) {
    this.teamRepository = teamRepository;
    this.userRepository = userRepository;
  }

  private TeamDto convertToDto(Team team) {
    TeamDto teamDto = new TeamDto();
    teamDto.setId(team.getId());
    teamDto.setName(team.getName());

    teamDto.setTeamMemberIds(
        (team.getTeamMembers() == null || team.getTeamMembers().isEmpty())
            ? new ArrayList<>()
            : team.getTeamMembers().stream()
                .map(User::getId)
                .toList()
    );
    teamDto.setProjectIds(
        (team.getProjects() == null || team.getProjects().isEmpty())
            ? new ArrayList<>()
            : team.getProjects().stream()
                .map(Project::getId)
                .toList()
    );

    return teamDto;
  }

  public TeamDto getTeamByName(String name) {
    Team team = teamRepository.findByNameIgnoreCase(name)
        .orElseThrow(() -> new TeamNotFoundException("Invalid team name: " + name));

    return convertToDto(team);
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

  public TeamDto getTeamByMemberId(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

    if (!user.getRole().equals(Role.TEAMMEMBER)) {
      throw new InvalidRoleException(
          "User with ID " + userId + " does not have the required role.");
    }

    Team team = teamRepository.findByTeamMemberId(userId)
        .orElseThrow(() -> new TeamNotFoundException("No team found for user ID: " + userId));

    return convertToDto(team);
  }

  public Page<TeamDto> getTeamsWithProjects(Pageable pageable) {
    Page<Team> teams = teamRepository.findTeamsWithProjects(pageable);

    if (teams.isEmpty()) {
      throw new NoDataFoundException("No teams found with assigned projects.");
    }

    return teams.map(this::convertToDto);
  }

  public Page<TeamDto> getTeamsWithMinMembers(int size, Pageable pageable) {
    Page<Team> teams = teamRepository.findByTeamMembersSizeGreaterThan(size, pageable);

    if (teams.isEmpty()) {
      throw new NoDataFoundException("No teams found with more than " + size + " members.");
    }

    return teams.map(this::convertToDto);
  }

  public Page<TeamDto> getTeamsWithoutMembers(Pageable pageable) {
    Page<Team> teams = teamRepository.findTeamsWithoutMembers(pageable);

    if (teams.isEmpty()) {
      throw new NoDataFoundException("No teams found without members.");
    }

    return teams.map(this::convertToDto);
  }

  public Page<TeamDto> getTeamsByProjectId(Long projectId, Pageable pageable) {
    Page<Team> teams = teamRepository.findTeamsByProjectId(projectId, pageable);

    if (teams.isEmpty()) {
      throw new NoDataFoundException("No teams found for project ID: " + projectId);
    }

    return teams.map(this::convertToDto);
  }

  @Transactional
  public TeamDto createTeam(TeamDto teamDto) {
    if (teamRepository.existsByNameIgnoreCase(teamDto.getName())) {
      throw new TeamNameAlreadyExistsException(
          "A team with the name " + teamDto.getName() + " already exists.");
    }

    Team team = new Team();
    team.setName(teamDto.getName());
    team = teamRepository.save(team);

    return convertToDto(team);
  }

  @Transactional
  public TeamDto updateTeam(Long id, TeamDto teamDto) {
    Team team = teamRepository.findById(id)
        .orElseThrow(() -> new TeamNotFoundException("Invalid team id: " + id));

    if (teamRepository.existsByNameIgnoreCaseAndIdNot(teamDto.getName(), id)) {
      throw new TeamNameAlreadyExistsException(
          "Team with name " + teamDto.getName() + " already exists.");
    }

    List<Long> existingTeamMemberIds = team.getTeamMembers().stream().map(User::getId).toList();

    if (!teamDto.getTeamMemberIds().equals(existingTeamMemberIds)) {
      throw new IllegalArgumentException("Cannot change the team members here.");
    }

    team.setName(teamDto.getName());
    team = teamRepository.save(team);

    return convertToDto(team);
  }

  @Transactional
  public TeamDto addMemberToTeam(Long teamId, Long userId) {
    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

    if (!user.getRole().equals(Role.TEAMMEMBER)) {
      throw new InvalidRoleException("User does not have the required role.");
    }

    if (team.getTeamMembers().contains(user)) {
      throw new IllegalArgumentException("User is already a member of the team.");
    }

    team.getTeamMembers().add(user);
    user.setTeam(team);
    team = teamRepository.save(team);

    return convertToDto(team);
  }

  @Transactional
  public TeamDto removeMemberFromTeam(Long teamId, Long userId) {
    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

    if (!team.getTeamMembers().contains(user)) {
      throw new IllegalArgumentException("User is not a member of the team.");
    }

    team.getTeamMembers().remove(user);
    user.setTeam(null);
    team = teamRepository.save(team);

    return convertToDto(team);
  }

}
