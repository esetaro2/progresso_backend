package com.progresso.backend.teammanagement;

import com.progresso.backend.entity.Team;
import com.progresso.backend.enumeration.Status;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamRepository extends JpaRepository<Team, Long> {

  @Query("SELECT t FROM Team t WHERE (:active IS NULL OR t.active = :active) "
      + "AND (:searchTerm IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
  Page<Team> findAllTeamsWithFilters(@Param("active") Boolean active,
      @Param("searchTerm") String searchTerm, Pageable pageable);

  Boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

  Boolean existsByNameIgnoreCase(String name);

  @Query("SELECT t FROM Team t WHERE t.active = true AND NOT EXISTS "
      + "(SELECT p FROM t.projects p WHERE p.status IN :activeStatuses) "
      + "AND (:searchTerm IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
  Page<Team> findTeamsWithoutActiveProjects(@Param("activeStatuses") List<Status> activeStatuses,
      @Param("searchTerm") String searchTerm,
      Pageable pageable);
}
