package com.progresso.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.progresso.backend.dto.UserResponseDto;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.exception.NoDataFoundException;
import com.progresso.backend.exception.TeamNotFoundException;
import com.progresso.backend.model.Team;
import com.progresso.backend.model.User;
import com.progresso.backend.repository.TeamRepository;
import com.progresso.backend.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @InjectMocks
  private UserService userService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private TeamRepository teamRepository;


  @Test
  void getUsersByTeamId_TeamIdIsNull_ThrowsIllegalArgumentException() {
    Pageable pageable = PageRequest.of(0, 10);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> userService.getUsersByTeamId(null, pageable, "John Doe"));
    assertEquals("Team ID cannot be null.", exception.getMessage());
  }

  @Test
  void getUsersByTeamId_TeamNotFound_ThrowsTeamNotFoundException() {
    Long teamId = 1L;
    Pageable pageable = PageRequest.of(0, 10);
    when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

    TeamNotFoundException exception = assertThrows(TeamNotFoundException.class,
        () -> userService.getUsersByTeamId(teamId, pageable, "John Doe"));
    assertEquals("Team not found.", exception.getMessage());
  }

  @Test
  void getUsersByTeamId_SearchTermNotNull_TrimsSpaces() {
    Long teamId = 1L;
    Pageable pageable = PageRequest.of(0, 10);
    String rawSearchTerm = "  John   Doe  ";
    String trimmedSearchTerm = "John Doe";

    Team team = new Team();
    team.setId(teamId);
    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

    List<User> users = new ArrayList<>();
    User user = new User();
    user.setId(1L);
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setUsername("j.doe.am1@progresso.com");
    user.setRole(Role.ADMIN);
    user.setActive(true);
    users.add(user);
    Page<User> userPageFromRepo = new PageImpl<>(users, pageable, users.size());

    when(userRepository.findUsersByTeamId(teamId, trimmedSearchTerm, pageable))
        .thenReturn(userPageFromRepo);

    Page<UserResponseDto> result = userService.getUsersByTeamId(teamId, pageable, rawSearchTerm);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.getTotalElements());
    UserResponseDto dto = result.getContent().get(0);
    assertEquals(user.getId(), dto.getId());
    assertEquals(user.getFirstName(), dto.getFirstName());
    assertEquals(user.getLastName(), dto.getLastName());
    assertEquals(user.getUsername(), dto.getUsername());
  }

  @Test
  void getUsersByTeamId_SearchTermNull_ReturnsAllUsers() {
    Long teamId = 1L;
    Pageable pageable = PageRequest.of(0, 10);

    Team team = new Team();
    team.setId(teamId);
    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

    List<User> users = new ArrayList<>();
    User user = new User();
    user.setId(1L);
    user.setFirstName("Alice");
    user.setLastName("Smith");
    user.setUsername("a.smith.tm1@progresso.com");
    user.setRole(Role.TEAMMEMBER);
    user.setActive(true);
    users.add(user);
    Page<User> userPage = new PageImpl<>(users, pageable, users.size());

    when(userRepository.findUsersByTeamId(teamId, null, pageable))
        .thenReturn(userPage);

    Page<UserResponseDto> result = userService.getUsersByTeamId(teamId, pageable, null);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.getTotalElements());
    UserResponseDto dto = result.getContent().get(0);
    assertEquals(user.getId(), dto.getId());
  }

  @Test
  void getUsersByTeamId_Pagination_ReturnsOnlyRequestedPage() {
    Long teamId = 1L;
    Pageable pageable = PageRequest.of(1,
        2);

    Team team = new Team();
    team.setId(teamId);
    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

    User user1 = new User();
    user1.setId(1L);
    user1.setFirstName("John");
    user1.setLastName("Doe");
    user1.setUsername("j.doe.am1@progresso.com");
    user1.setRole(Role.ADMIN);
    user1.setActive(true);

    User user2 = new User();
    user2.setId(2L);
    user2.setFirstName("Jane");
    user2.setLastName("Doe");
    user2.setUsername("j.doe.am2@progresso.com");
    user2.setRole(Role.ADMIN);
    user2.setActive(true);

    User user3 = new User();
    user3.setId(3L);
    user3.setFirstName("Jim");
    user3.setLastName("Beam");
    user3.setUsername("j.beam.am3@progresso.com");
    user3.setRole(Role.ADMIN);
    user3.setActive(true);

    List<User> allUsers = List.of(user1, user2, user3);
    Page<User> page = new PageImpl<>(List.of(user3), pageable, allUsers.size());

    when(userRepository.findUsersByTeamId(teamId, null, pageable)).thenReturn(page);

    Page<UserResponseDto> result = userService.getUsersByTeamId(teamId, pageable, null);

    assertNotNull(result);
    assertEquals(3, result.getTotalElements());
    assertEquals(1, result.getContent().size());
    assertEquals(user3.getId(), result.getContent().get(0).getId());
  }

  @Test
  void getUsersByTeamId_UsersFound_ReturnsPageOfUsers() {
    Long teamId = 1L;
    Pageable pageable = PageRequest.of(0, 10);
    String searchTerm = "John";

    Team team = new Team();
    team.setId(teamId);
    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

    List<User> users = new ArrayList<>();
    User user1 = new User();
    user1.setId(1L);
    user1.setFirstName("John");
    user1.setLastName("Doe");
    user1.setUsername("j.doe.am1@progresso.com");
    user1.setRole(Role.ADMIN);
    user1.setActive(true);
    users.add(user1);

    Page<User> userPage = new PageImpl<>(users, pageable, users.size());
    when(userRepository.findUsersByTeamId(teamId, searchTerm, pageable))
        .thenReturn(userPage);

    Page<UserResponseDto> result = userService.getUsersByTeamId(teamId, pageable, searchTerm);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(user1.getId(), result.getContent().get(0).getId());
  }

  @Test
  void getUsersByTeamId_SearchTermEmpty_ReturnsAllUsers() {
    Long teamId = 1L;
    Pageable pageable = PageRequest.of(0, 10);
    String searchTerm = "";

    Team team = new Team();
    team.setId(teamId);
    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

    List<User> users = new ArrayList<>();
    User user1 = new User();
    user1.setId(1L);
    user1.setFirstName("Alice");
    user1.setLastName("Smith");
    user1.setUsername("a.smith.tm1@progresso.com");
    user1.setRole(Role.TEAMMEMBER);
    user1.setActive(true);
    users.add(user1);

    User user2 = new User();
    user2.setId(2L);
    user2.setFirstName("Bob");
    user2.setLastName("Jones");
    user2.setUsername("b.jones.tm2@progresso.com");
    user2.setRole(Role.TEAMMEMBER);
    user2.setActive(true);
    users.add(user2);

    Page<User> userPage = new PageImpl<>(users, pageable, users.size());
    when(userRepository.findUsersByTeamId(teamId, searchTerm, pageable)).thenReturn(userPage);

    Page<UserResponseDto> result = userService.getUsersByTeamId(teamId, pageable, searchTerm);

    assertNotNull(result);
    assertEquals(2, result.getTotalElements());
    assertEquals(user1.getId(), result.getContent().get(0).getId());
    assertEquals(user2.getId(), result.getContent().get(1).getId());
  }

  @Test
  void getUsersByTeamId_ValidTeamWithUsers_ReturnsPaginatedUsers() {
    Long teamId = 1L;
    Pageable pageable = PageRequest.of(0, 5);

    Team team = new Team();
    team.setId(teamId);
    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

    List<User> users = new ArrayList<>();
    for (long i = 1; i <= 3; i++) {
      User u = new User();
      u.setId(i);
      u.setFirstName("User" + i);
      u.setLastName("Test");
      u.setUsername("u.teammember.tm" + i + "@progresso.com");
      u.setActive(true);
      u.setRole(Role.TEAMMEMBER);
      users.add(u);
    }

    Page<User> userPage = new PageImpl<>(users, pageable, users.size());

    when(userRepository.findUsersByTeamId(teamId, null, pageable)).thenReturn(userPage);

    Page<UserResponseDto> result = userService.getUsersByTeamId(teamId, pageable, null);

    assertNotNull(result);
    assertEquals(3, result.getTotalElements());
    assertEquals(3, result.getContent().size());

    UserResponseDto dto = result.getContent().get(0);
    assertEquals(users.get(0).getId(), dto.getId());
    assertEquals(users.get(0).getFirstName(), dto.getFirstName());
    assertEquals(users.get(0).getLastName(), dto.getLastName());
    assertEquals(users.get(0).getUsername(), dto.getUsername());

    verify(teamRepository, times(1)).findById(teamId);
    verify(userRepository, times(1)).findUsersByTeamId(teamId, null, pageable);
  }

  @Test
  void getUsersByTeamId_TeamWithNoUsers_ThrowsNoDataFoundException() {
    Long teamId = 1L;
    Pageable pageable = PageRequest.of(0, 5);

    Team team = new Team();
    team.setId(teamId);
    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

    Page<User> userPage = Page.empty();

    when(userRepository.findUsersByTeamId(teamId, null, pageable)).thenReturn(userPage);

    NoDataFoundException exception = assertThrows(NoDataFoundException.class, () -> {
      userService.getUsersByTeamId(teamId, pageable, null);
    });

    assertEquals("No users found in this team.", exception.getMessage());

    verify(teamRepository, times(1)).findById(teamId);
    verify(userRepository, times(1)).findUsersByTeamId(teamId, null, pageable);
  }
}
