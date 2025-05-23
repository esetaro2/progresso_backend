package com.progresso.backend.commentmanagement;

import com.progresso.backend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  Page<Comment> findByProjectId(Long projectId, Pageable pageable);
}
