package com.progresso.backend.service;

import com.progresso.backend.dto.UserLoginDto;
import com.progresso.backend.dto.UserRegistrationDto;
import com.progresso.backend.dto.UserResponseDto;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.Role;
import com.progresso.backend.model.User;
import com.progresso.backend.repository.RoleRepository;
import com.progresso.backend.repository.UserRepository;
import com.progresso.backend.security.JwtUtil;
import com.progresso.backend.security.PasswordGenerator;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final EmailService emailService;

  @Autowired
  public AuthService(UserRepository userRepository, RoleRepository roleRepository,
      UserService userService,
      PasswordEncoder passwordEncoder,
      JwtUtil jwtUtil, EmailService emailService) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
    this.emailService = emailService;
  }

  private String generateUsername(String firstName, String lastName, String roleName) {
    int count = userRepository.countByRole(roleName);
    return String.format("%s.%s.%s.%d@progresso.com", firstName.toLowerCase(),
        lastName.toLowerCase(), roleName, count + 1);
  }

  public User saveUser(User user) {
    return userRepository.save(user);
  }

  public UserResponseDto registerUser(UserRegistrationDto userRegistrationDto) {
    if (userRepository.findByEmail(userRegistrationDto.getEmail()).isPresent()) {
      return null;
    }

    Role role = roleRepository.findByName(userRegistrationDto.getRole());
    if (role == null) {
      throw new RuntimeException("Role not found");
    }

    User user = new User();
    user.setFirstName(userRegistrationDto.getFirstName());
    user.setLastName(userRegistrationDto.getLastName());
    user.setBirthDate(userRegistrationDto.getBirthDate());
    user.setPhoneNumber(userRegistrationDto.getPhoneNumber());
    user.setStreetAddress(userRegistrationDto.getStreetAddress());
    user.setCity(userRegistrationDto.getCity());
    user.setStateProvinceRegion(userRegistrationDto.getStateProvinceRegion());
    user.setCountry(userRegistrationDto.getCountry());
    user.setZipCode(userRegistrationDto.getZipCode());
    user.setEmail(userRegistrationDto.getEmail());
    user.setUsername(
        generateUsername(userRegistrationDto.getFirstName(), userRegistrationDto.getLastName(),
            userRegistrationDto.getRole()));
    user.setRole(role);

    String password = PasswordGenerator.generateSecurePassword();
    System.out.println("Username:" + user.getUsername() + " Password:" + password);
    user.setPassword(passwordEncoder.encode(password));

    User savedUser = saveUser(user);

    String subject = "Your new Progresso Account Details";
    String message = String.format(
        "Hello %s,\n\nYour account has been created.\nUsername: %s\nPassword: %s",
        user.getFirstName(), user.getUsername(), password);
    emailService.sendMessage(user.getEmail(), subject, message);

    return userService.convertToDto(savedUser);
  }

  public UserResponseDto authenticateUser(UserLoginDto loginDto) {
    Optional<User> userOptional = userRepository.findByUsername(loginDto.getUsername());
    if (userOptional.isPresent()) {
      User user = userOptional.get();
      if (passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
        String token = jwtUtil.generateToken(user);
        return userService.convertToDtoToken(user, token);
      } else {
        throw new UserNotFoundException("Invalid password for username: " + loginDto.getUsername());
      }
    } else {
      throw new UserNotFoundException("User not found with username: " + loginDto.getUsername());
    }
  }
}
