package com.progresso.backend.repository;

import com.progresso.backend.enumeration.Priority;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.model.Project;
import com.progresso.backend.model.Team;
import com.progresso.backend.model.User;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

  @Query("SELECT p FROM Project p")
  Page<Project> findAllProjects(Pageable pageable);

  Page<Project> findByStatus(Status status, Pageable pageable);

  Page<Project> findByProjectManagerId(Long projectManagerId, Pageable pageable);

  Page<Project> findByPriority(Priority priority, Pageable pageable);

  Page<Project> findByDueDateBefore(LocalDate dueDate, Pageable pageable);

  Page<Project> findByCompletionDateAfter(LocalDate completionDate, Pageable pageable);

  @Query("SELECT p FROM Project p WHERE p.projectManager.id = :managerId "
      + "AND p.status != 'COMPLETED' AND p.status != 'INACTIVE'")
  Page<Project> findActiveByProjectManager(Long managerId, Pageable pageable);

  @Query("SELECT DISTINCT p FROM Project p JOIN p.tasks t WHERE t.status = :taskStatus")
  Page<Project> findByTaskStatus(Status taskStatus, Pageable pageable);

  Page<Project> findByTeamId(Long teamId, Pageable pageable);

  Page<Project> findByTeamIdAndStatus(Long teamId, Status status, Pageable pageable);

  Page<Project> findByTeamIdAndDueDateBefore(Long teamId, LocalDate dueDate, Pageable pageable);

  @Query("SELECT p FROM Project p WHERE p.team.id = :teamId AND p.status != 'COMPLETED' "
      + "AND p.status != 'CANCELLED'")
  Page<Project> findActiveByTeamId(Long teamId, Pageable pageable);

  Page<Project> findByStatusAndPriority(Status status, Priority priority, Pageable pageable);

  Page<Project> findByStartDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

  Page<Project> findByCompletionDateBefore(LocalDate completionDate, Pageable pageable);

  long countByProjectManagerAndStatusNotIn(User projectManager, List<Status> excludedStatus);

  long countByTeamAndStatusNotIn(Team team, List<Status> excludedStatus);

  boolean existsByName(String name);
}
