package com.progresso.backend.repository;

import com.progresso.backend.enumeration.Status;
import com.progresso.backend.model.Team;
import java.util.List;
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

  @Query("SELECT t FROM Team t JOIN t.teamMembers tm WHERE tm.id = :userId AND tm.active = true")
  Page<Team> findByTeamMemberId(@Param("userId") Long userId, Pageable pageable);

  @Query("SELECT t FROM Team t WHERE SIZE(t.projects) > 0 AND t.active = true")
  Page<Team> findTeamsWithProjects(Pageable pageable);

  @Query("SELECT t FROM Team t WHERE t.active = true AND NOT EXISTS "
      + "(SELECT p FROM t.projects p WHERE p.status IN :activeStatuses) "
      + "AND (:searchTerm IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
  Page<Team> findTeamsWithoutActiveProjects(@Param("activeStatuses") List<Status> activeStatuses,
      @Param("searchTerm") String searchTerm,
      Pageable pageable);

  @Query("SELECT t FROM Team t WHERE t.active = true "
      + "AND (SELECT COUNT(tm) FROM t.teamMembers tm WHERE tm.active = true) > :size")
  Page<Team> findByTeamMembersSizeGreaterThan(@Param("size") int size, Pageable pageable);

  @Query("SELECT t FROM Team t WHERE t.active = true "
      + "AND (SELECT COUNT(tm) FROM t.teamMembers tm WHERE tm.active = true) = 0")
  Page<Team> findTeamsWithoutMembers(Pageable pageable);

  @Query("SELECT t FROM Team t JOIN t.projects p WHERE p.id = :projectId "
      + "AND EXISTS (SELECT tm FROM t.teamMembers tm WHERE tm.active = true)")
  Page<Team> findTeamsByProjectId(@Param("projectId") Long projectId, Pageable pageable);

  Page<Team> findTeamsByActive(Boolean active, Pageable pageable);
}
