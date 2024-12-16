package com.progresso.backend.controller;

import com.progresso.backend.dto.UserLoginDto;
import com.progresso.backend.dto.UserRegistrationDto;
import com.progresso.backend.dto.UserResponseDto;
import com.progresso.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

  private final AuthService authService;

  @Autowired
  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ResponseEntity<UserResponseDto> loginUser(@Valid @RequestBody UserLoginDto loginDto) {
    UserResponseDto userDto = authService.authenticateUser(loginDto);
    return ResponseEntity.ok(userDto);
  }

  @PreAuthorize("hasAuthority('admin')")
  @PostMapping("/register")
  public ResponseEntity<String> registerUser(
      @Valid @RequestBody UserRegistrationDto registrationDto) {
    UserResponseDto userResponseDto = authService.registerUser(registrationDto);
    if (userResponseDto != null) {
      return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDto.toString());
    } else {
      return ResponseEntity.badRequest().body("Email already registered");
    }
  }
}
