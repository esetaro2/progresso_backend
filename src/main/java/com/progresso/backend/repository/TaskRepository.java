package com.progresso.backend.repository;

import com.progresso.backend.enumeration.Priority;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.model.Task;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

  Page<Task> findByStatus(Status status, Pageable pageable);

  @Query("SELECT t FROM Task t WHERE t.priority = :priority")
  Page<Task> findByPriority(@Param("priority") Priority priority, Pageable pageable);

  @Query("SELECT t FROM Task t WHERE t.dueDate < :dueDate")
  Page<Task> findByDueDateBefore(@Param("dueDate") LocalDate dueDate, Pageable pageable);

  @Query("SELECT t FROM Task t WHERE t.completionDate > :completionDate")
  Page<Task> findByCompletionDateAfter(@Param("completionDate") LocalDate completionDate,
      Pageable pageable);

  @Query("SELECT t FROM Task t "
      + "WHERE t.project.id = :projectId "
      + "AND t.status <> 'CANCELLED' "
      + "AND (:status IS NULL OR t.status = :status) "
      + "AND (:priority IS NULL OR t.priority = :priority)")
  Page<Task> findByProjectIdAndStatusAndPriority(
      @Param("projectId") Long projectId,
      @Param("status") Status status,
      @Param("priority") Priority priority,
      Pageable pageable);


  @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
  Page<Task> findByProjectIdAndStatus(@Param("projectId") Long projectId,
      @Param("status") Status status, Pageable pageable);

  @Query("SELECT t FROM Task t WHERE t.name LIKE %:name% AND t.project.id = :projectId "
      + "AND t.status <> 'CANCELLED'")
  Page<Task> findByNameAndProjectId(@Param("name") String name, @Param("projectId") Long projectId,
      Pageable pageable);

  @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status = 'COMPLETED'")
  Page<Task> findCompletedTasksByProjectId(@Param("projectId") Long projectId, Pageable pageable);

  @Query("SELECT t FROM Task t WHERE t.assignedUser.id = :userId AND t.assignedUser.active = true "
      + "AND t.status <> 'CANCELLED'")
  Page<Task> findByAssignedUserId(@Param("userId") Long userId, Pageable pageable);

  @Query("SELECT t FROM Task t WHERE t.assignedUser.id = :userId AND t.assignedUser.active = true "
      + "AND t.status = :status")
  Page<Task> findByAssignedUserIdAndStatus(@Param("userId") Long userId,
      @Param("status") Status status, Pageable pageable);

  @Query(
      "SELECT t FROM Task t WHERE t.assignedUser.id = :userId AND t.assignedUser.active = true "
          + "AND t.dueDate < CURRENT_DATE "
          + "AND t.status <> 'COMPLETED'")
  Page<Task> findOverdueTasksByUserId(@Param("userId") Long userId, Pageable pageable);

  @Query("SELECT t FROM Task t WHERE t.assignedUser.id = :userId AND t.assignedUser.active = true "
      + "AND t.completionDate < :completionDate")
  Page<Task> findByAssignedUserIdAndCompletionDateBefore(@Param("userId") Long userId,
      @Param("completionDate") LocalDate completionDate, Pageable pageable);

  @Query("SELECT t FROM Task t WHERE t.assignedUser.id = :userId AND t.assignedUser.active = true "
      + "AND t.startDate > :startDate")
  Page<Task> findByAssignedUserIdAndStartDateAfter(@Param("userId") Long userId,
      @Param("startDate") LocalDate startDate, Pageable pageable);

  @Query(
      "SELECT t FROM Task t WHERE t.project.id = :projectId AND t.completionDate < :completionDate")
  Page<Task> findByProjectIdAndCompletionDateBefore(@Param("projectId") Long projectId,
      @Param("completionDate") LocalDate completionDate, Pageable pageable);

  @Query("SELECT t FROM Task t WHERE t.startDate BETWEEN :startDate AND :endDate")
  Page<Task> findByStartDateBetween(@Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate, Pageable pageable);

  @Query("SELECT COUNT(t) > 0 FROM Task t WHERE t.project.id = :projectId AND t.name = :name")
  boolean existsByProjectIdAndName(@Param("projectId") Long projectId,
      @Param("name") String name);
}
