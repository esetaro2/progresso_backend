package com.progresso.backend.repository;

import com.progresso.backend.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  Page<Comment> findByProjectIdAndParentIsNull(Long projectId, Pageable pageable);

  Page<Comment> findByParentId(Long parentId, Pageable pageable);

  Page<Comment> findByProjectId(Long projectId, Pageable pageable);

  Page<Comment> findByUserId(Long userId, Pageable pageable);

  Page<Comment> findByProjectIdAndContentContaining(Long projectId, String content,
      Pageable pageable);

  @Query("SELECT c FROM Comment c WHERE c.project.id = :projectId AND c.parent IS NULL")
  Page<Comment> findRootCommentsByProjectId(@Param("projectId") Long projectId, Pageable pageable);

  @Query("SELECT c FROM Comment c WHERE c.parent.id = :parentId")
  Page<Comment> findRepliesByParentId(@Param("parentId") Long parentId, Pageable pageable);

  @Query("SELECT c FROM Comment c WHERE c.project.id = :projectId AND c.user.id = :userId")
  Page<Comment> findCommentsByUserIdAndProjectId(@Param("projectId") Long projectId,
      @Param("userId") Long userId, Pageable pageable);

  @Query("SELECT c FROM Comment c WHERE c.project.id = :projectId AND c.deleted = false")
  Page<Comment> findActiveCommentsByProjectId(@Param("projectId") Long projectId,
      Pageable pageable);

  @Query("SELECT c FROM Comment c WHERE c.user.id = :userId AND c.deleted = false")
  Page<Comment> findActiveCommentsByUserId(@Param("userId") Long userId, Pageable pageable);

  @Query("SELECT c FROM Comment c WHERE c.project.id = :projectId AND c.content LIKE %:content% "
      + "AND c.deleted = false")
  Page<Comment> findActiveCommentsByProjectIdAndContentContaining(
      @Param("projectId") Long projectId, @Param("content") String content, Pageable pageable);
}
