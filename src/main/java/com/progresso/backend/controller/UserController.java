package com.progresso.backend.controller;

import com.progresso.backend.dto.UserResponseDto;
import com.progresso.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long userId) {
    return ResponseEntity.ok(userService.getUserById(userId));
  }

  @GetMapping
  public ResponseEntity<Page<UserResponseDto>> getAllUsers(Pageable pageable) {
    Page<UserResponseDto> usersDto = userService.getAllUsers(pageable);
    return ResponseEntity.ok(usersDto);
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping("/availablePms")
  public ResponseEntity<Page<UserResponseDto>> getAvailablePms(Pageable pageable,
      @RequestParam(required = false) String searchTerm) {
    Page<UserResponseDto> usersDto = userService.getAvailableProjectManagers(pageable, searchTerm);
    return ResponseEntity.ok(usersDto);
  }

  @GetMapping("/role/{roleName}")
  public ResponseEntity<Page<UserResponseDto>> getUsersByRole(
      @PathVariable String roleName,
      Pageable pageable) {
    Page<UserResponseDto> usersDto = userService.getUsersByRole(roleName, pageable);
    return ResponseEntity.ok(usersDto);
  }

  @GetMapping("/search")
  public ResponseEntity<Page<UserResponseDto>> searchUsers(
      @RequestParam(required = false) String firstName,
      @RequestParam(required = false) String lastName,
      @RequestParam(required = false) String username,
      Pageable pageable) {
    Page<UserResponseDto> usersDto = userService.getUsersByFirstNameOrLastNameOrUserName(
        firstName, lastName, username, pageable);
    return ResponseEntity.ok(usersDto);
  }

  @GetMapping("/teams/{teamId}/users")
  public ResponseEntity<Page<UserResponseDto>> getUsersByTeamId(
      @PathVariable Long teamId,
      Pageable pageable) {

    Page<UserResponseDto> users = userService.getUsersByTeamId(teamId, pageable);

    return ResponseEntity.ok(users);
  }

  @GetMapping("/teams/{teamId}/user/{userId}")
  public ResponseEntity<UserResponseDto> getUserFromTeam(@PathVariable Long teamId,
      @PathVariable Long userId) {
    UserResponseDto userResponseDto = userService.getUserFromTeam(teamId, userId);
    return ResponseEntity.ok(userResponseDto);
  }

  @GetMapping("/projects/{projectId}/users")
  public ResponseEntity<Page<UserResponseDto>> getUsersByProjectId(
      @PathVariable Long projectId,
      Pageable pageable) {

    Page<UserResponseDto> users = userService.getUsersByProjectId(projectId, pageable);

    return ResponseEntity.ok(users);
  }

  @GetMapping("/active")
  public ResponseEntity<Page<UserResponseDto>> findActiveUsers(Pageable pageable) {
    Page<UserResponseDto> activeUsers = userService.findByActiveTrue(pageable);
    return ResponseEntity.ok(activeUsers);
  }
}
