package com.progresso.backend.auth;

import com.progresso.backend.dto.UserChangePasswordDto;
import com.progresso.backend.dto.UserLoginDto;
import com.progresso.backend.dto.UserLoginResponseDto;
import com.progresso.backend.dto.UserRegistrationDto;
import com.progresso.backend.dto.UserResponseDto;
import com.progresso.backend.dto.UserUpdateDtoAdmin;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.exception.ActiveProjectsException;
import com.progresso.backend.exception.EmailAlreadyExistsException;
import com.progresso.backend.exception.InvalidPasswordException;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.UserNotActiveException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.Project;
import com.progresso.backend.model.Task;
import com.progresso.backend.model.User;
import com.progresso.backend.user.UserService;
import com.progresso.backend.task.TaskRepository;
import com.progresso.backend.user.UserRepository;
import com.progresso.backend.security.JwtUtil;
import com.progresso.backend.security.PasswordGenerator;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository userRepository;
  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final TaskRepository taskRepository;

  @Autowired
  public AuthService(UserRepository userRepository,
      UserService userService,
      PasswordEncoder passwordEncoder,
      JwtUtil jwtUtil, TaskRepository taskRepository) {
    this.userRepository = userRepository;
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
    this.taskRepository = taskRepository;
  }

  private String generateUsername(String firstName, String lastName, Role role) {
    String sanitizedFirstName = firstName.replace(" ", "_");
    String sanitizedLastName = lastName.replace(" ", "_");

    int count = userRepository.countByRole(role);
    String roleInitials = switch (role.toString()) {
      case "ADMIN" -> "am";
      case "PROJECTMANAGER" -> "pm";
      case "TEAMMEMBER" -> "tm";
      default -> {
        logger.error("generateUsername: Invalid role: {}", role);
        throw new InvalidRoleException("Invalid role.");
      }
    };

    String username = String.format("%s.%s.%s%d@progresso.com",
        StringUtils.lowerCase(sanitizedFirstName.substring(0, 1)),
        StringUtils.lowerCase(sanitizedLastName),
        roleInitials, count + 1);

    logger.info("generateUsername: Generated username: {}", username);
    return username;
  }

  private Integer incrementTokenVersion(Integer version) {
    if (version == Integer.MAX_VALUE) {
      logger.warn("incrementTokenVersion: Version reached Integer.MAX_VALUE, resetting to 0.");
      return 0;
    } else {
      Integer newVersion = version + 1;
      logger.info("incrementTokenVersion: Incremented version from {} to {}", version, newVersion);
      return newVersion;
    }
  }

  @Transactional
  public UserResponseDto registerUser(UserRegistrationDto userRegistrationDto) {
    if (userRepository.findByEmail(userRegistrationDto.getEmail()).isPresent()) {
      logger.error("registerUser: This email already exists: {}", userRegistrationDto.getEmail());
      throw new EmailAlreadyExistsException("This email already exists.");
    }

    User user = new User();
    user = userRegistrationDto.toEntity(user);
    user.setActive(true);
    user.setUsername(generateUsername(user.getFirstName(), user.getLastName(), user.getRole()));

    String password = PasswordGenerator.generateSecurePassword();
    logger.info("registerUser: Generated username: {} and password: {} for user: {}",
        user.getUsername(), password, user.getFirstName());
    user.setPassword(passwordEncoder.encode(password));

    user = userRepository.save(user);

    logger.info("registerUser: Registered new user with email: {}", user.getEmail());
    return userService.convertToDto(user);
  }

  public boolean canChangePassword(Long userId, String username) {
    User currentUser = userRepository.findByUsername(username)
        .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

    return currentUser.getId().equals(userId);
  }

  @Transactional
  public void changePassword(Long userId, UserChangePasswordDto changePasswordDto) {
    if (userId == null) {
      logger.error("changePassword: User id cannot be null.");
      throw new UserNotFoundException("User id cannot be null.");
    }

    User currentUser = userRepository.findById(userId)
        .orElseThrow(() -> {
          logger.error("changePassword: User not found with id: {}", userId);
          return new UserNotFoundException("User not found.");
        });

    if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(),
        currentUser.getPassword())) {
      logger.error("changePassword: Current password does not match for user with id: {}", userId);
      throw new InvalidPasswordException("Current password is incorrect.");
    }

    if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword())) {
      logger.error(
          "changePassword: New password and confirmation do not match for user with id: {}",
          userId);
      throw new InvalidPasswordException("New password and confirmation do not match.");
    }

    currentUser.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
    userRepository.save(currentUser);

    logger.info("changePassword: Password updated successfully for user with id: {}", userId);
  }

  @Transactional
  public UserResponseDto updateUserAdmin(Long userId, UserUpdateDtoAdmin userUpdateDtoAdmin) {
    if (userId == null) {
      logger.error("updateUserAdmin: User id cannot be null.");
      throw new UserNotFoundException("User id cannot be null.");
    }

    User currentUser = userRepository.findById(userId)
        .orElseThrow(() -> {
          logger.error("updateUserAdmin: User not found with id: {}", userId);
          return new UserNotFoundException("User not found.");
        });

    boolean anyInfoChanged =
        !currentUser.getFirstName().trim()
            .equalsIgnoreCase(userUpdateDtoAdmin.getFirstName().trim())
            || !currentUser.getLastName().trim()
            .equalsIgnoreCase(userUpdateDtoAdmin.getLastName().trim())
            || !currentUser.getRole().name().trim()
            .equalsIgnoreCase(userUpdateDtoAdmin.getRole().trim());

    Optional<User> emailOwner = userRepository.findByEmail(userUpdateDtoAdmin.getEmail());
    if (emailOwner.isPresent() && !emailOwner.get().getId().equals(userId)) {
      logger.error("updateUserAdmin: This email already exists: {}", userUpdateDtoAdmin.getEmail());
      throw new EmailAlreadyExistsException("This email already exists.");
    }

    User updatedUser = userUpdateDtoAdmin.toEntity(currentUser);

    if (anyInfoChanged) {
      String newUsername = generateUsername(updatedUser.getFirstName(), updatedUser.getLastName(),
          updatedUser.getRole());
      logger.info("updateUserAdmin: Username changed from {} to {}", currentUser.getUsername(),
          newUsername);
      updatedUser.setUsername(newUsername);
    }

    User savedUser = userRepository.save(updatedUser);
    logger.info("updateUserAdmin: User updated successfully with id: {}", savedUser.getId());

    return userService.convertToDto(savedUser);
  }

  public UserLoginResponseDto authenticateUser(UserLoginDto loginDto) {
    User user = userRepository.findByUsername(loginDto.getUsername())
        .orElseThrow(() -> {
          logger.error("authenticateUser: User not found with username: {}",
              loginDto.getUsername());
          return new UserNotFoundException(
              "User not found with username: " + loginDto.getUsername());
        });

    if (!user.getActive()) {
      logger.error("authenticateUser: User {} is not active.", user.getUsername());
      throw new UserNotActiveException("User " + user.getUsername() + " is not active.");
    }

    if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
      logger.error("authenticateUser: Invalid password for username: {}", loginDto.getUsername());
      throw new InvalidPasswordException(
          "Invalid password for username: " + loginDto.getUsername());
    }

    String token = jwtUtil.generateToken(user);
    logger.info("authenticateUser: User {} authenticated successfully.", user.getUsername());
    return userService.convertToDtoToken(user, token);
  }

  @Transactional
  public UserResponseDto logout() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();

    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> {
          logger.error("logout: User not found with username: {}", username);
          return new UserNotFoundException("User not found with username: " + username);
        });

    Integer version = incrementTokenVersion(user.getTokenVersion());
    user.setTokenVersion(version);

    User logoutUser = userRepository.save(user);

    logger.info("logout: User {} has logged out successfully.", username);
    return userService.convertToDto(logoutUser);
  }

  @Transactional
  public UserResponseDto deactivateUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          logger.error("deactivateUser: User not found with ID: {}", userId);
          return new UserNotFoundException("User not found.");
        });

    if (!user.getActive()) {
      logger.error("deactivateUser: User {} is already deactivated.", user.getUsername());
      throw new UserNotActiveException("User is already deactivated.");
    }

    Integer version = incrementTokenVersion(user.getTokenVersion());
    user.setTokenVersion(version);

    List<Project> activeProjects = user.getManagedProjects().stream()
        .filter(project -> project.getStatus() != Status.COMPLETED
            && project.getStatus() != Status.CANCELLED)
        .toList();

    if (!activeProjects.isEmpty()) {
      String activeProjectsDetails = activeProjects.stream()
          .map(project -> "ID: " + project.getId() + ", Name: " + project.getName())
          .collect(Collectors.joining("\n"));

      logger.error("deactivateUser: User {} is managing active projects:\n{}", user.getUsername(),
          activeProjectsDetails);
      throw new ActiveProjectsException(
          "User is managing active projects:\n" + activeProjectsDetails
              + ".\n Please reassign projects to another project manager.");
    }

    List<Task> activeTasks = user.getAssignedTasks().stream()
        .filter(task -> task.getStatus().equals(Status.IN_PROGRESS))
        .peek(task -> task.setAssignedUser(null)).toList();

    taskRepository.saveAll(activeTasks);

    user.getAssignedTasks().removeAll(activeTasks);
    user.setActive(false);

    User deactivatedUser = userRepository.save(user);
    logger.info("deactivateUser: User {} has been deactivated successfully.", user.getUsername());

    return userService.convertToDto(deactivatedUser);
  }

  @Transactional
  public UserResponseDto activateUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          logger.error("activateUser: User not found with ID: {}", userId);
          return new UserNotFoundException("User not found.");
        });

    if (user.getActive()) {
      logger.error("activateUser: User {} is already active.", user.getUsername());
      throw new UserNotActiveException("User is already active.");
    }

    user.setActive(true);
    User activatedUser = userRepository.save(user);

    logger.info("activateUser: User {} has been activated successfully.", user.getUsername());
    return userService.convertToDto(activatedUser);
  }
}
