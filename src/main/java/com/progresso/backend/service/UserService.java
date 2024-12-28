package com.progresso.backend.service;

import com.progresso.backend.dto.UserResponseDto;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.NoDataFoundException;
import com.progresso.backend.exception.TeamNotFoundException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.Project;
import com.progresso.backend.model.Task;
import com.progresso.backend.model.User;
import com.progresso.backend.repository.TeamRepository;
import com.progresso.backend.repository.UserRepository;
import java.util.ArrayList;
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
    dto.setManagedProjectIds(
        !CollectionUtils.isEmpty(user.getManagedProjects()) ? user.getManagedProjects().stream()
            .map(Project::getId).toList() : new ArrayList<>());
    dto.setAssignedTaskIds(
        !CollectionUtils.isEmpty(user.getAssignedTasks()) ? user.getAssignedTasks().stream()
            .map(Task::getId).toList() : new ArrayList<>());
    dto.setTeamId(user.getTeam().getId());
    return dto;
  }

  public UserResponseDto convertToDtoToken(User user, String token) {
    UserResponseDto dto = convertToDtoCommon(user);
    dto.setToken(token);
    return dto;
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

    Page<User> users = userRepository.findByTeamId(teamId, pageable);

    if (users.isEmpty()) {
      throw new NoDataFoundException("No users found for team ID: " + teamId);
    }

    return users.map(this::convertToDto);
  }

  public UserResponseDto getUserByTeamAndId(Long teamId, Long userId) {
    teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with ID: " + teamId));

    User user = userRepository.findByTeamIdAndId(teamId, userId)
        .orElseThrow(() -> new UserNotFoundException(
            "User with ID " + userId + " not found in team " + teamId));

    if (!user.getRole().equals(Role.TEAMMEMBER)) {
      throw new InvalidRoleException("User is not a team member: " + user.getUsername());
    }

    if (!user.getTeam().getId().equals(teamId)) {
      throw new UserNotFoundException(
          "User with ID " + userId + " does not belong to team " + teamId);
    }

    return convertToDto(user);
  }

  public Page<UserResponseDto> getUsersByProjectId(Long projectId, Pageable pageable) {
    Page<User> users = userRepository.findUsersByProjectId(projectId, pageable);

    if (users.isEmpty()) {
      throw new NoDataFoundException("No users found for project ID: " + projectId);
    }

    return users.map(this::convertToDto);
  }
}

