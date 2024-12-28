package com.progresso.backend.repository;

import com.progresso.backend.model.Team;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<Team, Long> {

  Optional<Team> findByNameIgnoreCase(String name);

  @Query("SELECT t FROM Team t")
  Page<Team> findAllTeams(Pageable pageable);

  Boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

  Boolean existsByNameIgnoreCase(String name);

  @Query("SELECT t FROM Team t JOIN t.teamMembers tm WHERE tm.id = :userId")
  Optional<Team> findByTeamMemberId(@Param("userId") Long userId);

  @Query("SELECT t FROM Team t WHERE SIZE(t.projects) > 0")
  Page<Team> findTeamsWithProjects(Pageable pageable);

  @Query("SELECT t FROM Team t WHERE SIZE(t.teamMembers) > :size")
  Page<Team> findByTeamMembersSizeGreaterThan(@Param("size") int size, Pageable pageable);

  @Query("SELECT t FROM Team t WHERE SIZE(t.teamMembers) = 0")
  Page<Team> findTeamsWithoutMembers(Pageable pageable);

  @Query("SELECT t FROM Team t JOIN t.projects p WHERE p.id = :projectId")
  Page<Team> findTeamsByProjectId(@Param("projectId") Long projectId, Pageable pageable);
}
