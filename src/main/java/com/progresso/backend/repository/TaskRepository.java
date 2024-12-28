package com.progresso.backend.repository;

import com.progresso.backend.enumeration.Status;
import com.progresso.backend.model.Task;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

  Page<Task> findByStatus(Status status, Pageable pageable);

  Page<Task> findByPriority(com.progresso.backend.enumeration.Priority priority, Pageable pageable);

  Page<Task> findByDueDateBefore(LocalDate dueDate, Pageable pageable);

  Page<Task> findByCompletionDateAfter(LocalDate completionDate, Pageable pageable);

  Page<Task> findByProjectId(Long projectId, Pageable pageable);

  Page<Task> findByProjectIdAndStatus(Long projectId, Status status, Pageable pageable);

  @Query("SELECT t FROM Task t WHERE t.name LIKE %:name% AND t.project.id = :projectId")
  Page<Task> findByNameAndProjectId(String name, Long projectId, Pageable pageable);

  @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status = 'COMPLETED'")
  Page<Task> findCompletedTasksByProjectId(Long projectId, Pageable pageable);

  Page<Task> findByAssignedUserId(Long userId, Pageable pageable);

  Page<Task> findByAssignedUserIdAndStatus(Long userId, Status status, Pageable pageable);

  @Query("SELECT t FROM Task t WHERE t.assignedUser.id = :userId "
      + "AND t.dueDate < CURRENT_DATE AND t.status <> 'COMPLETED'")
  Page<Task> findOverdueTasksByUserId(Long userId, Pageable pageable);

  Page<Task> findByAssignedUserIdAndCompletionDateBefore(Long userId, LocalDate completionDate,
      Pageable pageable);

  Page<Task> findByAssignedUserIdAndStartDateAfter(Long userId, LocalDate startDate,
      Pageable pageable);

  Page<Task> findByProjectIdAndCompletionDateBefore(Long projectId, LocalDate completionDate,
      Pageable pageable);

  Page<Task> findByStartDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
