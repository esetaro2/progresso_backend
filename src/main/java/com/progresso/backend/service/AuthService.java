package com.progresso.backend.service;

import com.progresso.backend.dto.UserLoginDto;
import com.progresso.backend.dto.UserRegistrationDto;
import com.progresso.backend.dto.UserResponseDto;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.exception.EmailAlreadyExistsException;
import com.progresso.backend.exception.InvalidPasswordException;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.User;
import com.progresso.backend.repository.UserRepository;
import com.progresso.backend.security.JwtUtil;
import com.progresso.backend.security.PasswordGenerator;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.EnumUtils;
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
    user.setUsername(
        generateUsername(user.getFirstName(), user.getLastName(),
            user.getRole()));

    String roleString = userRegistrationDto.getRole().toUpperCase();
    if (!EnumUtils.isValidEnum(Role.class, roleString)) {
      throw new InvalidRoleException("Invalid role: " + roleString);
    }

    Role role = EnumUtils.getEnum(Role.class, roleString);
    user.setRole(role);

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

  public UserResponseDto authenticateUser(UserLoginDto loginDto) {
    return userRepository.findByUsername(loginDto.getUsername())
        .map(user -> {
          if (passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            String token = jwtUtil.generateToken(user);
            return userService.convertToDtoToken(user, token);
          } else {
            throw new InvalidPasswordException(
                "Invalid password for username: " + loginDto.getUsername());
          }
        })
        .orElseThrow(() -> new UserNotFoundException(
            "User not found with username: " + loginDto.getUsername()));
  }
}
