package com.progresso.backend.service;

import com.progresso.backend.dto.TeamDto;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.NoDataFoundException;
import com.progresso.backend.exception.TeamNameAlreadyExistsException;
import com.progresso.backend.exception.TeamNotFoundException;
import com.progresso.backend.exception.UserNotActiveException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.Project;
import com.progresso.backend.model.Task;
import com.progresso.backend.model.Team;
import com.progresso.backend.model.User;
import com.progresso.backend.repository.TaskRepository;
import com.progresso.backend.repository.TeamRepository;
import com.progresso.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class TeamService {

  private final TeamRepository teamRepository;
  private final UserRepository userRepository;
  private final TaskRepository taskRepository;

  @Autowired
  public TeamService(TeamRepository teamRepository, UserRepository userRepository,
      TaskRepository taskRepository) {
    this.teamRepository = teamRepository;
    this.userRepository = userRepository;
    this.taskRepository = taskRepository;
  }

  private TeamDto convertToDto(Team team) {
    TeamDto teamDto = new TeamDto();
    teamDto.setId(team.getId());
    teamDto.setName(team.getName());
    teamDto.setActive(team.getActive());
    teamDto.setTeamMemberIds(
        !CollectionUtils.isEmpty(team.getTeamMembers()) ? team.getTeamMembers().stream()
            .map(User::getId).toList() : new ArrayList<>());
    teamDto.setProjectIds(
        !CollectionUtils.isEmpty(team.getProjects()) ? team.getProjects().stream()
            .map(Project::getId).toList() : new ArrayList<>());

    return teamDto;
  }

  public boolean isProjectManagerOfTeamProjects(Long teamId, String username) {
    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found"));
    return team.getProjects().isEmpty() || team.getProjects().stream()
        .filter(project -> project.getCompletionDate() == null)
        .anyMatch(project -> project.getProjectManager().getUsername().equals(username));
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

  @Transactional
  public Page<TeamDto> getTeamsByMemberId(Long userId, Pageable pageable) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

    if (!user.getRole().equals(Role.TEAMMEMBER)) {
      throw new InvalidRoleException(
          "User with ID " + userId + " does not have the required role.");
    }

    Page<Team> teams = teamRepository.findByTeamMemberId(userId, pageable);

    if (!teams.hasContent()) {
      throw new NoDataFoundException("No teams found.");
    }

    return teams.map(this::convertToDto);
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
  public Page<TeamDto> getTeamsByActive(Boolean active, Pageable pageable) {
    Page<Team> teams = teamRepository.findTeamsByActive(active, pageable);

    if (teams.isEmpty()) {
      throw new NoDataFoundException("No teams found for active status: " + active);
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
    team.setActive(true);
    team = teamRepository.save(team);

    return convertToDto(team);
  }

  @Transactional
  public TeamDto updateTeam(Long id, String newName) {
    Team team = teamRepository.findById(id)
        .orElseThrow(() -> new TeamNotFoundException("Invalid team id: " + id));

    if (teamRepository.existsByNameIgnoreCaseAndIdNot(newName, id)) {
      throw new TeamNameAlreadyExistsException(
          "Team with name " + newName + " already exists.");
    }

    team.setName(newName);
    team = teamRepository.save(team);

    return convertToDto(team);
  }

  @Transactional
  public TeamDto addMemberToTeam(Long teamId, Long userId) {
    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

    if (team.getTeamMembers().contains(user)) {
      throw new IllegalArgumentException("Team already has member " + user.getUsername());
    }

    if (!team.getActive()) {
      throw new IllegalStateException("Team is not active");
    }

    if (!user.getActive()) {
      throw new UserNotActiveException("User + " + user.getUsername() + " is not active");
    }

    if (!user.getRole().equals(Role.TEAMMEMBER)) {
      throw new InvalidRoleException("User does not have the required role.");
    }

    if (user.getTeams().stream().anyMatch(Team::getActive)) {
      throw new IllegalStateException("User is already active in another team");
    }

    team.getTeamMembers().add(user);
    user.getTeams().add(team);

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

    List<Task> tasksToRemove = new ArrayList<>();

    for (Task task : user.getAssignedTasks()) {
      if (task.getProject().getTeam().equals(team)) {
        task.setAssignedUser(null);
        tasksToRemove.add(task);
        taskRepository.save(task);
      }
    }

    user.getAssignedTasks().removeAll(tasksToRemove);

    team.getTeamMembers().remove(user);
    user.getTeams().remove(team);

    team = teamRepository.save(team);
    userRepository.save(user);

    return convertToDto(team);
  }

  @Transactional
  public TeamDto deleteTeam(Long teamId) {
    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));

    if (team.getProjects().stream()
        .anyMatch(project -> !project.getStatus().equals(Status.COMPLETED))) {
      throw new IllegalStateException("Cannot delete a team with active projects.");
    }

    team.setActive(false);

    teamRepository.save(team);

    return convertToDto(team);
  }
}
