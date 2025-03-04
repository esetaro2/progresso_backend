package com.progresso.backend.service;

import com.progresso.backend.dto.TeamDto;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.NoDataFoundException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class TeamService {

  private static final Logger logger = LoggerFactory.getLogger(TeamService.class);

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

  @SuppressWarnings("checkstyle:LineLength")
  public boolean isProjectManagerOfTeamProjects(Long teamId, String username) {
    if (teamId == null || username == null || username.isEmpty()) {
      logger.error("isProjectManagerOfTeamProjects: Team ID and username cannot be null or empty.");
      throw new IllegalArgumentException("Team ID and username cannot be null or empty.");
    }

    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> {
          logger.error("isProjectManagerOfTeamProjects: Team not found with ID: {}", teamId);
          return new TeamNotFoundException("Team not found.");
        });

    List<Project> activeProjects = team.getProjects().stream()
        .filter(project -> project.getCompletionDate() == null)
        .toList();

    if (activeProjects.isEmpty()) {
      logger.info("isProjectManagerOfTeamProjects: No active projects for team with ID: {}",
          teamId);
      return true;
    }

    boolean isProjectManager = activeProjects.stream()
        .anyMatch(project ->
            project.getProjectManager() != null
                && username.equals(project.getProjectManager().getUsername())
        );

    if (isProjectManager) {
      logger.info(
          "isProjectManagerOfTeamProjects: User {} is project manager of one or more active projects in team with ID: {}",
          username, teamId);
    } else {
      logger.info(
          "isProjectManagerOfTeamProjects: User {} is not project manager of any active projects in team with ID: {}",
          username, teamId);
    }

    return isProjectManager;
  }

  public boolean isTeamMemberOfTeam(Long teamId, String username) {
    logger.info("Checking if user {} is a member of team {}", username, teamId);
    if (teamId == null || username == null || username.isEmpty()) {
      logger.error("isTeamMemberOfTeam: Team ID and username cannot be null or empty.");
      throw new IllegalArgumentException("Team ID and username cannot be null or empty.");
    }

    userRepository.findByUsername(username)
        .orElseThrow(() -> {
          logger.error("isTeamMemberOfTeam: User not found with username: {}", username);
          return new UserNotFoundException("User not found.");
        });

    Team team = teamRepository.findById(teamId).orElseThrow(() -> {
      logger.error("isTeamMemberOfTeam: Team not found with ID: {}", teamId);
      return new TeamNotFoundException("Team not found.");
    });

    boolean isMember = team.getTeamMembers().stream().map(User::getUsername)
        .anyMatch(username::equals);
    logger.info("User {} is a member of team {}: {}", username, teamId, isMember);

    return isMember;
  }

  public TeamDto getTeamById(Long id) {
    if (id == null) {
      logger.error("getTeamById: Team id cannot be null.");
      throw new IllegalArgumentException("Team id cannot be null.");
    }

    return teamRepository.findById(id)
        .map(team -> {
          logger.info("getTeamById: Team found with id: {}", id);
          return convertToDto(team);
        })
        .orElseThrow(() -> {
          logger.error("getTeamById: Team not found with id: {}", id);
          return new TeamNotFoundException("Team not found.");
        });
  }

  public Page<TeamDto> getAllTeamsWithFilters(Boolean active, String searchTerm,
      Pageable pageable) {
    String processedSearchTerm =
        (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm.trim() : null;

    Page<TeamDto> teamsDto = teamRepository.findAllTeamsWithFilters(active, processedSearchTerm,
        pageable).map(this::convertToDto);

    if (teamsDto.isEmpty()) {
      logger.warn("getAllTeams: No teams found.");
      throw new NoDataFoundException("No teams found.");
    }

    logger.info("getAllTeams: Retrieved {} teams.", teamsDto.getTotalElements());
    return teamsDto;
  }

  @SuppressWarnings("checkstyle:LineLength")
  public Page<TeamDto> getTeamsWithoutActiveProjects(Pageable pageable, String searchTerm) {
    List<Status> activeStatuses = List.of(Status.NOT_STARTED, Status.IN_PROGRESS);

    String processedSearchTerm =
        (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm.trim() : null;

    Page<Team> teams = teamRepository.findTeamsWithoutActiveProjects(activeStatuses,
        processedSearchTerm, pageable);

    if (teams.isEmpty()) {
      logger.warn("getTeamsWithoutActiveProjects: No available teams found for search term: {}",
          searchTerm);
      throw new NoDataFoundException("No available teams found.");
    }

    logger.info(
        "getTeamsWithoutActiveProjects: Retrieved {} teams without active projects for search term: {}",
        teams.getTotalElements(), searchTerm);
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

    logger.info("createTeam: Created team with name: {}", finalTeamName);
    return convertToDto(team);
  }

  @Transactional
  public TeamDto updateTeam(Long id, String newName) {
    if (id == null) {
      logger.error("updateTeam: Team id cannot be null.");
      throw new IllegalArgumentException("Team id cannot be null.");
    }

    Team team = teamRepository.findById(id)
        .orElseThrow(() -> {
          logger.error("updateTeam: Team not found with ID: {}", id);
          return new TeamNotFoundException("Team not found.");
        });

    if (!team.getActive()) {
      logger.error("updateTeam: Cannot update an inactive team. Team ID: {}, Team Name: {}",
          team.getId(), team.getName());
      throw new IllegalArgumentException("Cannot update an inactive team.");
    }

    String finalName = newName;
    if (team.getName().equals(finalName)) {
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
    }

    team.setName(finalName);
    team = teamRepository.save(team);

    logger.info("updateTeam: Updated team with ID: {} to name: {}", id, finalName);
    return convertToDto(team);
  }

  @Transactional
  public TeamDto addMembersToTeam(Long teamId, List<Long> userIds) {
    if (teamId == null) {
      logger.error("addMembersToTeam: Team id cannot be null.");
      throw new IllegalArgumentException("Team id cannot be null.");
    }

    if (userIds == null || userIds.isEmpty()) {
      logger.error("addMembersToTeam: Users list cannot be null or empty.");
      throw new IllegalArgumentException("Users list cannot be null or empty.");
    }

    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> {
          logger.error("addMembersToTeam: Team not found with ID: {}", teamId);
          return new TeamNotFoundException("Team not found.");
        });

    if (!team.getActive()) {
      logger.error("addMembersToTeam: Team {} is not active.", team.getName());
      throw new IllegalStateException("This team is not active.");
    }

    List<User> users = userRepository.findAllById(userIds);

    if (users.size() != userIds.size()) {
      List<Long> foundIds = users.stream().map(User::getId).toList();
      List<Long> notFoundIds = new ArrayList<>(userIds);
      notFoundIds.removeAll(foundIds);
      logger.error("addMembersToTeam: Some users have not been found. Not found IDs: {}",
          notFoundIds);
      throw new UserNotFoundException("Some users have not been found.");
    }

    for (User member : users) {
      if (team.getTeamMembers().contains(member)) {
        logger.error("addMembersToTeam: Team {} already has member {}.", team.getName(),
            member.getUsername());
        throw new IllegalArgumentException(
            "This team already has this member: " + member.getUsername());
      }

      if (!member.getActive()) {
        logger.error("addMembersToTeam: User {} is not active.", member.getUsername());
        throw new UserNotActiveException("User " + member.getUsername() + " is not active.");
      }

      if (!member.getRole().equals(Role.TEAMMEMBER)) {
        logger.error("addMembersToTeam: User {} does not have the required role.",
            member.getUsername());
        throw new InvalidRoleException(
            "User " + member.getUsername() + " does not have the required role.");
      }

      if (member.getTeams().stream().anyMatch(Team::getActive)) {
        logger.error("addMembersToTeam: User {} is already active in another team.",
            member.getUsername());
        throw new IllegalStateException(
            "User " + member.getUsername() + " is already active in another team.");
      }

      team.getTeamMembers().add(member);
      member.getTeams().add(team);
    }

    team = teamRepository.save(team);

    logger.info("addMembersToTeam: Successfully added members to team with ID: {}", teamId);
    return convertToDto(team);
  }

  @Transactional
  public TeamDto removeMembersFromTeam(Long teamId, List<Long> userIds) {
    if (teamId == null) {
      logger.error("removeMembersFromTeam: Team id cannot be null.");
      throw new IllegalArgumentException("Team id cannot be null.");
    }

    if (userIds == null || userIds.isEmpty()) {
      logger.error("removeMembersFromTeam: Users list cannot be null or empty.");
      throw new IllegalArgumentException("Users list cannot be null or empty.");
    }

    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> {
          logger.error("removeMembersFromTeam: Team not found with ID: {}", teamId);
          return new TeamNotFoundException("Team not found.");
        });

    if (!team.getActive()) {
      logger.error("removeMembersFromTeam: Team {} is not active.", team.getName());
      throw new IllegalStateException("This team is not active.");
    }

    List<User> users = userRepository.findAllById(userIds);

    if (users.size() != userIds.size()) {
      List<Long> foundIds = users.stream().map(User::getId).toList();
      List<Long> notFoundIds = new ArrayList<>(userIds);
      notFoundIds.removeAll(foundIds);
      logger.error("removeMembersFromTeam: Some users have not been found. Not found IDs: {}",
          notFoundIds);
      throw new UserNotFoundException("Some users have not been found.");
    }

    for (User user : users) {
      if (!user.getActive()) {
        logger.error("removeMembersFromTeam: User {} is not active.", user.getUsername());
        throw new UserNotActiveException("User " + user.getUsername() + " is not active.");
      }

      if (!team.getTeamMembers().contains(user)) {
        logger.error("removeMembersFromTeam: User with ID {} is not a member of the team.",
            user.getId());
        throw new IllegalArgumentException(
            "User " + user.getUsername() + " is not a member of this team.");
      }
    }

    for (User user : users) {
      List<Task> tasksToUpdate = user.getAssignedTasks().stream()
          .filter(task -> task.getProject().getTeam().equals(team) && task.getStatus()
              .equals(Status.IN_PROGRESS))
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

    logger.info("removeMembersFromTeam: Removed {} members from team with ID: {}", users.size(),
        teamId);
    return convertToDto(team);
  }

  @Transactional
  public TeamDto deleteTeam(Long teamId) {
    if (teamId == null) {
      logger.error("deleteTeam: Team id cannot be null.");
      throw new IllegalArgumentException("Team id cannot be null.");
    }

    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> {
          logger.error("deleteTeam: Team not found with ID: {}", teamId);
          return new TeamNotFoundException("Team not found.");
        });

    boolean hasActiveProjects = team.getProjects().stream()
        .anyMatch(project -> !project.getStatus().equals(Status.COMPLETED) && !project.getStatus()
            .equals(Status.CANCELLED));

    if (hasActiveProjects) {
      logger.error("deleteTeam: Cannot deactivate a team with active projects. Team ID: {}",
          teamId);
      throw new IllegalStateException("Cannot deactivate a team with active projects.");
    }

    team.setActive(false);
    teamRepository.save(team);

    logger.info("deleteTeam: Team with ID: {} has been deactivated.", teamId);
    return convertToDto(team);
  }
}
