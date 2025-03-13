package com.progresso.backend.team;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.progresso.backend.dto.TeamDto;
import com.progresso.backend.entity.Team;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {

  @InjectMocks
  private TeamService teamService;

  @Mock
  private TeamRepository teamRepository;

  @Test
  void createTeam_ValidName() {
    String teamName = "Unique Team";
    when(teamRepository.existsByNameIgnoreCase(teamName)).thenReturn(false);

    Team team = new Team();
    team.setId(1L);
    team.setName(teamName);
    team.setActive(true);

    when(teamRepository.save(any(Team.class))).thenReturn(team);

    TeamDto result = teamService.createTeam(teamName);

    assertNotNull(result);
    assertEquals(team.getId(), result.getId());
    assertEquals(team.getName(), result.getName());
    assertEquals(team.getActive(), result.getActive());

    verify(teamRepository, times(1)).save(any(Team.class));
    verify(teamRepository, times(1)).existsByNameIgnoreCase(teamName);
  }

  @Test
  void createTeam_DuplicateName() {
    String teamName = "Duplicate Team";
    String newTeamName = "Duplicate Team (1)";

    when(teamRepository.existsByNameIgnoreCase(teamName)).thenReturn(true);
    when(teamRepository.existsByNameIgnoreCase(newTeamName)).thenReturn(false);

    Team team = new Team();
    team.setId(1L);
    team.setName(newTeamName);
    team.setActive(true);

    when(teamRepository.save(any(Team.class))).thenReturn(team);

    TeamDto result = teamService.createTeam(teamName);

    assertNotNull(result);
    assertEquals(team.getId(), result.getId());
    assertEquals(team.getName(), result.getName());
    assertEquals(team.getActive(), result.getActive());

    verify(teamRepository, times(1)).save(any(Team.class));
    verify(teamRepository, times(1)).existsByNameIgnoreCase(teamName);
    verify(teamRepository, times(1)).existsByNameIgnoreCase(newTeamName);
  }

  @Test
  void createTeam_NameExceeds100Characters() {
    String longTeamName = "A".repeat(101);

    when(teamRepository.existsByNameIgnoreCase(longTeamName)).thenReturn(true);

    String expectedName = "A".repeat(96) + " (1)";

    when(teamRepository.existsByNameIgnoreCase(expectedName)).thenReturn(false);

    Team savedTeam = new Team();
    savedTeam.setId(1L);
    savedTeam.setName(expectedName);
    savedTeam.setActive(true);
    when(teamRepository.save(any(Team.class))).thenReturn(savedTeam);

    TeamDto createdTeam = teamService.createTeam(longTeamName);

    assertNotNull(createdTeam);
    assertEquals(expectedName, createdTeam.getName());

    verify(teamRepository, times(1)).save(any(Team.class));
  }

  @Test
  void createTeam_NameIsNull_ThrowsIllegalArgumentException() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> teamService.createTeam(null));
    assertEquals("Team name cannot be null or empty.", exception.getMessage());
  }

  @Test
  void createTeam_NameIsEmpty_ThrowsIllegalArgumentException() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> teamService.createTeam(""));
    assertEquals("Team name cannot be null or empty.", exception.getMessage());
  }

}
