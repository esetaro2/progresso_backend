package com.progresso.backend.service;

import com.progresso.backend.dto.UserLoginDto;
import com.progresso.backend.dto.UserRegistrationDto;
import com.progresso.backend.dto.UserResponseDto;
import com.progresso.backend.enumeration.RoleType;
import com.progresso.backend.exception.EmailAlreadyExistsException;
import com.progresso.backend.exception.InvalidPasswordException;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.User;
import com.progresso.backend.repository.UserRepository;
import com.progresso.backend.security.JwtUtil;
import com.progresso.backend.security.PasswordGenerator;
import jakarta.transaction.Transactional;
import java.util.Optional;
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

  private String generateUsername(String firstName, String lastName, RoleType role) {
    int count = userRepository.countByRole(role);
    String roleInitials = switch (role.toString()) {
      case "ADMIN" -> "am";
      case "PROJECTMANAGER" -> "pm";
      case "TEAMMEMBER" -> "tm";
      default -> throw new InvalidRoleException("Invalid role");
    };

    return String.format("%s.%s.%s%d@progresso.com", firstName.toLowerCase().charAt(0),
        lastName.toLowerCase(), roleInitials, count + 1);
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

    RoleType role = RoleType.valueOf(userRegistrationDto.getRole().toUpperCase());
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
    Optional<User> userOptional = userRepository.findByUsername(loginDto.getUsername());
    if (userOptional.isPresent()) {
      User user = userOptional.get();
      if (passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
        String token = jwtUtil.generateToken(user);
        return userService.convertToDtoToken(user, token);
      } else {
        throw new InvalidPasswordException(
            "Invalid password for username: " + loginDto.getUsername());
      }
    } else {
      throw new UserNotFoundException("User not found with username: " + loginDto.getUsername());
    }
  }
}
