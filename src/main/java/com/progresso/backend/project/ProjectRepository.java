package com.progresso.backend.project;

import com.progresso.backend.enumeration.Priority;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.model.Project;
import com.progresso.backend.model.Team;
import com.progresso.backend.model.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

  @Query("SELECT p FROM Project p WHERE (:status IS NULL OR p.status = :status)"
      + "AND (:priority IS NULL OR p.priority = :priority) "
      + "AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))")
  Page<Project> findAllWithFilters(
      @Param("status") Status status,
      @Param("priority") Priority priority,
      @Param("name") String name,
      Pageable pageable);

  @Query("SELECT p FROM Project p "
      + "JOIN p.projectManager pm "
      + "WHERE pm.username = :managerUsername "
      + "AND (:status IS NULL OR p.status = :status) "
      + "AND (:priority IS NULL OR p.priority = :priority) "
      + "AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) ")
  Page<Project> findByProjectManagerUsernameAndFilters(
      @Param("managerUsername") String managerUsername,
      @Param("status") Status status,
      @Param("priority") Priority priority,
      @Param("name") String name,
      Pageable pageable);

  @Query("SELECT p FROM Project p "
      + "JOIN p.team t "
      + "JOIN t.teamMembers tm "
      + "WHERE tm.username = :teamMemberUsername "
      + "AND (:status IS NULL OR p.status = :status) "
      + "AND (:priority IS NULL OR p.priority = :priority) "
      + "AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) ")
  Page<Project> findByTeamMemberUsernameAndFilters(
      @Param("teamMemberUsername") String teamMemberUsername,
      @Param("status") Status status,
      @Param("priority") Priority priority,
      @Param("name") String name,
      Pageable pageable);

  @Query("SELECT p FROM Project p "
      + "JOIN p.team t "
      + "JOIN t.teamMembers tm "
      + "WHERE tm.username = :teamMemberUsername "
      + "AND ((p.status = 'IN_PROGRESS') OR (p.status = 'NOT_STARTED'))")
  Page<Project> findActiveProjectsByTeamMemberUsername(
      @Param("teamMemberUsername") String teamMemberUsername,
      Pageable pageable);

  long countByProjectManagerAndStatusNotIn(User projectManager, List<Status> excludedStatus);

  long countByTeamAndStatusNotIn(Team team, List<Status> excludedStatus);

  boolean existsByNameIgnoreCase(String name);
}
