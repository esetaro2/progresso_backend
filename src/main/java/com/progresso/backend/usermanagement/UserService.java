package com.progresso.backend.usermanagement;

import com.progresso.backend.dto.UserLoginResponseDto;
import com.progresso.backend.dto.UserResponseDto;
import com.progresso.backend.dto.UserUpdateDtoAdmin;
import com.progresso.backend.entity.Comment;
import com.progresso.backend.entity.Project;
import com.progresso.backend.entity.Task;
import com.progresso.backend.entity.Team;
import com.progresso.backend.entity.User;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.NoDataFoundException;
import com.progresso.backend.exception.TeamNotFoundException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.teammanagement.TeamRepository;
import java.util.ArrayList;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;
  private final TeamRepository teamRepository;

  public UserService(UserRepository userRepository,
      TeamRepository teamRepository) {
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

  public UserLoginResponseDto convertToDtoToken(User user, String token) {
    UserLoginResponseDto userLoginResponseDto = new UserLoginResponseDto();
    userLoginResponseDto.setToken(token);
    userLoginResponseDto.setUserResponseDto(convertToDtoCommon(user));
    return userLoginResponseDto;
  }

  public UserResponseDto convertToDto(User user) {
    return convertToDtoCommon(user);
  }

  public UserResponseDto getUserById(Long id) {
    if (id == null) {
      logger.error("getUserById: User id cannot be null.");
      throw new IllegalArgumentException("User id cannot be null.");
    } else {
      return userRepository.findById(id)
          .map(user -> {
            logger.info("getUserById: User found with id: {}", id);
            return convertToDto(user);
          })
          .orElseThrow(() -> {
            logger.error("getUserById: User not found with id: {}", id);
            return new UserNotFoundException("User not found.");
          });
    }
  }

  public UserUpdateDtoAdmin getUserDetailsAdmin(Long userId) {
    if (userId == null) {
      logger.error("getUserDetailsAdmin: User id cannot be null.");
      throw new IllegalArgumentException("User id cannot be null.");
    }

    User currentUser = userRepository.findById(userId)
        .map(user -> {
          logger.info("getUserDetailsAdmin: User found with id: {}", userId);
          return user;
        }).orElseThrow(() -> {
          logger.error("getUserDetailsAdmin: User not found with id: {}", userId);
          return new UserNotFoundException("User not found.");
        });

    return new UserUpdateDtoAdmin(
        currentUser.getFirstName(), currentUser.getLastName(), currentUser.getPhoneNumber(),
        currentUser.getStreetAddress(), currentUser.getCity(), currentUser.getStateProvinceRegion(),
        currentUser.getCountry(), currentUser.getZipCode(), currentUser.getEmail(),
        currentUser.getRole().name()
    );
  }

  @SuppressWarnings("checkstyle:LineLength")
  public Page<UserResponseDto> getAllUsersWithFilters(Pageable pageable, String searchTerm,
      String role, Boolean active) {
    logger.info(
        "getAllUsersWithFilters: Fetching users with filters - searchTerm: {}, role: {}, active: {}",
        searchTerm, role, active);

    if (searchTerm != null) {
      searchTerm = searchTerm.trim().replaceAll("\\s+", " ");
    }

    Role roleEnum = null;
    if (role != null && !role.trim().isEmpty()) {
      roleEnum = EnumUtils.getEnum(Role.class, role.toUpperCase());
      if (roleEnum == null) {
        logger.error("getAllUsersWithFilters: Invalid role: {}.", role);
        throw new InvalidRoleException("Invalid role: " + role + ".");
      }
    }

    Page<UserResponseDto> usersDto = userRepository.findAllWithFilters(roleEnum, active, searchTerm,
        pageable).map(this::convertToDtoCommon);

    if (usersDto.isEmpty()) {
      logger.warn(
          "getAllUsers: No users found with provided filters - searchTerm: {}, role: {}, active: {} ",
          searchTerm,
          role, active);
      throw new NoDataFoundException("No users found.");
    }

    logger.info("getAllUsers: Retrieved {} users.", usersDto.getTotalElements());
    return usersDto;
  }

  public Page<UserResponseDto> getAvailableProjectManagers(Pageable pageable, String searchTerm) {
    logger.info(
        "getAvailableProjectManagers: Fetching available project managers with search term: {}",
        searchTerm);

    if (searchTerm != null) {
      searchTerm = searchTerm.trim().replaceAll("\\s+", " ");
    }

    Page<UserResponseDto> usersPage = userRepository.findAvailableProjectManagers(
        searchTerm, pageable).map(this::convertToDtoCommon);

    if (usersPage.isEmpty()) {
      logger.warn(
          "getAvailableProjectManagers: No available project managers found with search term: {}",
          searchTerm);
      throw new NoDataFoundException("No available project managers.");
    }

    logger.info("getAvailableProjectManagers: Retrieved {} available project managers.",
        usersPage.getTotalElements());

    return usersPage;
  }

  public Page<UserResponseDto> getAvailableTeamMembers(Pageable pageable, String searchTerm) {
    logger.info("getAvailableTeamMembers: Fetching available team members with searchTerm: {}",
        searchTerm);

    if (searchTerm != null) {
      searchTerm = searchTerm.trim().replaceAll("\\s+", " ");
    }

    Page<UserResponseDto> userPage = userRepository.findAvailableTeamMembers(searchTerm, pageable)
        .map(this::convertToDtoCommon);

    logger.info("getAvailableTeamMembers: Retrieved {} available team members.",
        userPage.getTotalElements());

    if (userPage.isEmpty()) {
      logger.warn("getAvailableTeamMembers: No available team members found with search term: {}",
          searchTerm);
      throw new NoDataFoundException("No available team members.");
    }

    logger.info("getAvailableTeamMembers: Retrieved {} available team members.",
        userPage.getTotalElements());

    return userPage;
  }

  public Page<UserResponseDto> getUsersByTeamId(Long teamId, Pageable pageable, String searchTerm) {
    if (teamId == null) {
      logger.error("getUsersByTeamId: Team ID cannot be null.");
      throw new IllegalArgumentException("Team ID cannot be null.");
    }

    teamRepository.findById(teamId)
        .orElseThrow(() -> {
          logger.error("getUsersByTeamId: Team not found with ID: {}", teamId);
          return new TeamNotFoundException("Team not found.");
        });

    if (searchTerm != null) {
      searchTerm = searchTerm.trim().replaceAll("\\s+", " ");
    }

    Page<UserResponseDto> userPage = userRepository.findUsersByTeamId(teamId, searchTerm, pageable)
        .map(this::convertToDtoCommon);

    logger.debug("getUsersByTeamId: Filtered users size: {}", userPage.getTotalElements());

    if (userPage.isEmpty()) {
      logger.warn("getUsersByTeamId: No users found in team with ID: {}", teamId);
      throw new NoDataFoundException("No users found in this team.");
    }

    logger.info("getUsersByTeamId: Retrieved {} users for team with ID: {}",
        userPage.getTotalElements(),
        teamId);
    return userPage;
  }
}
