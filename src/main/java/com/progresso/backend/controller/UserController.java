package com.progresso.backend.controller;

import com.progresso.backend.dto.TeamMemberDto;
import com.progresso.backend.dto.UserResponseDto;
import com.progresso.backend.service.TeamMemberService;
import com.progresso.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;
  private final TeamMemberService teamMemberService;

  @Autowired
  public UserController(UserService userService, TeamMemberService teamMemberService) {
    this.userService = userService;
    this.teamMemberService = teamMemberService;
  }

  @GetMapping
  public ResponseEntity<Page<UserResponseDto>> getAllUsers(Pageable pageable) {
    Page<UserResponseDto> usersDto = userService.getAllUsers(pageable);
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

  @GetMapping("/{userId}/members")
  public ResponseEntity<Page<TeamMemberDto>> getMembersByUserIdAndStatus(
      @PathVariable("userId") Long userId,
      @RequestParam Boolean isActive,
      Pageable pageable) {
    Page<TeamMemberDto> members = teamMemberService.findMembersByUserIdAndStatus(userId, isActive,
        pageable);
    return ResponseEntity.ok(members);
  }
}
