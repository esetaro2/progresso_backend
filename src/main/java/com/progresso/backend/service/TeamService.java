package com.progresso.backend.service;

import com.progresso.backend.dto.TeamDto;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.NoDataFoundException;
import com.progresso.backend.exception.ProjectNotFoundException;
import com.progresso.backend.exception.TeamNotFoundException;
import com.progresso.backend.exception.UserNotActiveException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.Project;
import com.progresso.backend.model.Task;
import com.progresso.backend.model.Team;
import com.progresso.backend.model.User;
import com.progresso.backend.repository.ProjectRepository;
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
  private final ProjectRepository projectRepository;

  @Autowired
  public TeamService(TeamRepository teamRepository, UserRepository userRepository,
      TaskRepository taskRepository, ProjectRepository projectRepository) {
    this.teamRepository = teamRepository;
    this.userRepository = userRepository;
    this.taskRepository = taskRepository;
    this.projectRepository = projectRepository;
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
    if (teamId == null || username == null || username.isEmpty()) {
      throw new IllegalArgumentException("Team ID and username cannot be null or empty");
    }

    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found"));

    List<Project> activeProjects = team.getProjects().stream()
        .filter(project -> project.getCompletionDate() == null)
        .toList();

    if (activeProjects.isEmpty()) {
      return true;
    }

    return activeProjects.stream()
        .anyMatch(project ->
            project.getProjectManager() != null
                && username.equals(project.getProjectManager().getUsername())
        );
  }

  public TeamDto getTeamByName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Team name cannot be null or empty");
    }

    Team team = teamRepository.findByNameIgnoreCase(name)
        .orElseThrow(() -> new TeamNotFoundException("Invalid team name: " + name));

    return convertToDto(team);
  }


  public TeamDto getTeamById(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("Team id cannot be null");
    }

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

  public Page<TeamDto> getTeamsWithoutActiveProjects(Pageable pageable, String searchTerm) {
    List<Status> activeStatuses = List.of(Status.NOT_STARTED, Status.IN_PROGRESS);

    String processedSearchTerm =
        (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm.trim() : null;

    Page<Team> teams = teamRepository.findTeamsWithoutActiveProjects(activeStatuses,
        processedSearchTerm, pageable);

    if (teams.isEmpty()) {
      throw new NoDataFoundException("No available teams found.");
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
    if (projectId == null) {
      throw new IllegalArgumentException("Project ID cannot be null");
    }

    projectRepository.findById(projectId)
        .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

    Page<Team> teams = teamRepository.findTeamsByProjectId(projectId, pageable);

    if (teams.isEmpty()) {
      throw new NoDataFoundException("No teams found for project ID: " + projectId);
    }

    return teams.map(this::convertToDto);
  }

  public Page<TeamDto> getTeamsByActive(Boolean active, Pageable pageable) {
    Page<Team> teams = teamRepository.findTeamsByActive(active, pageable);

    if (teams.isEmpty()) {
      throw new NoDataFoundException("No teams found for active status: " + active);
    }

    return teams.map(this::convertToDto);
  }

  @Transactional
  public TeamDto createTeam(String teamName) {
    var finalTeamName = teamName;
    int counter = 1;

    while (teamRepository.existsByNameIgnoreCase(finalTeamName)) {
      String suffix = " (" + counter + ")";

      if ((finalTeamName.length() + suffix.length()) > 100) {
        finalTeamName = finalTeamName.substring(0, 100 - suffix.length());
        break;
      }

      finalTeamName = finalTeamName + suffix;
      counter++;
    }

    Team team = new Team();
    team.setName(finalTeamName);
    team.setActive(true);

    team = teamRepository.save(team);

    return convertToDto(team);
  }

  @Transactional
  public TeamDto updateTeam(Long id, String newName) {
    if (id == null) {
      throw new IllegalArgumentException("Team id cannot be null");
    }

    Team team = teamRepository.findById(id)
        .orElseThrow(() -> new TeamNotFoundException("Invalid team id: " + id));

    String finalName = newName;
    int counter = 1;
    while (teamRepository.existsByNameIgnoreCaseAndIdNot(finalName, id)) {
      String suffix = " (" + counter + ")";

      if ((finalName.length() + suffix.length()) > 100) {
        finalName = finalName.substring(0, 100 - suffix.length());
        break;
      }

      finalName = finalName + suffix;
      counter++;
    }

    team.setName(finalName);
    team = teamRepository.save(team);

    return convertToDto(team);
  }

  @Transactional
  public TeamDto addMembersToTeam(Long teamId, List<Long> userIds) {
    if (teamId == null) {
      throw new IllegalArgumentException("Team id cannot be null");
    }

    if (userIds == null || userIds.isEmpty()) {
      throw new IllegalArgumentException("Users list cannot be null or empty");
    }

    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));

    List<User> users = userRepository.findAllById(userIds);

    if (users.size() != userIds.size()) {
      throw new UserNotFoundException("One or more users not found.");
    }

    for (User member : users) {
      if (team.getTeamMembers().contains(member)) {
        throw new IllegalArgumentException("Team already has member " + member.getUsername());
      }

      if (!team.getActive()) {
        throw new IllegalStateException("Team is not active");
      }

      if (!member.getActive()) {
        throw new UserNotActiveException("User " + member.getUsername() + " is not active");
      }

      if (!member.getRole().equals(Role.TEAMMEMBER)) {
        throw new InvalidRoleException(
            "User " + member.getUsername() + " does not have the required role.");
      }

      if (member.getTeams().stream().anyMatch(Team::getActive)) {
        throw new IllegalStateException(
            "User " + member.getUsername() + " is already active in another team");
      }

      team.getTeamMembers().add(member);
      member.getTeams().add(team);
    }

    team = teamRepository.save(team);

    return convertToDto(team);
  }

  @Transactional
  public TeamDto removeMembersFromTeam(Long teamId, List<Long> userIds) {
    if (teamId == null) {
      throw new IllegalArgumentException("Team id cannot be null");
    }

    if (userIds == null || userIds.isEmpty()) {
      throw new IllegalArgumentException("Users list cannot be null or empty");
    }

    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));

    List<User> users = userRepository.findAllById(userIds);

    if (users.size() != userIds.size()) {
      List<Long> foundIds = users.stream().map(User::getId).toList();
      List<Long> notFoundIds = new ArrayList<>(userIds);
      notFoundIds.removeAll(foundIds);
      throw new UserNotFoundException("Users not found with IDs: " + notFoundIds);
    }

    for (User user : users) {
      if (!user.getActive()) {
        throw new UserNotActiveException("User " + user.getUsername() + " is not active.");
      }

      if (!team.getTeamMembers().contains(user)) {
        throw new IllegalArgumentException(
            "User with ID " + user.getId() + " is not a member of the team.");
      }
    }

    for (User user : users) {
      List<Task> tasksToUpdate = user.getAssignedTasks().stream()
          .filter(task -> task.getProject().getTeam().equals(team))
          .peek(task -> task.setAssignedUser(null)).toList();

      user.getAssignedTasks().removeAll(tasksToUpdate);
      taskRepository.saveAll(tasksToUpdate);
    }

    for (User user : users) {
      team.getTeamMembers().remove(user);
      user.getTeams().remove(team);
    }

    teamRepository.save(team);
    userRepository.saveAll(users);

    return convertToDto(team);
  }

  @Transactional
  public TeamDto deleteTeam(Long teamId) {
    if (teamId == null) {
      throw new IllegalArgumentException("Team id cannot be null");
    }

    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));

    boolean hasActiveProjects = team.getProjects().stream()
        .anyMatch(project -> !project.getStatus().equals(Status.COMPLETED));

    if (hasActiveProjects) {
      throw new IllegalStateException("Cannot delete a team with active projects.");
    }

    team.setActive(false);

    teamRepository.save(team);

    return convertToDto(team);
  }
}
