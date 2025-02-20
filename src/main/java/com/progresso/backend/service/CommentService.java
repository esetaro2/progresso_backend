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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

  private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

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

    if (comment.getReplies() != null) {
      List<CommentDto> repliesDto = comment.getReplies().stream()
          .map(this::convertToDto).toList();
      commentDto.setReplies(repliesDto);
    }

    return commentDto;
  }

  private Boolean isUserInProject(User user, Project project) {
    Boolean isInProject =
        project.getTeam().getTeamMembers().contains(user) || project.getProjectManager()
            .equals(user);
    logger.info("isUserInProject: User {} is {}in the project with ID: {}", user.getUsername(),
        isInProject ? "" : "not ", project.getId());
    return isInProject;
  }

  public Boolean isManagerOrMemberOfProject(Long projectId, String username) {
    if (projectId == null) {
      logger.error("isManagerOrMemberOfProject: Project id cannot be null.");
      throw new ProjectNotFoundException("Project id cannot be null.");
    }

    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> {
          logger.error("isManagerOrMemberOfProject: Project not found with ID: {}", projectId);
          return new ProjectNotFoundException("Project not found.");
        });

    Boolean isManagerOrMember = project.getProjectManager().getUsername().equals(username)
        || project.getTeam().getTeamMembers().stream().map(User::getUsername)
        .anyMatch(memberUsername -> memberUsername.equals(username));

    logger.info(
        "isManagerOrMemberOfProject: User {} is {}a manager or member of the project with ID: {}",
        username, isManagerOrMember ? "" : "not ", projectId);
    return isManagerOrMember;
  }

  public Boolean isCommentOwner(Long commentId, String username) {
    if (commentId == null) {
      logger.error("isCommentOwner: Comment id cannot be null");
      throw new IllegalArgumentException("Comment id cannot be null.");
    }

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> {
          logger.error("isCommentOwner: Comment not found with ID: {}", commentId);
          return new CommentNotFoundException("Comment not found.");
        });

    Boolean isOwner = comment.getUser().getUsername().equals(username);

    logger.info("isCommentOwner: User {} is {}the owner of the comment with ID: {}", username,
        isOwner ? "" : "not ", commentId);
    return isOwner;
  }

  public CommentDto getCommentById(Long id) {
    if (id == null) {
      logger.error("getCommentById: Comment id cannot be null.");
      throw new IllegalArgumentException("Comment id cannot be null.");
    }

    Comment comment = commentRepository.findById(id)
        .orElseThrow(() -> {
          logger.error("getCommentById: Comment not found with ID: {}", id);
          return new CommentNotFoundException("Comment not found.");
        });

    logger.info("getCommentById: Retrieved comment with ID: {}", id);
    return convertToDto(comment);
  }

  public Page<CommentDto> findByProjectIdAndParentIsNull(Long projectId, Pageable pageable) {
    if (projectId == null) {
      logger.error("findByProjectIdAndParentIsNull: Project id cannot be null.");
      throw new IllegalArgumentException("Project id cannot be null.");
    }

    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> {
          logger.error("findByProjectIdAndParentIsNull: Project not found with ID: {}", projectId);
          return new ProjectNotFoundException("Project not found.");
        });

    Page<Comment> comments = commentRepository.findByProjectIdAndParentIsNull(projectId, pageable);

    if (comments.isEmpty()) {
      logger.warn("findByProjectIdAndParentIsNull: No root comments found for project: {}",
          project.getName());
      throw new NoDataFoundException(
          "No root comments found for project: " + project.getName() + ".");
    }

    logger.info(
        "findByProjectIdAndParentIsNull: Retrieved {} root comments for project with ID: {}",
        comments.getTotalElements(), projectId);
    return comments.map(this::convertToDto);
  }

  public Page<CommentDto> findByParentId(Long parentId, Pageable pageable) {
    if (parentId == null) {
      logger.error("findByParentId: Parent id cannot be null.");
      throw new IllegalArgumentException("Parent id cannot be null.");
    }

    commentRepository.findById(parentId)
        .orElseThrow(() -> {
          logger.error("findByParentId: Comment not found with ID: {}", parentId);
          return new CommentNotFoundException("Comment not found.");
        });

    Page<Comment> comments = commentRepository.findByParentId(parentId, pageable);

    if (comments.isEmpty()) {
      logger.warn("findByParentId: No replies found for comment with ID: {}", parentId);
      throw new NoDataFoundException("No replies found for this comment.");
    }

    logger.info("findByParentId: Retrieved {} replies for comment with ID: {}",
        comments.getTotalElements(), parentId);
    return comments.map(this::convertToDto);
  }

  public Page<CommentDto> findByProjectId(Long projectId, Pageable pageable) {
    if (projectId == null) {
      logger.error("findByProjectId: Project id cannot be null.");
      throw new IllegalArgumentException("Project id cannot be null.");
    }

    projectRepository.findById(projectId)
        .orElseThrow(() -> {
          logger.error("findByProjectId: Project not found with ID: {}", projectId);
          return new ProjectNotFoundException("Project not found.");
        });

    Page<Comment> comments = commentRepository.findByProjectId(projectId, pageable);

    if (comments.isEmpty()) {
      logger.warn("findByProjectId: No comments found for project with ID: {}", projectId);
      throw new NoDataFoundException("No comments found for this project.");
    }

    logger.info("findByProjectId: Retrieved {} comments for project with ID: {}",
        comments.getTotalElements(), projectId);
    return comments.map(this::convertToDto);
  }

  public Page<CommentDto> findByUserId(Long userId, Pageable pageable) {
    if (userId == null) {
      logger.error("findByUserId: User id cannot be null.");
      throw new IllegalArgumentException("User id cannot be null.");
    }

    userRepository.findById(userId)
        .orElseThrow(() -> {
          logger.error("findByUserId: User not found with ID: {}", userId);
          return new UserNotFoundException("User not found.");
        });

    Page<Comment> comments = commentRepository.findByUserId(userId, pageable);

    if (comments.isEmpty()) {
      logger.warn("findByUserId: No comments found for user with ID: {}", userId);
      throw new NoDataFoundException("No comments found for this user.");
    }

    logger.info("findByUserId: Retrieved {} comments for user with ID: {}",
        comments.getTotalElements(), userId);
    return comments.map(this::convertToDto);
  }

  @SuppressWarnings("checkstyle:LineLength")
  public Page<CommentDto> findByProjectIdAndContentContaining(Long projectId, String content,
      Pageable pageable) {
    if (projectId == null) {
      logger.error("findByProjectIdAndContentContaining: Project id cannot be null.");
      throw new IllegalArgumentException("Project id cannot be null.");
    }

    projectRepository.findById(projectId)
        .orElseThrow(() -> {
          logger.error("findByProjectIdAndContentContaining: Project not found with ID: {}",
              projectId);
          return new ProjectNotFoundException("Project not found.");
        });

    Page<Comment> comments = commentRepository.findByProjectIdAndContentContaining(projectId,
        content, pageable);

    if (comments.isEmpty()) {
      logger.warn(
          "findByProjectIdAndContentContaining: No comments found with the given content for project with ID: {}",
          projectId);
      throw new NoDataFoundException("No comments found with the given content.");
    }

    logger.info(
        "findByProjectIdAndContentContaining: Retrieved {} comments with the given content for project with ID: {}",
        comments.getTotalElements(), projectId);
    return comments.map(this::convertToDto);
  }

  public Page<CommentDto> findRootCommentsByProjectId(Long projectId, Pageable pageable) {
    if (projectId == null) {
      logger.error("findRootCommentsByProjectId: Project id cannot be null.");
      throw new IllegalArgumentException("Project id cannot be null.");
    }

    projectRepository.findById(projectId)
        .orElseThrow(() -> {
          logger.error("findRootCommentsByProjectId: Project not found with ID: {}", projectId);
          return new ProjectNotFoundException("Project not found.");
        });

    Page<Comment> comments = commentRepository.findRootCommentsByProjectId(projectId, pageable);

    if (comments.isEmpty()) {
      logger.warn("findRootCommentsByProjectId: No root comments found for project with ID: {}",
          projectId);
      throw new NoDataFoundException("No root comments found for this project.");
    }

    logger.info("findRootCommentsByProjectId: Retrieved {} root comments for project with ID: {}",
        comments.getTotalElements(), projectId);
    return comments.map(this::convertToDto);
  }

  public Page<CommentDto> findRepliesByParentId(Long parentId, Pageable pageable) {
    if (parentId == null) {
      logger.error("findRepliesByParentId: Parent id cannot be null.");
      throw new IllegalArgumentException("Parent id cannot be null.");
    }

    commentRepository.findById(parentId)
        .orElseThrow(() -> {
          logger.error("findRepliesByParentId: Comment not found with ID: {}", parentId);
          return new CommentNotFoundException("Comment not found.");
        });

    Page<Comment> comments = commentRepository.findRepliesByParentId(parentId, pageable);

    if (comments.isEmpty()) {
      logger.warn("findRepliesByParentId: No replies found for comment with ID: {}", parentId);
      throw new NoDataFoundException("No replies found for this comment.");
    }

    logger.info("findRepliesByParentId: Retrieved {} replies for comment with ID: {}",
        comments.getTotalElements(), parentId);
    return comments.map(this::convertToDto);
  }

  public Page<CommentDto> findCommentsByUserIdAndProjectId(Long projectId, Long userId,
      Pageable pageable) {
    if (projectId == null) {
      logger.error("findCommentsByUserIdAndProjectId: Project id cannot be null.");
      throw new IllegalArgumentException("Project id cannot be null.");
    }

    if (userId == null) {
      logger.error("findCommentsByUserIdAndProjectId: User id cannot be null.");
      throw new IllegalArgumentException("User id cannot be null.");
    }

    projectRepository.findById(projectId)
        .orElseThrow(() -> {
          logger.error("findCommentsByUserIdAndProjectId: Project not found with ID: {}",
              projectId);
          return new ProjectNotFoundException("Project not found.");
        });

    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          logger.error("findCommentsByUserIdAndProjectId: User not found with ID: {}", userId);
          return new UserNotFoundException("User not found.");
        });

    Page<Comment> comments = commentRepository.findCommentsByUserIdAndProjectId(userId, projectId,
        pageable);

    if (comments.isEmpty()) {
      logger.warn(
          "findCommentsByUserIdAndProjectId: No comments found by user {} in project with ID: {}",
          user.getUsername(), projectId);
      throw new NoDataFoundException(
          "No comments found by " + user.getUsername() + " in this project.");
    }

    logger.info(
        "findCommentsByUserIdAndProjectId: Retrieved {} comments by user {} in project with ID: {}",
        comments.getTotalElements(), user.getUsername(), projectId);
    return comments.map(this::convertToDto);
  }

  public Page<CommentDto> findActiveCommentsByProjectId(Long projectId, Pageable pageable) {
    if (projectId == null) {
      logger.error("findActiveCommentsByProjectId: Project id cannot be null.");
      throw new IllegalArgumentException("Project id cannot be null.");
    }

    projectRepository.findById(projectId)
        .orElseThrow(() -> {
          logger.error("findActiveCommentsByProjectId: Project not found with ID: {}", projectId);
          return new ProjectNotFoundException("Project not found.");
        });

    Page<Comment> comments = commentRepository.findActiveCommentsByProjectId(projectId, pageable);

    if (comments.isEmpty()) {
      logger.warn("findActiveCommentsByProjectId: No active comments found for project with ID: {}",
          projectId);
      throw new NoDataFoundException("No active comments found for this project.");
    }

    logger.info(
        "findActiveCommentsByProjectId: Retrieved {} active comments for project with ID: {}",
        comments.getTotalElements(), projectId);
    return comments.map(this::convertToDto);
  }

  public Page<CommentDto> findActiveCommentsByUserId(Long userId, Pageable pageable) {
    if (userId == null) {
      logger.error("findActiveCommentsByUserId: User id cannot be null.");
      throw new IllegalArgumentException("User id cannot be null.");
    }

    userRepository.findById(userId)
        .orElseThrow(() -> {
          logger.error("findActiveCommentsByUserId: User not found with ID: {}", userId);
          return new UserNotFoundException("User not found.");
        });

    Page<Comment> comments = commentRepository.findActiveCommentsByUserId(userId, pageable);

    if (comments.isEmpty()) {
      logger.warn("findActiveCommentsByUserId: No active comments found for user with ID: {}",
          userId);
      throw new NoDataFoundException("No active comments found for this user.");
    }

    logger.info("findActiveCommentsByUserId: Retrieved {} active comments for user with ID: {}",
        comments.getTotalElements(), userId);
    return comments.map(this::convertToDto);
  }

  @SuppressWarnings("checkstyle:LineLength")
  public Page<CommentDto> findActiveCommentsByProjectIdAndContentContaining(Long projectId,
      String content, Pageable pageable) {
    if (projectId == null) {
      logger.error("findActiveCommentsByProjectIdAndContentContaining: Project id cannot be null.");
      throw new IllegalArgumentException("Project id cannot be null.");
    }

    projectRepository.findById(projectId)
        .orElseThrow(() -> {
          logger.error(
              "findActiveCommentsByProjectIdAndContentContaining: Project not found with ID: {}",
              projectId);
          return new ProjectNotFoundException("Project not found.");
        });

    Page<Comment> comments = commentRepository.findActiveCommentsByProjectIdAndContentContaining(
        projectId, content, pageable);

    if (comments.isEmpty()) {
      logger.warn(
          "findActiveCommentsByProjectIdAndContentContaining: No comments found for project with ID: {} with the given content.",
          projectId);
      throw new NoDataFoundException("No comments found for this project with the given content.");
    }

    logger.info(
        "findActiveCommentsByProjectIdAndContentContaining: Retrieved {} comments with the given content for project with ID: {}",
        comments.getTotalElements(), projectId);
    return comments.map(this::convertToDto);
  }

  @Transactional
  public CommentDto createComment(CommentDto commentDto) {
    User user = userRepository.findById(commentDto.getUserId())
        .orElseThrow(() -> {
          logger.error("createComment: User not found with ID: {}", commentDto.getUserId());
          return new UserNotFoundException("User not found.");
        });

    Project project = projectRepository.findById(commentDto.getProjectId())
        .orElseThrow(() -> {
          logger.error("createComment: Project not found with ID: {}", commentDto.getProjectId());
          return new ProjectNotFoundException("Project not found.");
        });

    if (!user.getActive()) {
      logger.error("createComment: User {} is not active.", user.getUsername());
      throw new UserNotActiveException("User " + user.getUsername() + " is not active.");
    }

    if (project.getStatus().equals(Status.COMPLETED) || project.getStatus()
        .equals(Status.CANCELLED)) {
      logger.error("createComment: Cannot comment on an inactive project. Project ID: {}",
          project.getId());
      throw new IllegalArgumentException("Cannot comment on an inactive project.");
    }

    if (!isUserInProject(user, project)) {
      logger.error("createComment: User {} is not working on project with ID: {}",
          user.getUsername(), project.getId());
      throw new UserNotActiveException(
          "User " + user.getUsername() + " is not working on this project.");
    }

    Comment comment = new Comment();
    comment.setContent(commentDto.getContent());
    comment.setCreationDate(LocalDateTime.now());
    comment.setUser(user);
    comment.setProject(project);
    comment.setParent(
        commentDto.getParentId() != null ? commentRepository.findById(commentDto.getParentId())
            .orElseThrow(() -> {
              logger.error("createComment: Parent comment not found with ID: {}",
                  commentDto.getParentId());
              return new CommentNotFoundException("Parent comment not found.");
            }) : null);
    comment.setModified(false);
    comment.setDeleted(false);

    if (comment.getParent() != null && comment.getParent().getDeleted()) {
      logger.error("createComment: Cannot reply to a deleted comment. Parent Comment ID: {}",
          comment.getParent().getId());
      throw new IllegalArgumentException("Cannot reply to a deleted comment.");
    }

    if (comment.getParent() != null) {
      comment.getParent().getReplies().add(comment);
      commentRepository.save(comment.getParent());
    }

    user.getComments().add(comment);
    project.getComments().add(comment);

    Comment savedComment = commentRepository.save(comment);
    logger.info("createComment: Created comment with ID: {} for project with ID: {} by user: {}",
        savedComment.getId(), project.getId(), user.getUsername());
    return convertToDto(savedComment);
  }

  @Transactional
  public CommentDto updateComment(Long commentId, String newContent) {
    if (commentId == null) {
      logger.error("updateComment: Comment id cannot be null.");
      throw new IllegalArgumentException("Comment id cannot be null.");
    }

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> {
          logger.error("updateComment: Comment not found with ID: {}", commentId);
          return new CommentNotFoundException("Comment not found.");
        });

    if (comment.getDeleted()) {
      logger.error("updateComment: Cannot update a deleted comment. Comment ID: {}", commentId);
      throw new IllegalArgumentException("Cannot update a deleted comment.");
    }

    comment.setContent(newContent);
    comment.setModified(true);
    comment.setModifiedDate(LocalDateTime.now());

    Comment updatedComment = commentRepository.save(comment);

    logger.info("updateComment: Updated comment with ID: {}", commentId);
    return convertToDto(updatedComment);
  }

  @Transactional
  public CommentDto deleteComment(Long commentId) {
    if (commentId == null) {
      logger.error("deleteComment: Comment id cannot be null.");
      throw new IllegalArgumentException("Comment id cannot be null.");
    }

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> {
          logger.error("deleteComment: Comment not found with ID: {}", commentId);
          return new CommentNotFoundException("Comment not found.");
        });

    if (comment.getDeleted()) {
      logger.error("deleteComment: This comment has already been deleted. Comment ID: {}",
          commentId);
      throw new IllegalArgumentException("This comment has already been deleted.");
    }

    comment.setContent("This comment has been deleted.");
    comment.setDeleted(true);

    Comment deletedComment = commentRepository.save(comment);

    logger.info("deleteComment: Deleted comment with ID: {}", commentId);
    return convertToDto(deletedComment);
  }
}
