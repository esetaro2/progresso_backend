package com.progresso.backend.service;

import com.progresso.backend.dto.CommentDto;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.exception.CommentNotFoundException;
import com.progresso.backend.exception.NoDataFoundException;
import com.progresso.backend.exception.ProjectNotFoundException;
import com.progresso.backend.exception.UserNotActiveException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.Comment;
import com.progresso.backend.model.Project;
import com.progresso.backend.model.User;
import com.progresso.backend.repository.CommentRepository;
import com.progresso.backend.repository.ProjectRepository;
import com.progresso.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final ProjectRepository projectRepository;

  @Autowired
  public CommentService(CommentRepository commentRepository, UserRepository userRepository,
      ProjectRepository projectRepository) {
    this.commentRepository = commentRepository;
    this.userRepository = userRepository;
    this.projectRepository = projectRepository;
  }

  public CommentDto convertToDto(Comment comment) {
    CommentDto commentDto = new CommentDto();
    commentDto.setId(comment.getId());
    commentDto.setContent(comment.getContent());
    commentDto.setCreationDate(comment.getCreationDate());
    commentDto.setUserId(comment.getUser().getId());
    commentDto.setProjectId(comment.getProject().getId());
    commentDto.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
    commentDto.setModified(comment.getModified());
    commentDto.setModifiedDate(comment.getModifiedDate());
    commentDto.setDeleted(comment.getDeleted());
    return commentDto;
  }

  private Boolean isUserInProject(User user, Project project) {
    return project.getTeam().getTeamMembers().contains(user) || project.getProjectManager()
        .equals(user);
  }

  public Boolean isManagerOrMemberOfProject(Long projectId, String username) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

    return project.getProjectManager().getUsername().equals(username) || project.getTeam()
        .getTeamMembers().stream().map(User::getUsername)
        .anyMatch(memberUsername -> memberUsername.equals(username));
  }

  public Boolean isCommentOwner(Long commentId, String username) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentNotFoundException("Comment not found with ID: " + commentId));

    return comment.getUser().getUsername().equals(username);
  }

  public CommentDto getCommentById(Long id) {
    Comment comment = commentRepository.findById(id)
        .orElseThrow(() -> new CommentNotFoundException("Comment not found with ID: " + id));

    return convertToDto(comment);
  }

  public Page<CommentDto> findByProjectIdAndParentIsNull(Long projectId, Pageable pageable) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

    Page<Comment> comments = commentRepository.findByProjectIdAndParentIsNull(projectId, pageable);

    if (comments.isEmpty()) {
      throw new NoDataFoundException("No root comments found for project: " + project.getName());
    }

    return comments.map(this::convertToDto);
  }

  public Page<CommentDto> findByParentId(Long parentId, Pageable pageable) {
    commentRepository.findById(parentId)
        .orElseThrow(() -> new CommentNotFoundException("Comment not found with ID: " + parentId));

    Page<Comment> comments = commentRepository.findByParentId(parentId, pageable);

    if (comments.isEmpty()) {
      throw new NoDataFoundException("No replies found for comment with ID: " + parentId);
    }

    return comments.map(this::convertToDto);
  }

  public Page<CommentDto> findByProjectId(Long projectId, Pageable pageable) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

    Page<Comment> comments = commentRepository.findByProjectId(projectId, pageable);

    if (comments.isEmpty()) {
      throw new NoDataFoundException("No comments found for project: " + project.getName());
    }

    return comments.map(this::convertToDto);
  }

  public Page<CommentDto> findByUserId(Long userId, Pageable pageable) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

    Page<Comment> comments = commentRepository.findByUserId(userId, pageable);

    if (comments.isEmpty()) {
      throw new NoDataFoundException("No comments found for user: " + user.getUsername());
    }

    return comments.map(this::convertToDto);
  }

  public Page<CommentDto> findByProjectIdAndContentContaining(Long projectId, String content,
      Pageable pageable) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

    Page<Comment> comments = commentRepository.findByProjectIdAndContentContaining(projectId,
        content, pageable);

    if (comments.isEmpty()) {
      throw new NoDataFoundException(
          "No comments found for project " + project.getName() + " with the given content");
    }

    return comments.map(this::convertToDto);
  }

  public Page<CommentDto> findRootCommentsByProjectId(Long projectId, Pageable pageable) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

    Page<Comment> comments = commentRepository.findRootCommentsByProjectId(projectId, pageable);

    if (comments.isEmpty()) {
      throw new NoDataFoundException("No root comments found for project: " + project.getName());
    }

    return comments.map(this::convertToDto);
  }

  public Page<CommentDto> findRepliesByParentId(Long parentId, Pageable pageable) {
    commentRepository.findById(parentId)
        .orElseThrow(() -> new CommentNotFoundException("Comment not found with ID: " + parentId));

    Page<Comment> comments = commentRepository.findRepliesByParentId(parentId, pageable);

    if (comments.isEmpty()) {
      throw new NoDataFoundException("No replies found for comment with ID: " + parentId);
    }

    return comments.map(this::convertToDto);
  }

  public Page<CommentDto> findCommentsByUserIdAndProjectId(Long projectId, Long userId,
      Pageable pageable) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

    Page<Comment> comments = commentRepository.findCommentsByUserIdAndProjectId(userId, projectId,
        pageable);

    if (comments.isEmpty()) {
      throw new NoDataFoundException(
          "No comments found by user " + user.getUsername() + " in project " + project.getName());
    }

    return comments.map(this::convertToDto);
  }

  public Page<CommentDto> findActiveCommentsByProjectId(Long projectId, Pageable pageable) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

    Page<Comment> comments = commentRepository.findActiveCommentsByProjectId(projectId, pageable);

    if (comments.isEmpty()) {
      throw new NoDataFoundException("No active comments found for project: " + project.getName());
    }

    return comments.map(this::convertToDto);
  }

  public Page<CommentDto> findActiveCommentsByUserId(Long userId, Pageable pageable) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

    Page<Comment> comments = commentRepository.findActiveCommentsByUserId(userId, pageable);

    if (comments.isEmpty()) {
      throw new NoDataFoundException("No active comments found for user: " + user.getUsername());
    }

    return comments.map(this::convertToDto);
  }

  public Page<CommentDto> findActiveCommentsByProjectIdAndContentContaining(Long projectId,
      String content, Pageable pageable) {
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ProjectNotFoundException("Project not found with ID: " + projectId));

    Page<Comment> comments = commentRepository.findActiveCommentsByProjectIdAndContentContaining(
        projectId, content, pageable);

    if (comments.isEmpty()) {
      throw new NoDataFoundException(
          "No comments found for project: " + project.getName() + " with the given content");
    }

    return comments.map(this::convertToDto);
  }

  @Transactional
  public CommentDto createComment(CommentDto commentDto) {
    User user = userRepository.findById(commentDto.getUserId()).orElseThrow(
        () -> new UserNotFoundException("User not found with ID: " + commentDto.getUserId()));
    Project project = projectRepository.findById(commentDto.getProjectId()).orElseThrow(
        () -> new ProjectNotFoundException(
            "Project not found with ID: " + commentDto.getProjectId()));

    if (!user.getActive()) {
      throw new UserNotActiveException("User " + user.getUsername() + " is not active");
    }

    if (project.getStatus().equals(Status.COMPLETED) || project.getStatus()
        .equals(Status.CANCELLED)) {
      throw new IllegalArgumentException("Cannot comment an inactive project");
    }

    if (!isUserInProject(user, project)) {
      throw new UserNotActiveException(
          "User " + user.getUsername() + " is not working at this project");
    }

    Comment comment = new Comment();
    comment.setContent(commentDto.getContent());
    comment.setCreationDate(LocalDateTime.now());
    comment.setUser(user);
    comment.setProject(project);
    comment.setParent(
        commentDto.getParentId() != null ? commentRepository.findById(commentDto.getParentId())
            .orElseThrow(() -> new CommentNotFoundException(
                "Parent not found with ID: " + commentDto.getParentId())) : null);
    comment.setModified(false);
    comment.setDeleted(false);

    if (comment.getParent() != null) {
      comment.getParent().getReplies().add(comment);
      commentRepository.save(comment.getParent());
    }

    user.getComments().add(comment);
    project.getComments().add(comment);

    Comment savedComment = commentRepository.save(comment);
    return convertToDto(savedComment);
  }

  @Transactional
  public CommentDto updateComment(Long commentId, String newContent) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentNotFoundException("Comment not found with ID: " + commentId));

    comment.setContent(newContent);
    comment.setModified(true);
    comment.setModifiedDate(LocalDateTime.now());

    Comment updatedComment = commentRepository.save(comment);

    return convertToDto(updatedComment);
  }

  @Transactional
  public CommentDto deleteComment(Long commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CommentNotFoundException("Comment not found with ID: " + commentId));

    if (comment.getDeleted()) {
      throw new IllegalArgumentException("Comment already deleted");
    }

    comment.setContent("This comment has been deleted.");
    comment.setDeleted(true);

    Comment deletedComment = commentRepository.save(comment);

    return convertToDto(deletedComment);
  }
}
