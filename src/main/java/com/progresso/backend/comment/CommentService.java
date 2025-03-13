package com.progresso.backend.comment;

import com.progresso.backend.dto.CommentDto;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.exception.CommentNotFoundException;
import com.progresso.backend.exception.NoDataFoundException;
import com.progresso.backend.exception.ProjectNotFoundException;
import com.progresso.backend.exception.UserNotActiveException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.Comment;
import com.progresso.backend.model.Project;
import com.progresso.backend.model.User;
import com.progresso.backend.project.ProjectRepository;
import com.progresso.backend.user.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
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
    commentDto.setUserFirstName(comment.getUser().getFirstName());
    commentDto.setUserLastName(comment.getUser().getLastName());
    commentDto.setUserUsername(comment.getUser().getUsername());
    commentDto.setProjectId(comment.getProject().getId());
    commentDto.setModified(comment.getModified());
    commentDto.setModifiedDate(comment.getModifiedDate());
    commentDto.setDeleted(comment.getDeleted());

    commentDto.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
    commentDto.setParentContent(
        comment.getParent() != null ? comment.getParent().getContent() : null);
    commentDto.setParentCreationDate(
        comment.getParent() != null ? comment.getParent().getCreationDate() : null);
    commentDto.setParentUserId(
        comment.getParent() != null ? comment.getParent().getUser().getId() : null);
    commentDto.setParentUserFirstName(
        comment.getParent() != null ? comment.getParent().getUser().getFirstName() : null);
    commentDto.setParentUserLastName(
        comment.getParent() != null ? comment.getParent().getUser().getLastName() : null);
    commentDto.setParentUserUsername(
        comment.getParent() != null ? comment.getParent().getUser().getUsername() : null);
    commentDto.setParentModified(
        comment.getParent() != null ? comment.getParent().getModified() : null);
    commentDto.setParentModifiedDate(
        comment.getParent() != null ? comment.getParent().getModifiedDate() : null);
    commentDto.setParentDeleted(
        comment.getParent() != null ? comment.getParent().getDeleted() : null);

    return commentDto;
  }

  public Boolean isUserInProject(User user, Project project) {
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
        || (project.getTeam() != null && project.getTeam().getTeamMembers().stream()
        .map(User::getUsername)
        .anyMatch(memberUsername -> memberUsername.equals(username)));

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

  @SuppressWarnings("checkstyle:LineLength")
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

    if (!user.getRole().equals(Role.ADMIN) && !isUserInProject(user, project)) {
      logger.error("createComment: User {} is not working on project with ID: {}",
          user.getUsername(), project.getId());
      throw new UserNotFoundException(
          "User " + user.getUsername() + " is not working on this project.");
    }

    Comment comment = new Comment();
    comment.setContent(commentDto.getContent());
    comment.setCreationDate(LocalDateTime.now());
    comment.setUser(user);
    comment.setProject(project);
    comment.setModified(false);
    comment.setDeleted(false);

    if (commentDto.getParentId() != null) {
      Comment parentComment = commentRepository.findById(commentDto.getParentId())
          .orElseThrow(() -> {
            logger.error("createComment: Parent comment not found with ID: {}",
                commentDto.getParentId());
            return new CommentNotFoundException("Parent comment not found.");
          });

      if (parentComment.getDeleted()) {
        logger.error("createComment: Cannot reply to a deleted comment. Parent Comment ID: {}",
            parentComment.getId());
        throw new IllegalArgumentException("Cannot reply to a deleted comment.");
      }

      comment.setParent(parentComment);
    }

    user.getComments().add(comment);
    project.getComments().add(comment);

    Comment savedComment = commentRepository.save(comment);

    if (comment.getParent() != null) {
      logger.info(
          "createComment: Created comment with ID: {} with parent with ID: {} for project with ID: {} by user: {}",
          savedComment.getId(), savedComment.getParent().getId(), project.getId(),
          user.getUsername());
    } else {
      logger.info(
          "createComment: Created comment with ID: {} for project with ID: {} by user: {}",
          savedComment.getId(), project.getId(), user.getUsername());
    }

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

    Project project = comment.getProject();

    if (comment.getDeleted()) {
      logger.error("updateComment: Cannot update a deleted comment. Comment ID: {}", commentId);
      throw new IllegalArgumentException("Cannot update a deleted comment.");
    }

    if (project.getStatus().equals(Status.COMPLETED) || project.getStatus()
        .equals(Status.CANCELLED)) {
      logger.error("updateComment: Cannot update a comment on an inactive project. Project ID: {}",
          project.getId());
      throw new IllegalArgumentException("Cannot update a comment on an inactive project.");
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

    Project project = comment.getProject();

    if (project.getStatus().equals(Status.COMPLETED) || project.getStatus()
        .equals(Status.CANCELLED)) {
      logger.error("deleteComment: Cannot delete a comment on an inactive project. Project ID: {}",
          project.getId());
      throw new IllegalArgumentException("Cannot delete a comment on an inactive project.");
    }

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
