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

  Page<Team> findByProjectManagerId(Long id, Pageable pageable);

  Page<Team> findByProjectManagerIdAndIsActive(Long id, Boolean isActive, Pageable pageable);

  Page<Team> findByMembersUserId(Long id, Pageable pageable);

  Page<Team> findTeamsByIsActive(Boolean isActive, Pageable pageable);

  Boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

  Boolean existsByNameIgnoreCase(String name);

  @Query("SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.isActive = true")
  Long countActiveMembersByTeamId(@Param("teamId") Long teamId);

  @Query("SELECT t FROM Team t LEFT JOIN t.members tm "
      + "WHERE (:name IS NULL OR LOWER(t.name) LIKE %:name%) "
      + "AND (:isActive IS NULL OR t.isActive = :isActive) "
      + "AND (:projectManagerId IS NULL OR t.projectManager.id = :projectManagerId) "
      + "AND (:memberUserId IS NULL OR tm.user.id = :memberUserId) "
      + "GROUP BY t")
  Page<Team> findTeamsByAdvancedFilters(
      @Param("name") String name,
      @Param("isActive") Boolean isActive,
      @Param("projectManagerId") Long projectManagerId,
      @Param("memberUserId") Long memberUserId,
      Pageable pageable
  );
}
