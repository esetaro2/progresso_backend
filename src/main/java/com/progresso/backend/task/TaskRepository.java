package com.progresso.backend.task;

import com.progresso.backend.entity.Task;
import com.progresso.backend.enumeration.Priority;
import com.progresso.backend.enumeration.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

  @Query("SELECT t FROM Task t "
      + "WHERE t.project.id = :projectId "
      + "AND (:status IS NULL OR t.status = :status) "
      + "AND (:priority IS NULL OR t.priority = :priority)")
  Page<Task> findByProjectIdAndStatusAndPriority(
      @Param("projectId") Long projectId,
      @Param("status") Status status,
      @Param("priority") Priority priority,
      Pageable pageable);

  @Query("SELECT COUNT(t) > 0 FROM Task t WHERE t.project.id = :projectId AND t.name = :name")
  boolean existsByProjectIdAndName(@Param("projectId") Long projectId,
      @Param("name") String name);
}
