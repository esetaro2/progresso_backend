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

  @PreAuthorize("hasAuthority('ADMIN') or "
      + "@commentService.isManagerOrMemberOfProject(#projectId, authentication.name)")
  @GetMapping("/project/{projectId}/comments")
  public ResponseEntity<Page<CommentDto>> findByProjectId(@PathVariable Long projectId,
      Pageable pageable) {
    Page<CommentDto> comments = commentService.findByProjectId(projectId, pageable);
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
