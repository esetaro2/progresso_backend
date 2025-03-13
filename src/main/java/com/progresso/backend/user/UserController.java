package com.progresso.backend.user;

import com.progresso.backend.dto.UserResponseDto;
import com.progresso.backend.dto.UserUpdateDtoAdmin;
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

  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping("/{userId}/details/admin")
  public ResponseEntity<UserUpdateDtoAdmin> getUserDetailsByUserIdAdmin(@PathVariable Long userId) {
    return ResponseEntity.ok(userService.getUserDetailsAdmin(userId));
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping
  public ResponseEntity<Page<UserResponseDto>> getAllUsers(Pageable pageable,
      @RequestParam(required = false) String searchTerm,
      @RequestParam(required = false) String role,
      @RequestParam(required = false) Boolean active) {
    Page<UserResponseDto> usersDto = userService.getAllUsersWithFilters(pageable, searchTerm, role,
        active);
    return ResponseEntity.ok(usersDto);
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping("/available-project-managers")
  public ResponseEntity<Page<UserResponseDto>> getAvailablePms(Pageable pageable,
      @RequestParam(required = false) String searchTerm) {
    Page<UserResponseDto> usersDto = userService.getAvailableProjectManagers(pageable, searchTerm);
    return ResponseEntity.ok(usersDto);
  }

  @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('PROJECTMANAGER')")
  @GetMapping("/available-team-members")
  public ResponseEntity<Page<UserResponseDto>> getAvailableTeamMembers(
      @RequestParam(required = false) String searchTerm,
      Pageable pageable) {

    Page<UserResponseDto> availableMembers = userService.getAvailableTeamMembers(pageable,
        searchTerm);
    return ResponseEntity.ok(availableMembers);
  }

  @PreAuthorize("hasAuthority('ADMIN') "
      + "or hasAuthority('PROJECTMANAGER') or (hasAuthority('TEAMMEMBER')"
      + "and @teamService.isTeamMemberOfTeam(#teamId, authentication.name))")
  @GetMapping("/teams/{teamId}/team-members")
  public ResponseEntity<Page<UserResponseDto>> getUsersByTeamId(
      @PathVariable Long teamId,
      @RequestParam(required = false) String searchTerm,
      Pageable pageable) {

    Page<UserResponseDto> users = userService.getUsersByTeamId(teamId, pageable, searchTerm);

    return ResponseEntity.ok(users);
  }
}
