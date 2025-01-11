package com.progresso.backend.service;

import com.progresso.backend.dto.LoginResponseDto;
import com.progresso.backend.dto.UserLoginDto;
import com.progresso.backend.dto.UserRegistrationDto;
import com.progresso.backend.dto.UserResponseDto;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.exception.EmailAlreadyExistsException;
import com.progresso.backend.exception.InvalidPasswordException;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.UserNotActiveException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.User;
import com.progresso.backend.repository.UserRepository;
import com.progresso.backend.security.JwtUtil;
import com.progresso.backend.security.PasswordGenerator;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final EmailService emailService;

  @Autowired
  public AuthService(UserRepository userRepository,
      UserService userService,
      PasswordEncoder passwordEncoder,
      JwtUtil jwtUtil, EmailService emailService) {
    this.userRepository = userRepository;
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
    this.emailService = emailService;
  }

  private String generateUsername(String firstName, String lastName, Role role) {
    int count = userRepository.countByRole(role);
    String roleInitials = switch (role.toString()) {
      case "ADMIN" -> "am";
      case "PROJECTMANAGER" -> "pm";
      case "TEAMMEMBER" -> "tm";
      default -> throw new InvalidRoleException("Invalid role");
    };

    return String.format("%s.%s.%s%d@progresso.com",
        StringUtils.lowerCase(firstName.substring(0, 1)), StringUtils.lowerCase(lastName),
        roleInitials, count + 1);
  }

  @Transactional
  public UserResponseDto registerUser(UserRegistrationDto userRegistrationDto) {
    if (userRepository.findByEmail(userRegistrationDto.getEmail()).isPresent()) {
      throw new EmailAlreadyExistsException("This email already exists");
    }

    User user = new User();
    user = userRegistrationDto.toEntity(user);
    user.setActive(true);
    user.setUsername(
        generateUsername(user.getFirstName(), user.getLastName(),
            user.getRole()));

    String password = PasswordGenerator.generateSecurePassword();
    System.out.println("Username:" + user.getUsername() + " Password:" + password);
    user.setPassword(passwordEncoder.encode(password));

    user = userRepository.save(user);

    String subject = "Your new Progresso Account Details";
    String message = String.format(
        "Hello %s,\n\nYour account has been created.\nUsername: %s\nPassword: %s",
        user.getFirstName(), user.getUsername(), password);
    emailService.sendMessage(user.getEmail(), subject, message);

    return userService.convertToDto(user);
  }

  public LoginResponseDto authenticateUser(UserLoginDto loginDto) {
    User user = userRepository.findByUsername(loginDto.getUsername())
        .orElseThrow(() -> new UserNotFoundException(
            "User not found with username: " + loginDto.getUsername()));

    if (!user.getActive()) {
      throw new UserNotActiveException("User " + user.getUsername() + " is not active");
    }

    if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
      throw new InvalidPasswordException(
          "Invalid password for username: " + loginDto.getUsername());
    }

    String token = jwtUtil.generateToken(user);
    return userService.convertToDtoToken(user, token);
  }
}
