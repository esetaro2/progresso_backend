package com.progresso.backend.service;

import com.progresso.backend.dto.UserResponseDto;
import com.progresso.backend.enumeration.RoleType;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.NoDataFoundException;
import com.progresso.backend.model.Team;
import com.progresso.backend.model.User;
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

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  private UserResponseDto convertToDtoCommon(User user) {
    UserResponseDto dto = new UserResponseDto();
    dto.setId(user.getId());
    dto.setFirstName(user.getFirstName());
    dto.setLastName(user.getLastName());
    dto.setUsername(user.getUsername());
    dto.setRole(user.getRole().toString());
    dto.setManagedTeamIds(
        !CollectionUtils.isEmpty(user.getManagedTeams()) ? user.getManagedTeams().stream()
            .map(Team::getId).toList() : new ArrayList<>());
    dto.setTeamIds(
        !CollectionUtils.isEmpty(user.getTeamMemberships()) ? user.getTeamMemberships().stream()
            .map(tm -> tm.getTeam().getId()).toList() : new ArrayList<>());
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
    RoleType roleEnum = EnumUtils.getEnum(RoleType.class, role.toUpperCase());

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
}

