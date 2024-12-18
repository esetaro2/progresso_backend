package com.progresso.backend.service;

import com.progresso.backend.dto.MembersDto;
import com.progresso.backend.dto.TeamDto;
import com.progresso.backend.enumeration.RoleType;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.NoDataFoundException;
import com.progresso.backend.exception.TeamMemberAlreadyActiveException;
import com.progresso.backend.exception.TeamNameAlreadyExistsException;
import com.progresso.backend.exception.TeamNotFoundException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.Team;
import com.progresso.backend.model.TeamMember;
import com.progresso.backend.model.User;
import com.progresso.backend.repository.TeamMemberRepository;
import com.progresso.backend.repository.TeamRepository;
import com.progresso.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeamService {

  private final TeamRepository teamRepository;
  private final TeamMemberRepository teamMemberRepository;
  private final UserRepository userRepository;

  @Autowired
  public TeamService(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository,
      UserRepository userRepository) {
    this.teamRepository = teamRepository;
    this.teamMemberRepository = teamMemberRepository;
    this.userRepository = userRepository;
  }

  private TeamDto convertToDto(Team team) {
    TeamDto teamDto = new TeamDto();
    teamDto.setName(team.getName());
    teamDto.setIsActive(team.getIsActive());
    teamDto.setProjectManagerId(team.getProjectManager().getId());

    teamDto.setMemberIds(
        (team.getMembers() == null || team.getMembers().isEmpty())
            ? new ArrayList<>()
            : team.getMembers().stream()
                .map(TeamMember::getUser)
                .map(User::getId)
                .toList()
    );

    return teamDto;
  }

  private User validateAndGetProjectManager(Long projectManagerId) {
    User projectManager = userRepository.findById(projectManagerId)
        .orElseThrow(() -> new UserNotFoundException(
            "Invalid Project Manager Id: " + projectManagerId));

    if (!projectManager.getRole().toString().equals(RoleType.PROJECTMANAGER.toString())) {
      throw new InvalidRoleException("The user is not a project manager: " + projectManager);
    }

    return projectManager;
  }

  public List<TeamDto> getAllTeams() {
    List<TeamDto> teamsDto = teamRepository.findAll().stream().map(this::convertToDto).toList();
    if (teamsDto.isEmpty()) {
      throw new NoDataFoundException("No teams found.");
    }

    return teamsDto;
  }

  public TeamDto getTeamById(Long id) {
    return teamRepository.findById(id)
        .map(this::convertToDto)
        .orElseThrow(() -> new TeamNotFoundException("Invalid team id: " + id));
  }

  @Transactional
  public TeamDto createTeam(TeamDto teamDto) {
    if (teamRepository.existsByNameIgnoreCase(teamDto.getName())) {
      throw new TeamNameAlreadyExistsException(
          "A team with the name " + teamDto.getName() + " already exists.");
    }

    User projectManager = validateAndGetProjectManager(teamDto.getProjectManagerId());

    Team team = teamDto.toEntity(projectManager);
    team = teamRepository.save(team);

    return convertToDto(team);
  }

  @Transactional
  public TeamDto addMembersToTeam(Long teamId, MembersDto membersDto) {
    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Invalid team id: " + teamId));

    List<User> members = userRepository.findAllById(membersDto.getMemberIds());

    for (User member : members) {
      boolean isMemberActive = teamMemberRepository.findActiveMembersByUserId(member.getId())
          .stream().anyMatch(TeamMember::getIsActive);
      if (isMemberActive) {
        throw new TeamMemberAlreadyActiveException(
            "The user " + member.getUsername() + " is already active in another team.");
      }

      if (!member.getRole().equals(RoleType.TEAMMEMBER)) {
        throw new InvalidRoleException("The user is not a team member: " + member.getUsername());
      }

      TeamMember teamMember = new TeamMember();
      teamMember.setTeam(team);
      teamMember.setUser(member);
      teamMember.setIsActive(true);
      teamMember.setJoinDate(LocalDate.now());
      teamMemberRepository.save(teamMember);

      team.getMembers().add(teamMember);
    }

    team = teamRepository.save(team);

    List<Long> memberIds = team.getMembers().stream()
        .map(teamMember -> teamMember.getUser().getId()).toList();

    TeamDto teamDto = convertToDto(team);
    teamDto.setMemberIds(memberIds);

    return teamDto;
  }


  @Transactional
  public TeamDto removeMembersFromTeam(Long teamId, MembersDto membersDto) {
    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Invalid team id: " + teamId));

    List<TeamMember> members = teamMemberRepository.findActiveMembersByUserIdsAndTeam(
        membersDto.getMemberIds(), team);
    if (members.isEmpty()) {
      throw new NoDataFoundException("No active members found in the team with given ids.");
    }

    for (TeamMember teamMember : members) {
      if (!teamMember.getUser().getRole().toString().equals(RoleType.TEAMMEMBER.toString())) {
        throw new InvalidRoleException("Cannot remove users that are not team members.");
      }

      teamMember.setIsActive(false);
      teamMember.setRemoveDate(LocalDate.now());
      teamMemberRepository.save(teamMember);
    }

    return convertToDto(team);
  }

  @Transactional
  public TeamDto updateTeam(Long id, TeamDto teamDto) {
    Team team = teamRepository.findById(id)
        .orElseThrow(() -> new TeamNotFoundException("Invalid team id: " + id));

    if (teamRepository.existsByNameIgnoreCaseAndIdNot(teamDto.getName(), id)) {
      throw new TeamNameAlreadyExistsException(
          "Team with name " + teamDto.getName() + " already exists.");
    }

    team.setName(teamDto.getName());
    team = teamRepository.save(team);

    return convertToDto(team);
  }

  @Transactional
  public TeamDto changeProjectManager(Long id, Long projectManagerId) {
    Team team = teamRepository.findById(id)
        .orElseThrow(() -> new TeamNotFoundException("Invalid team id: " + id));

    User newProjectManager = validateAndGetProjectManager(projectManagerId);
    if (team.getProjectManager().getId().equals(newProjectManager.getId())) {
      throw new IllegalArgumentException(
          "The selected user is already the current project manager.");
    }

    team.setProjectManager(newProjectManager);
    team = teamRepository.save(team);

    return convertToDto(team);
  }

  public TeamDto getTeamByName(String name) {
    Team team = teamRepository.findByName(name)
        .orElseThrow(() -> new TeamNotFoundException("Invalid team name: " + name));

    return convertToDto(team);
  }

  public List<TeamDto> getTeamsByProjectManagerId(Long projectManagerId) {
    List<Team> teams = teamRepository.findByProjectManagerId(projectManagerId);
    if (teams.isEmpty()) {
      throw new NoDataFoundException(
          "No teams found with project manager id: " + projectManagerId);
    }

    return teams.stream().map(this::convertToDto).toList();
  }

  public List<TeamDto> getTeamsByMemberId(Long memberId) {
    List<Team> teams = teamRepository.findByMembersId(memberId);
    if (teams.isEmpty()) {
      throw new NoDataFoundException("No teams found with member id: " + memberId);
    }

    return teams.stream().map(this::convertToDto).toList();
  }

  @Transactional
  public TeamDto deactivateTeam(Long teamId) {
    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found"));

    team.setIsActive(false);
    teamRepository.save(team);

    List<TeamMember> activeMembers = teamMemberRepository.findByTeamAndIsActiveTrue(team);
    for (TeamMember member : activeMembers) {
      member.setIsActive(false);
    }

    teamMemberRepository.saveAll(activeMembers);

    return convertToDto(team);
  }
}
