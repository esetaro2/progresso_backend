package com.progresso.backend.repository;

import com.progresso.backend.model.Team;
import com.progresso.backend.model.TeamMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

  @Query("SELECT tm FROM TeamMember tm WHERE tm.team = :team AND tm.user.id IN :memberIds "
      + "AND tm.isActive = true")
  List<TeamMember> findActiveMembersByUserIdsAndTeam(@Param("memberIds") List<Long> memberIds,
      @Param("team") Team team);

  @Query("SELECT tm FROM TeamMember tm WHERE tm.user.id = :userId AND tm.isActive = true")
  List<TeamMember> findActiveMembersByUserId(@Param("userId") Long userId);

  List<TeamMember> findByTeamAndIsActiveTrue(Team team);
}
