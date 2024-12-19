package com.progresso.backend.repository;

import com.progresso.backend.model.Team;
import com.progresso.backend.model.TeamMember;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

  @Query("SELECT tm FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.isActive = :isActive")
  Page<TeamMember> findActiveMembersByTeamId(@Param("teamId") Long teamId,
      @Param("isActive") Boolean isActive, Pageable pageable);

  @Query("SELECT tm FROM TeamMember tm WHERE tm.user.id = :userId AND tm.isActive = :isActive")
  Page<TeamMember> findMembersByUserIdAndStatus(@Param("userId") Long userId,
      @Param("isActive") Boolean isActive, Pageable pageable);

  @Query("SELECT tm FROM TeamMember tm WHERE tm.user.id = :userId AND tm.isActive = true")
  List<TeamMember> findActiveMembersByUserId(@Param("userId") Long userId);

  List<TeamMember> findByTeamAndIsActiveTrue(Team team);

  List<TeamMember> findByUserIdAndTeamIdAndIsActiveTrue(Long userId, Long teamId);
}
