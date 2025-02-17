package com.progresso.backend.controller;

import com.progresso.backend.dto.CommentDto;
import com.progresso.backend.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@Validated
public class CommentController {

  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @GetMapping("/{id}")
  public ResponseEntity<CommentDto> getCommentById(@PathVariable Long id) {
    CommentDto commentDto = commentService.getCommentById(id);
    return ResponseEntity.ok(commentDto);
  }

  @GetMapping("/project/{projectId}/root-comments")
  public ResponseEntity<Page<CommentDto>> findByProjectIdAndParentIsNull(
      @PathVariable Long projectId, Pageable pageable) {
    Page<CommentDto> comments = commentService.findByProjectIdAndParentIsNull(projectId, pageable);
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/parent/{parentId}/replies")
  public ResponseEntity<Page<CommentDto>> findByParentId(@PathVariable Long parentId,
      Pageable pageable) {
    Page<CommentDto> comments = commentService.findByParentId(parentId, pageable);
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/project/{projectId}/comments")
  public ResponseEntity<Page<CommentDto>> findByProjectId(@PathVariable Long projectId,
      Pageable pageable) {
    Page<CommentDto> comments = commentService.findByProjectId(projectId, pageable);
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/user/{userId}/comments")
  public ResponseEntity<Page<CommentDto>> findByUserId(@PathVariable Long userId,
      Pageable pageable) {
    Page<CommentDto> comments = commentService.findByUserId(userId, pageable);
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/project/{projectId}/content")
  public ResponseEntity<Page<CommentDto>> findByProjectIdAndContentContaining(
      @PathVariable Long projectId, @RequestParam String content, Pageable pageable) {
    Page<CommentDto> comments = commentService.findByProjectIdAndContentContaining(projectId,
        content, pageable);
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/project/{projectId}/active-root-comments")
  public ResponseEntity<Page<CommentDto>> findRootCommentsByProjectId(@PathVariable Long projectId,
      Pageable pageable) {
    Page<CommentDto> comments = commentService.findRootCommentsByProjectId(projectId, pageable);
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/parent/{parentId}/active-replies")
  public ResponseEntity<Page<CommentDto>> findRepliesByParentId(@PathVariable Long parentId,
      Pageable pageable) {
    Page<CommentDto> comments = commentService.findRepliesByParentId(parentId, pageable);
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/project/{projectId}/user/{userId}/comments")
  public ResponseEntity<Page<CommentDto>> findCommentsByUserIdAndProjectId(
      @PathVariable Long projectId, @PathVariable Long userId, Pageable pageable) {
    Page<CommentDto> comments = commentService.findCommentsByUserIdAndProjectId(projectId, userId,
        pageable);
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/project/{projectId}/active-comments")
  public ResponseEntity<Page<CommentDto>> findActiveCommentsByProjectId(
      @PathVariable Long projectId, Pageable pageable) {
    Page<CommentDto> comments = commentService.findActiveCommentsByProjectId(projectId, pageable);
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/user/{userId}/active-comments")
  public ResponseEntity<Page<CommentDto>> findActiveCommentsByUserId(@PathVariable Long userId,
      Pageable pageable) {
    Page<CommentDto> comments = commentService.findActiveCommentsByUserId(userId, pageable);
    return ResponseEntity.ok(comments);
  }

  @GetMapping("/project/{projectId}/active-comments/content")
  public ResponseEntity<Page<CommentDto>> findActiveCommentsByProjectIdAndContentContaining(
      @PathVariable Long projectId, @RequestParam String content, Pageable pageable) {
    Page<CommentDto> comments = commentService.findActiveCommentsByProjectIdAndContentContaining(
        projectId, content, pageable);
    return ResponseEntity.ok(comments);
  }

  @PreAuthorize("hasAuthority('ADMIN') "
      + "or @commentService.isManagerOrMemberOfProject(#commentDto.projectId, authentication.name)")
  @PostMapping
  public ResponseEntity<CommentDto> createComment(@Valid @RequestBody CommentDto commentDto) {
    CommentDto createdComment = commentService.createComment(commentDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
  }

  @PreAuthorize("@commentService.isCommentOwner(#id, authentication.name)")
  @PutMapping("/{id}")
  public ResponseEntity<CommentDto> updateComment(@PathVariable Long id,
      @RequestParam @NotBlank(message = "Content cannot be empty")
      @Size(max = 500,
          message = "Content must be between 1 and 500 characters") String newContent) {
    CommentDto updatedComment = commentService.updateComment(id, newContent);
    return ResponseEntity.ok(updatedComment);
  }

  @PreAuthorize("hasAuthority('ADMIN') "
      + "or @commentService.isCommentOwner("
      + "#id, authentication.name)")
  @DeleteMapping("/{id}")
  public ResponseEntity<CommentDto> deleteComment(@PathVariable Long id) {
    CommentDto deletedComment = commentService.deleteComment(id);
    return ResponseEntity.ok(deletedComment);
  }
}
