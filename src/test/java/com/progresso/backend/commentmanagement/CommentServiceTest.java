package com.progresso.backend.commentmanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.progresso.backend.dto.CommentDto;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.exception.CommentNotFoundException;
import com.progresso.backend.exception.ProjectNotFoundException;
import com.progresso.backend.exception.UserNotActiveException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.entity.Comment;
import com.progresso.backend.entity.Project;
import com.progresso.backend.entity.Team;
import com.progresso.backend.entity.User;
import com.progresso.backend.projectmanagement.ProjectRepository;
import com.progresso.backend.usermanagement.UserRepository;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

  @Spy
  @InjectMocks
  private CommentService commentService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private CommentRepository commentRepository;

  @Test
  void createComment_UserNotFound() {
    CommentDto inputDto = new CommentDto();
    inputDto.setContent("This is a comment.");
    inputDto.setUserId(999L);
    inputDto.setProjectId(100L);
    inputDto.setParentId(null);

    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> commentService.createComment(inputDto));

    verify(commentRepository, never()).save(any(Comment.class));
  }

  @Test
  void createComment_UserNotActive() {
    CommentDto inputDto = new CommentDto();
    inputDto.setContent("This is a comment.");
    inputDto.setUserId(10L);
    inputDto.setProjectId(100L);
    inputDto.setParentId(null);

    User user = new User();
    user.setId(10L);
    user.setUsername("u.member.tm1@progresso.com");
    user.setActive(false);
    user.setRole(Role.TEAMMEMBER);
    user.setComments(new ArrayList<>());

    when(userRepository.findById(10L)).thenReturn(Optional.of(user));

    Project project = new Project();
    project.setId(100L);
    project.setStatus(Status.IN_PROGRESS);
    project.setComments(new ArrayList<>());
    when(projectRepository.findById(100L)).thenReturn(Optional.of(project));

    assertThrows(UserNotActiveException.class, () -> commentService.createComment(inputDto));

    verify(commentRepository, never()).save(any(Comment.class));
  }

  @Test
  void createComment_UserNotInProject() {
    CommentDto inputDto = new CommentDto();
    inputDto.setContent("This is a comment.");
    inputDto.setUserId(20L);
    inputDto.setProjectId(200L);
    inputDto.setParentId(null);

    User user = new User();
    user.setId(20L);
    user.setUsername("u.member.tm1@progresso.com");
    user.setActive(true);
    user.setRole(Role.TEAMMEMBER);
    user.setComments(new ArrayList<>());

    when(userRepository.findById(20L)).thenReturn(Optional.of(user));

    Project project = new Project();
    project.setId(200L);
    project.setStatus(Status.IN_PROGRESS);
    project.setComments(new ArrayList<>());
    project.setTeam(null);
    when(projectRepository.findById(200L)).thenReturn(Optional.of(project));

    doReturn(false).when(commentService).isUserInProject(any(User.class), any(Project.class));

    assertThrows(UserNotFoundException.class, () -> commentService.createComment(inputDto));

    verify(commentRepository, never()).save(any(Comment.class));
  }

  @Test
  void createComment_ProjectNotFound() {
    CommentDto inputDto = new CommentDto();
    inputDto.setContent("This is a comment.");
    inputDto.setUserId(20L);
    inputDto.setProjectId(200L);
    inputDto.setParentId(null);

    User user = new User();
    user.setId(20L);
    user.setUsername("u.member.tm1@progresso.com");
    user.setActive(true);
    user.setRole(Role.TEAMMEMBER);
    user.setComments(new ArrayList<>());

    when(userRepository.findById(20L)).thenReturn(Optional.of(user));

    when(projectRepository.findById(200L)).thenReturn(Optional.empty());

    assertThrows(ProjectNotFoundException.class, () -> commentService.createComment(inputDto));

    verify(commentRepository, never()).save(any(Comment.class));
  }

  @Test
  void createComment_ProjectCompleted() {
    CommentDto inputDto = new CommentDto();
    inputDto.setContent("This is a comment.");
    inputDto.setUserId(20L);
    inputDto.setProjectId(200L);
    inputDto.setParentId(null);

    User user = new User();
    user.setId(20L);
    user.setUsername("u.member.tm1@progresso.com");
    user.setActive(true);
    user.setRole(Role.TEAMMEMBER);
    user.setComments(new ArrayList<>());

    Project project = new Project();
    project.setId(200L);
    project.setStatus(Status.COMPLETED);
    project.setComments(new ArrayList<>());

    when(userRepository.findById(20L)).thenReturn(Optional.of(user));
    when(projectRepository.findById(200L)).thenReturn(Optional.of(project));

    assertThrows(IllegalArgumentException.class, () -> commentService.createComment(inputDto));

    verify(commentRepository, never()).save(any(Comment.class));
  }

  @Test
  void createComment_ProjectCancelled() {
    CommentDto inputDto = new CommentDto();
    inputDto.setContent("This is a comment.");
    inputDto.setUserId(20L);
    inputDto.setProjectId(200L);
    inputDto.setParentId(null);

    User user = new User();
    user.setId(20L);
    user.setUsername("u.member.tm1@progresso.com");
    user.setActive(true);
    user.setRole(Role.TEAMMEMBER);
    user.setComments(new ArrayList<>());

    Project project = new Project();
    project.setId(200L);
    project.setStatus(Status.CANCELLED);
    project.setComments(new ArrayList<>());

    when(userRepository.findById(20L)).thenReturn(Optional.of(user));
    when(projectRepository.findById(200L)).thenReturn(Optional.of(project));

    assertThrows(IllegalArgumentException.class, () -> commentService.createComment(inputDto));

    verify(commentRepository, never()).save(any(Comment.class));
  }

  @Test
  void createComment_ParentCommentNotFound() {
    CommentDto inputDto = new CommentDto();
    inputDto.setContent("This is a reply.");
    inputDto.setUserId(20L);
    inputDto.setProjectId(200L);
    inputDto.setParentId(5L);

    User user = new User();
    user.setId(20L);
    user.setUsername("u.member.tm1@progresso.com");
    user.setActive(true);
    user.setRole(Role.TEAMMEMBER);
    user.setComments(new ArrayList<>());

    Project project = new Project();
    project.setId(200L);
    project.setStatus(Status.IN_PROGRESS);
    project.setComments(new ArrayList<>());
    project.setTeam(new Team());

    when(userRepository.findById(20L)).thenReturn(Optional.of(user));
    when(projectRepository.findById(200L)).thenReturn(Optional.of(project));

    when(commentRepository.findById(5L)).thenReturn(Optional.empty());

    doReturn(true).when(commentService).isUserInProject(any(User.class), any(Project.class));

    assertThrows(CommentNotFoundException.class, () -> commentService.createComment(inputDto));

    verify(commentRepository, never()).save(any(Comment.class));
  }

  @Test
  void createComment_ParentCommentDeleted() {
    CommentDto inputDto = new CommentDto();
    inputDto.setContent("This is a reply.");
    inputDto.setUserId(20L);
    inputDto.setProjectId(200L);
    inputDto.setParentId(5L);

    User user = new User();
    user.setId(20L);
    user.setUsername("u.member.tm1@progresso.com");
    user.setActive(true);
    user.setRole(Role.TEAMMEMBER);
    user.setComments(new ArrayList<>());

    Project project = new Project();
    project.setId(200L);
    project.setStatus(Status.IN_PROGRESS);
    project.setComments(new ArrayList<>());
    project.setTeam(new Team());

    Comment parentComment = new Comment();
    parentComment.setId(5L);
    parentComment.setContent("Original comment");
    parentComment.setDeleted(true);

    when(userRepository.findById(20L)).thenReturn(Optional.of(user));
    when(projectRepository.findById(200L)).thenReturn(Optional.of(project));

    when(commentRepository.findById(5L)).thenReturn(Optional.of(parentComment));

    doReturn(true).when(commentService).isUserInProject(any(User.class), any(Project.class));

    assertThrows(IllegalArgumentException.class, () -> commentService.createComment(inputDto));

    verify(commentRepository, never()).save(any(Comment.class));
  }

  @Test
  void createComment_UserAdmin() {
    User user = new User();
    user.setId(20L);
    user.setUsername("a.admin.am1@progresso.com");
    user.setActive(true);
    user.setRole(Role.ADMIN);
    user.setComments(new ArrayList<>());

    Project project = new Project();
    project.setId(200L);
    project.setStatus(Status.IN_PROGRESS);
    project.setComments(new ArrayList<>());

    Team team = new Team();
    project.setTeam(team);

    CommentDto inputDto = new CommentDto();
    inputDto.setContent("This is an admin comment.");
    inputDto.setUserId(20L);
    inputDto.setProjectId(200L);
    inputDto.setParentId(null);

    when(userRepository.findById(20L)).thenReturn(Optional.of(user));

    when(projectRepository.findById(200L)).thenReturn(Optional.of(project));

    when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
      Comment c = invocation.getArgument(0);
      c.setId(1L);
      return c;
    });

    CommentDto expectedOutput = new CommentDto();
    expectedOutput.setId(1L);
    expectedOutput.setContent("This is an admin comment.");
    expectedOutput.setParentId(null);
    doReturn(expectedOutput).when(commentService).convertToDto(any(Comment.class));

    CommentDto result = commentService.createComment(inputDto);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("This is an admin comment.", result.getContent());
    assertNull(result.getParentId());

    ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
    verify(commentRepository).save(commentCaptor.capture());
    Comment savedComment = commentCaptor.getValue();
    assertNotNull(savedComment);
    assertEquals("This is an admin comment.", savedComment.getContent());
  }

  @Test
  void createComment_Success_NoParent() {
    CommentDto inputDto = new CommentDto();
    inputDto.setContent("This is a comment.");
    inputDto.setUserId(10L);
    inputDto.setProjectId(100L);
    inputDto.setParentId(null);

    User user = new User();
    user.setId(10L);
    user.setUsername("u.member.tm1@progresso.com");
    user.setActive(true);
    user.setRole(Role.TEAMMEMBER);
    user.setComments(new ArrayList<>());

    Project project = new Project();
    project.setId(100L);
    project.setStatus(Status.IN_PROGRESS);
    project.setComments(new ArrayList<>());

    when(userRepository.findById(10L)).thenReturn(Optional.of(user));
    when(projectRepository.findById(100L)).thenReturn(Optional.of(project));

    doReturn(true).when(commentService).isUserInProject(any(User.class), any(Project.class));

    when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
      Comment c = invocation.getArgument(0);
      c.setId(1L);
      return c;
    });

    CommentDto expectedOutput = new CommentDto();
    expectedOutput.setId(1L);
    expectedOutput.setContent("This is a comment.");
    doReturn(expectedOutput).when(commentService).convertToDto(any(Comment.class));

    CommentDto result = commentService.createComment(inputDto);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("This is a comment.", result.getContent());

    assertFalse(user.getComments().isEmpty());
    assertFalse(project.getComments().isEmpty());

    ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
    verify(commentRepository).save(commentCaptor.capture());
    Comment savedComment = commentCaptor.getValue();
    assertNotNull(savedComment);
    assertNull(savedComment.getParent());
  }

  @Test
  void createComment_Success_WithParent() {
    CommentDto inputDto = new CommentDto();
    inputDto.setContent("This is a reply.");
    inputDto.setUserId(20L);
    inputDto.setProjectId(200L);
    inputDto.setParentId(5L);

    User user = new User();
    user.setId(20L);
    user.setUsername("u.member.tm1@progresso.com");
    user.setActive(true);
    user.setRole(Role.TEAMMEMBER);
    user.setComments(new ArrayList<>());

    Project project = new Project();
    project.setId(200L);
    project.setStatus(Status.IN_PROGRESS);
    project.setComments(new ArrayList<>());

    Comment parentComment = new Comment();
    parentComment.setId(5L);
    parentComment.setContent("Original comment");
    parentComment.setDeleted(false);

    when(userRepository.findById(20L)).thenReturn(Optional.of(user));
    when(projectRepository.findById(200L)).thenReturn(Optional.of(project));
    when(commentRepository.findById(5L)).thenReturn(Optional.of(parentComment));

    doReturn(true).when(commentService).isUserInProject(any(User.class), any(Project.class));

    when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
      Comment c = invocation.getArgument(0);
      c.setId(2L);
      return c;
    });

    CommentDto expectedOutput = new CommentDto();
    expectedOutput.setId(2L);
    expectedOutput.setContent("This is a reply.");
    expectedOutput.setParentId(5L);

    doReturn(expectedOutput).when(commentService).convertToDto(any(Comment.class));

    CommentDto result = commentService.createComment(inputDto);

    assertNotNull(result);
    assertEquals(2L, result.getId());
    assertEquals("This is a reply.", result.getContent());
    assertEquals(5L, result.getParentId());

    ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
    verify(commentRepository).save(commentCaptor.capture());
    Comment savedComment = commentCaptor.getValue();
    assertNotNull(savedComment);
    assertNotNull(savedComment.getParent());
    assertEquals(5L, savedComment.getParent().getId());

    assertFalse(user.getComments().isEmpty());
    assertFalse(project.getComments().isEmpty());
  }
}
