package com.progresso.backend.service;

import com.progresso.backend.dto.UserLoginResponseDto;
import com.progresso.backend.dto.UserResponseDto;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.NoDataFoundException;
import com.progresso.backend.exception.ProjectNotFoundException;
import com.progresso.backend.exception.TeamNotFoundException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.Comment;
import com.progresso.backend.model.Project;
import com.progresso.backend.model.Task;
import com.progresso.backend.model.Team;
import com.progresso.backend.model.User;
import com.progresso.backend.repository.ProjectRepository;
import com.progresso.backend.repository.TeamRepository;
import com.progresso.backend.repository.UserRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;
  private final TeamRepository teamRepository;
  private final ProjectRepository projectRepository;

  public UserService(UserRepository userRepository, TeamRepository teamRepository,
      ProjectRepository projectRepository) {
    this.userRepository = userRepository;
    this.teamRepository = teamRepository;
    this.projectRepository = projectRepository;
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

  public Page<UserResponseDto> getAllUsers(Pageable pageable) {
    Page<UserResponseDto> usersDto = userRepository.findAllUsers(pageable)
        .map(this::convertToDtoCommon);

    if (usersDto.isEmpty()) {
      logger.warn("getAllUsers: No users found.");
      throw new NoDataFoundException("No users found.");
    }

    logger.info("getAllUsers: Retrieved {} users.", usersDto.getTotalElements());
    return usersDto;
  }

  public Page<UserResponseDto> getAvailableProjectManagers(Pageable pageable, String searchTerm) {
    logger.info(
        "getAvailableProjectManagers: Fetching available project managers with search term: {}",
        searchTerm);

    Page<User> page = userRepository.findByRoleAndActiveTrue(Role.PROJECTMANAGER,
        Pageable.unpaged());

    List<User> filteredUsers = page.getContent().stream()
        .filter(pm -> {
          if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String[] searchTerms = searchTerm.toLowerCase().trim().split("\\s+");
            String combinedFields = (pm.getFirstName() + " " + pm.getLastName() + " "
                + pm.getUsername()).toLowerCase();
            return Arrays.stream(searchTerms).allMatch(combinedFields::contains);
          } else {
            return true;
          }
        })
        .filter(pm -> {
          long activeProjects = projectRepository.countByProjectManagerAndStatusNotIn(
              pm, List.of(Status.CANCELLED, Status.COMPLETED));
          return activeProjects < 5;
        })
        .toList();

    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), filteredUsers.size());
    List<UserResponseDto> paginatedList;

    if (start <= end) {
      paginatedList = filteredUsers.subList(start, end).stream()
          .map(this::convertToDtoCommon)
          .toList();
    } else {
      paginatedList = new ArrayList<>();
    }

    if (paginatedList.isEmpty()) {
      logger.warn(
          "getAvailableProjectManagers: No available project managers found with search term: {}",
          searchTerm);
      throw new NoDataFoundException("No available project managers.");
    }

    logger.info("getAvailableProjectManagers: Retrieved {} available project managers.",
        paginatedList.size());
    return new PageImpl<>(paginatedList, pageable, filteredUsers.size());
  }

  public Page<UserResponseDto> getAvailableTeamMembers(Pageable pageable, String searchTerm) {
    logger.info("getAvailableTeamMembers: Fetching available team members with searchTerm: {}",
        searchTerm);

    Page<User> page = userRepository.findByRoleAndActiveTrue(Role.TEAMMEMBER, Pageable.unpaged());

    List<User> filteredUsers = page.getContent().stream()
        .filter(user -> {
          if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String[] searchTerms = searchTerm.toLowerCase().trim().split("\\s+");
            String combinedFields = (user.getFirstName() + " " + user.getLastName() + " "
                + user.getUsername())
                .toLowerCase();
            return Arrays.stream(searchTerms).allMatch(combinedFields::contains);
          }
          return true;
        })
        .filter(user -> user.getTeams().stream()
            .noneMatch(Team::getActive))
        .toList();

    logger.debug("getAvailableTeamMembers: Filtered users size: {}", filteredUsers.size());

    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), filteredUsers.size());

    List<UserResponseDto> paginatedList;
    if (start <= end) {
      paginatedList = filteredUsers.subList(start, end).stream()
          .map(this::convertToDtoCommon)
          .toList();
    } else {
      paginatedList = new ArrayList<>();
    }

    if (paginatedList.isEmpty()) {
      logger.warn("getAvailableTeamMembers: No available team members found.");
      throw new NoDataFoundException("No available team members.");
    }

    logger.info("getAvailableTeamMembers: Retrieved {} available team members.",
        paginatedList.size());
    return new PageImpl<>(paginatedList, pageable, filteredUsers.size());
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

    Page<User> page = userRepository.findUsersByTeamId(teamId, Pageable.unpaged());

    List<User> filteredUsers = page.getContent().stream()
        .filter(user -> {
          if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String[] searchTerms = searchTerm.toLowerCase().trim().split("\\s+");
            String combinedFields = (user.getFirstName() + " " + user.getLastName() + " "
                + user.getUsername())
                .toLowerCase();
            return Arrays.stream(searchTerms).allMatch(combinedFields::contains);
          }
          return true;
        })
        .toList();

    logger.debug("getUsersByTeamId: Filtered users size: {}", filteredUsers.size());

    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), filteredUsers.size());

    List<UserResponseDto> paginatedList;
    if (start <= end) {
      paginatedList = filteredUsers.subList(start, end).stream()
          .map(this::convertToDto)
          .toList();
    } else {
      paginatedList = new ArrayList<>();
    }

    if (paginatedList.isEmpty()) {
      logger.warn("getUsersByTeamId: No users found in team with ID: {}", teamId);
      throw new NoDataFoundException("No users found in this team.");
    }

    logger.info("getUsersByTeamId: Retrieved {} users for team with ID: {}", paginatedList.size(),
        teamId);
    return new PageImpl<>(paginatedList, pageable, filteredUsers.size());
  }

  public Page<UserResponseDto> getUsersByRole(String role, Pageable pageable) {
    if (role == null) {
      logger.error("getUsersByRole: Role cannot be null.");
      throw new IllegalArgumentException("Role cannot be null.");
    }

    Role roleEnum = EnumUtils.getEnum(Role.class, role.toUpperCase());

    if (roleEnum == null) {
      logger.error("getUsersByRole: Invalid role: {}.", role);
      throw new InvalidRoleException("Invalid role: " + role + ".");
    }

    Page<UserResponseDto> usersDto = userRepository.findByRoleAndActiveTrue(roleEnum, pageable)
        .map(this::convertToDtoCommon);

    if (!usersDto.hasContent()) {
      logger.warn("getUsersByRole: No users found for role: {}.", role);
      throw new NoDataFoundException("No users found for role: " + role + ".");
    }

    logger.info("getUsersByRole: Retrieved {} users for role: {}.", usersDto.getTotalElements(),
        role);
    return usersDto;
  }

  @SuppressWarnings("checkstyle:LineLength")
  public Page<UserResponseDto> getUsersByFirstNameOrLastNameOrUserName(
      String firstName, String lastName, String username, Pageable pageable) {
    if (StringUtils.isAllEmpty(firstName, lastName, username)) {
      logger.error(
          "getUsersByFirstNameOrLastNameOrUserName: At least one of the parameters (firstName, lastName, username) is required.");
      throw new IllegalArgumentException(
          "At least one of the parameters (firstName, lastName, username) is required.");
    }

    Page<UserResponseDto> usersDto = userRepository
        .findByFirstNameContainingOrLastNameContainingOrUsernameContainingAndActiveTrue(
            firstName, lastName, username, pageable)
        .map(this::convertToDtoCommon);

    if (usersDto.isEmpty()) {
      logger.warn(
          "getUsersByFirstNameOrLastNameOrUserName: No users found for the given search criteria: firstName={}, lastName={}, username={}",
          firstName, lastName, username);
      throw new NoDataFoundException("No users found for the given search criteria.");
    }

    logger.info(
        "getUsersByFirstNameOrLastNameOrUserName: Retrieved {} users for the given search criteria: firstName={}, lastName={}, username={}",
        usersDto.getTotalElements(), firstName, lastName, username);
    return usersDto;
  }

  public Page<UserResponseDto> getUsersByProjectId(Long projectId, Pageable pageable) {
    if (projectId == null) {
      logger.error("getUsersByProjectId: Project ID cannot be null.");
      throw new IllegalArgumentException("Project ID cannot be null.");
    }

    projectRepository.findById(projectId)
        .orElseThrow(() -> {
          logger.error("getUsersByProjectId: Project not found with ID: {}", projectId);
          return new ProjectNotFoundException("Project not found.");
        });

    Page<User> users = userRepository.findUsersByProjectId(projectId, pageable);

    if (users.isEmpty()) {
      logger.warn("getUsersByProjectId: No users found for this project with ID: {}", projectId);
      throw new NoDataFoundException("No users found for this project.");
    }

    logger.info("getUsersByProjectId: Retrieved {} users for project with ID: {}",
        users.getTotalElements(), projectId);
    return users.map(this::convertToDto);
  }

  public UserResponseDto getUserFromTeam(Long teamId, Long userId) {
    if (teamId == null) {
      logger.error("getUserFromTeam: Team ID cannot be null.");
      throw new IllegalArgumentException("Team ID cannot be null.");
    }

    if (userId == null) {
      logger.error("getUserFromTeam: User ID cannot be null.");
      throw new IllegalArgumentException("User ID cannot be null.");
    }

    teamRepository.findById(teamId)
        .orElseThrow(() -> {
          logger.error("getUserFromTeam: Team not found with ID: {}", teamId);
          return new TeamNotFoundException("Team not found.");
        });

    User user = userRepository.findUserInTeam(teamId, userId)
        .orElseThrow(() -> {
          logger.error("getUserFromTeam: User not found in team with ID: {}", userId);
          return new UserNotFoundException("This user is not found in this team.");
        });

    logger.info("getUserFromTeam: User found with ID: {} in team with ID: {}", userId, teamId);
    return convertToDto(user);
  }

  public Page<UserResponseDto> findByActiveTrue(Pageable pageable) {
    Page<User> users = userRepository.findByActiveTrue(pageable);

    if (users.isEmpty()) {
      logger.warn("findByActiveTrue: No active users found.");
      throw new NoDataFoundException("No active users found.");
    }

    logger.info("findByActiveTrue: Retrieved {} active users.", users.getTotalElements());
    return users.map(this::convertToDto);
  }
}
