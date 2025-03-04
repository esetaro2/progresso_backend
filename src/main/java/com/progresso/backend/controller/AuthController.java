package com.progresso.backend.controller;

import com.progresso.backend.dto.UserChangePasswordDto;
import com.progresso.backend.dto.UserLoginDto;
import com.progresso.backend.dto.UserLoginResponseDto;
import com.progresso.backend.dto.UserRegistrationDto;
import com.progresso.backend.dto.UserResponseDto;
import com.progresso.backend.dto.UserUpdateDtoAdmin;
import com.progresso.backend.service.AuthService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

  private final AuthService authService;

  @Autowired
  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PreAuthorize("hasAuthority('ADMIN') or "
      + "@authService.canChangePassword(#userId, authentication.name)")
  @PutMapping("/{userId}/change-password")
  public ResponseEntity<Map<String, String>> changePassword(
      @PathVariable Long userId,
      @RequestBody UserChangePasswordDto changePasswordDto) {

    authService.changePassword(userId, changePasswordDto);

    Map<String, String> response = new HashMap<>();
    response.put("message", "Password changed successfully.");
    return ResponseEntity.ok(response);
  }

  @PostMapping("/login")
  public ResponseEntity<UserLoginResponseDto> login(@Valid @RequestBody UserLoginDto loginDto) {
    UserLoginResponseDto userDto = authService.authenticateUser(loginDto);
    return ResponseEntity.ok(userDto);
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping("/register")
  public ResponseEntity<UserResponseDto> register(
      @Valid @RequestBody UserRegistrationDto registrationDto) {
    UserResponseDto userResponseDto = authService.registerUser(registrationDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDto);
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PutMapping("/update/{userId}/admin")
  public ResponseEntity<UserResponseDto> updateUserAdmin(@PathVariable Long userId,
      @Valid @RequestBody UserUpdateDtoAdmin userUpdateDtoAdmin) {
    UserResponseDto userResponseDto = authService.updateUserAdmin(userId,
        userUpdateDtoAdmin);
    return ResponseEntity.ok(userResponseDto);
  }

  @PostMapping("/logout")
  public ResponseEntity<UserResponseDto> logout() {
    UserResponseDto logoutUser = authService.logout();
    return ResponseEntity.ok(logoutUser);
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PutMapping("/{userId}/deactivate")
  public ResponseEntity<UserResponseDto> deactivateUser(@PathVariable Long userId) {
    UserResponseDto userResponseDto = authService.deactivateUser(userId);
    return ResponseEntity.ok(userResponseDto);
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PutMapping("/{userId}/activate")
  public ResponseEntity<UserResponseDto> activateUser(@PathVariable Long userId) {
    UserResponseDto userResponseDto = authService.activateUser(userId);
    return ResponseEntity.ok(userResponseDto);
  }
}
