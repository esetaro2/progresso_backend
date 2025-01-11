package com.progresso.backend.service;

import com.progresso.backend.dto.LoginResponseDto;
import com.progresso.backend.dto.UserResponseDto;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.exception.ActiveProjectsException;
import com.progresso.backend.exception.ActiveTasksException;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.NoDataFoundException;
import com.progresso.backend.exception.TeamNotFoundException;
import com.progresso.backend.exception.UserNotActiveException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.Comment;
import com.progresso.backend.model.Project;
import com.progresso.backend.model.Task;
import com.progresso.backend.model.Team;
import com.progresso.backend.model.User;
import com.progresso.backend.repository.TeamRepository;
import com.progresso.backend.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final TeamRepository teamRepository;

  public UserService(UserRepository userRepository, TeamRepository teamRepository) {
    this.userRepository = userRepository;
    this.teamRepository = teamRepository;
  }

  private UserResponseDto convertToDtoCommon(User user) {
    UserResponseDto dto = new UserResponseDto();
    dto.setId(user.getId());
    dto.setFirstName(user.getFirstName());
    dto.setLastName(user.getLastName());
    dto.setUsername(user.getUsername());
    dto.setRole(user.getRole().toString());
    dto.setActive(user.getActive());
    dto.setManagedProjectIds(
        !CollectionUtils.isEmpty(user.getManagedProjects()) ? user.getManagedProjects().stream()
            .map(Project::getId).toList() : new ArrayList<>());
    dto.setAssignedTaskIds(
        !CollectionUtils.isEmpty(user.getAssignedTasks()) ? user.getAssignedTasks().stream()
            .map(Task::getId).toList() : new ArrayList<>());
    dto.setTeamIds(
        !CollectionUtils.isEmpty(user.getTeams()) ? user.getTeams().stream().map(Team::getId)
            .toList() : new ArrayList<>());
    dto.setCommentIds(
        !CollectionUtils.isEmpty(user.getComments()) ? user.getComments().stream().map(
            Comment::getId).toList() : new ArrayList<>());
    return dto;
  }

  public LoginResponseDto convertToDtoToken(User user, String token) {
    LoginResponseDto loginResponseDto = new LoginResponseDto();
    loginResponseDto.setToken(token);
    loginResponseDto.setUserResponseDto(convertToDtoCommon(user));
    return loginResponseDto;
  }

  public UserResponseDto convertToDto(User user) {
    return convertToDtoCommon(user);
  }

  public Page<UserResponseDto> getAllUsers(Pageable pageable) {
    Page<UserResponseDto> usersDto = userRepository.findAllUsers(pageable)
        .map(this::convertToDtoCommon);

    if (!usersDto.hasContent()) {
      throw new NoDataFoundException("No users found");
    }

    return usersDto;
  }

  public Page<UserResponseDto> getUsersByRole(String role, Pageable pageable) {
    Role roleEnum = EnumUtils.getEnum(Role.class, role.toUpperCase());

    if (roleEnum == null) {
      throw new InvalidRoleException("Invalid role: " + role);
    }

    Page<UserResponseDto> usersDto = userRepository.findByRole(roleEnum, pageable)
        .map(this::convertToDtoCommon);

    if (!usersDto.hasContent()) {
      throw new NoDataFoundException("No users found for role: " + role);
    }

    return usersDto;
  }

  public Page<UserResponseDto> getUsersByFirstNameOrLastNameOrUserName(
      String firstName, String lastName, String username, Pageable pageable) {
    if (StringUtils.isAllEmpty(firstName, lastName, username)) {
      throw new IllegalArgumentException("At least one parameter is required");
    }

    Page<UserResponseDto> usersDto = userRepository
        .findByFirstNameContainingOrLastNameContainingOrUsernameContaining(firstName, lastName,
            username, pageable).map(this::convertToDtoCommon);

    if (!usersDto.hasContent()) {
      throw new NoDataFoundException("No users found for username " + username);
    }

    return usersDto;
  }

  public Page<UserResponseDto> getUsersByTeamId(Long teamId, Pageable pageable) {
    teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));

    Page<User> users = userRepository.findUsersByTeamId(teamId, pageable);

    if (users.isEmpty()) {
      throw new NoDataFoundException("No users found for team ID: " + teamId);
    }

    return users.map(this::convertToDto);
  }

  public Page<UserResponseDto> getUsersByProjectId(Long projectId, Pageable pageable) {
    Page<User> users = userRepository.findUsersByProjectId(projectId, pageable);

    if (users.isEmpty()) {
      throw new NoDataFoundException("No users found for project ID: " + projectId);
    }

    return users.map(this::convertToDto);
  }

  public UserResponseDto getUserFromTeam(Long teamId, Long userId) {
    teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));
    User user = userRepository.findUserInTeam(teamId, userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

    return convertToDto(user);
  }

  public UserResponseDto deactivateUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

    if (!user.getActive()) {
      throw new UserNotActiveException("User is already deactivated");
    }

    List<Project> activeProjects = user.getManagedProjects().stream().filter(
        project -> project.getStatus() != Status.COMPLETED
            && project.getStatus() != Status.CANCELLED).toList();

    if (!activeProjects.isEmpty()) {
      String activeProjectsDetails = activeProjects.stream()
          .map(project -> "ID: " + project.getId() + ", Name: " + project.getName())
          .collect(Collectors.joining("\n"));
      throw new ActiveProjectsException(
          "User is managing active projects:\n" + activeProjectsDetails
              + "\n Please reassign the project to another project manager");
    }

    List<Task> activeTasks = user.getAssignedTasks().stream()
        .filter(task -> !task.getStatus().equals(Status.COMPLETED)).toList();

    if (!activeTasks.isEmpty()) {
      String activeTasksDetails = activeTasks.stream().map(
              task -> "ID: " + task.getId() + ", Name: " + task.getName() + ", ProjectID: "
                  + task.getProject().getId() + ", ProjectName: " + task.getProject().getName())
          .collect(Collectors.joining("\n"));

      throw new ActiveTasksException("User is working on active tasks:\n" + activeTasksDetails
          + "\n Please reassign the task to another team member.");
    }

    user.setActive(false);

    User deactivatedUser = userRepository.save(user);

    return convertToDto(deactivatedUser);
  }
}

