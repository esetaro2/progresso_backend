package com.progresso.backend.service;

import com.progresso.backend.dto.MembersDto;
import com.progresso.backend.dto.TeamMemberDto;
import com.progresso.backend.enumeration.RoleType;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.NoDataFoundException;
import com.progresso.backend.exception.TeamMemberAlreadyActiveException;
import com.progresso.backend.exception.TeamNotFoundException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.Team;
import com.progresso.backend.model.TeamMember;
import com.progresso.backend.model.User;
import com.progresso.backend.repository.TeamMemberRepository;
import com.progresso.backend.repository.TeamRepository;
import com.progresso.backend.repository.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamMemberService {

  private final TeamMemberRepository teamMemberRepository;
  private final TeamRepository teamRepository;
  private final UserRepository userRepository;

  @Autowired
  public TeamMemberService(TeamMemberRepository teamMemberRepository,
      TeamRepository teamRepository,
      UserRepository userRepository) {
    this.teamMemberRepository = teamMemberRepository;
    this.teamRepository = teamRepository;
    this.userRepository = userRepository;
  }

  private TeamMemberDto convertToTeamMemberDto(TeamMember teamMember) {
    return new TeamMemberDto(
        teamMember.getId(),
        teamMember.getUser().getId(),
        teamMember.getUser().getUsername(),
        teamMember.getTeam().getId(),
        teamMember.getJoinDate(),
        teamMember.getRemoveDate(),
        teamMember.getIsActive()
    );
  }

  public Page<TeamMemberDto> findActiveMembersByTeamId(Long teamId, Boolean isActive,
      Pageable pageable) {
    teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with id: " + teamId));

    Page<TeamMember> teamMembers = teamMemberRepository.findActiveMembersByTeamId(teamId, isActive,
        pageable);

    if (!teamMembers.hasContent()) {
      throw new NoDataFoundException("No members with this state in this team: " + isActive);
    }

    return teamMembers.map(this::convertToTeamMemberDto);
  }

  public Page<TeamMemberDto> findMembersByUserIdAndStatus(Long userId, Boolean isActive,
      Pageable pageable) {
    userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

    Page<TeamMember> teamMembers = teamMemberRepository.findMembersByUserIdAndStatus(userId,
        isActive, pageable);
    return teamMembers.map(this::convertToTeamMemberDto);
  }

  public TeamMemberDto addMembersToTeam(Long teamId, MembersDto membersDto) {
    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with id: " + teamId));

    if (!team.getIsActive()) {
      throw new IllegalArgumentException("The team is not active: " + team.getName());
    }

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

    return convertToTeamMemberDto(team.getMembers().get(team.getMembers().size() - 1));
  }

  @Transactional
  public List<TeamMemberDto> removeMembersFromTeam(Long teamId, MembersDto membersDto) {
    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new TeamNotFoundException("Team not found with id: " + teamId));

    if (!team.getIsActive()) {
      throw new IllegalArgumentException("The team is not active: " + team.getName());
    }

    List<TeamMember> members = membersDto.getMemberIds().stream().flatMap(
        userId -> teamMemberRepository.findByUserIdAndTeamIdAndIsActiveTrue(userId, teamId)
            .stream()).toList();

    if (members.isEmpty()) {
      throw new NoDataFoundException(
          "The following members are not found in the team: " + membersDto.getMemberIds());
    }

    List<TeamMemberDto> removedMembersDto = new ArrayList<>();

    for (TeamMember teamMember : members) {
      if (!teamMember.getUser().getRole().equals(RoleType.TEAMMEMBER)) {
        throw new InvalidRoleException("Cannot remove users that are not team members.");
      }

      teamMember.setIsActive(false);
      teamMember.setRemoveDate(LocalDate.now());
      teamMemberRepository.save(teamMember);

      removedMembersDto.add(convertToTeamMemberDto(teamMember));
    }

    return removedMembersDto;
  }
}
