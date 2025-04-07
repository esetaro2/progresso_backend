package com.progresso.backend.taskmanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.progresso.backend.dto.TaskDto;
import com.progresso.backend.enumeration.Priority;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.exception.InvalidRoleException;
import com.progresso.backend.exception.ProjectNotFoundException;
import com.progresso.backend.exception.UserNotActiveException;
import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.entity.Project;
import com.progresso.backend.entity.Task;
import com.progresso.backend.entity.Team;
import com.progresso.backend.entity.User;
import com.progresso.backend.projectmanagement.ProjectRepository;
import com.progresso.backend.usermanagement.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

  @InjectMocks
  private TaskService taskService;

  @Mock
  private TaskRepository taskRepository;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private UserRepository userRepository;

  @Test
  void createAndAssignTask_UserIdIsNull_ThrowsIllegalArgumentException() {
    Project project = new Project();
    project.setId(1L);
    project.setStatus(Status.IN_PROGRESS);
    project.setStartDate(LocalDate.now().plusDays(1));
    project.setDueDate(LocalDate.now().plusDays(10));

    Team team = new Team();
    team.setActive(true);
    project.setTeam(team);

    User user = new User();
    user.setId(1L);
    user.setActive(true);
    user.setRole(Role.TEAMMEMBER);
    user.setUsername("team.member@example.com");

    team.setTeamMembers(List.of(user));

    TaskDto taskDto = new TaskDto();
    taskDto.setName("New Task");
    taskDto.setDescription("Task description");
    taskDto.setPriority("HIGH");
    taskDto.setStartDate(LocalDate.now().plusDays(2));
    taskDto.setDueDate(LocalDate.now().plusDays(5));
    taskDto.setProjectId(project.getId());

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> taskService.createAndAssignTask(taskDto, null));

    assertEquals("User id cannot be null.", exception.getMessage());
  }

  @Test
  void createAndAssignTask_StartDateInPast() {
    Project project = new Project();
    project.setId(1L);
    project.setName("Test Project");
    project.setStatus(Status.NOT_STARTED);
    project.setStartDate(LocalDate.now().plusDays(1));
    project.setDueDate(LocalDate.now().plusDays(10));

    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

    TaskDto taskDto = new TaskDto();
    taskDto.setName("New Task");
    taskDto.setDescription("Task description");
    taskDto.setPriority("HIGH");
    taskDto.setStartDate(LocalDate.now().minusDays(1));
    taskDto.setDueDate(LocalDate.now().plusDays(5));
    taskDto.setProjectId(1L);

    Exception exception = assertThrows(IllegalArgumentException.class,
        () -> taskService.createAndAssignTask(taskDto, 1L));

    String expectedMessage = "Start date must be today or in the future.";
    String actualMessage = exception.getMessage();
    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void createAndAssignTask_StartDateAfterDueDate() {
    Project project = new Project();
    project.setId(1L);
    project.setName("Test Project");
    project.setStatus(Status.NOT_STARTED);
    project.setStartDate(LocalDate.now().plusDays(1));
    project.setDueDate(LocalDate.now().plusDays(10));

    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

    TaskDto taskDto = new TaskDto();
    taskDto.setName("New Task");
    taskDto.setDescription("Task description");
    taskDto.setPriority("HIGH");
    taskDto.setStartDate(LocalDate.now().plusDays(6));
    taskDto.setDueDate(LocalDate.now().plusDays(5));
    taskDto.setProjectId(1L);

    Exception exception = assertThrows(IllegalArgumentException.class,
        () -> taskService.createAndAssignTask(taskDto, 1L));

    String expectedMessage = "Start date cannot be after due date.";
    String actualMessage = exception.getMessage();
    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void createAndAssignTask_StartDateBeforeProjectStart() {
    Project project = new Project();
    project.setId(1L);
    project.setName("Test Project");
    project.setStatus(Status.NOT_STARTED);
    project.setStartDate(LocalDate.now().plusDays(3));
    project.setDueDate(LocalDate.now().plusDays(10));

    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

    TaskDto taskDto = new TaskDto();
    taskDto.setName("New Task");
    taskDto.setDescription("Task description");
    taskDto.setPriority("HIGH");
    taskDto.setStartDate(LocalDate.now().plusDays(2));
    taskDto.setDueDate(LocalDate.now().plusDays(5));
    taskDto.setProjectId(1L);

    Exception exception = assertThrows(IllegalArgumentException.class,
        () -> taskService.createAndAssignTask(taskDto, 1L));

    String expectedMessage = "Start date cannot be before project start.";
    String actualMessage = exception.getMessage();
    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void createAndAssignTask_StartDateAfterProjectDueDate() {
    Project project = new Project();
    project.setId(1L);
    project.setName("Test Project");
    project.setStatus(Status.NOT_STARTED);
    project.setStartDate(LocalDate.now().plusDays(1));
    project.setDueDate(LocalDate.now().plusDays(10));

    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

    TaskDto taskDto = new TaskDto();
    taskDto.setName("New Task");
    taskDto.setDescription("Task description");
    taskDto.setPriority("HIGH");
    taskDto.setStartDate(LocalDate.now().plusDays(11));
    taskDto.setDueDate(LocalDate.now().plusDays(12));
    taskDto.setProjectId(1L);

    Exception exception = assertThrows(IllegalArgumentException.class,
        () -> taskService.createAndAssignTask(taskDto, 1L));

    String expectedMessage = "Start date cannot be after project due date.";
    String actualMessage = exception.getMessage();
    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void createAndAssignTask_ProjectNotFound() {
    TaskDto taskDto = new TaskDto();
    taskDto.setName("New Task");
    taskDto.setDescription("Task description");
    taskDto.setPriority("HIGH");
    taskDto.setStartDate(LocalDate.now().plusDays(2));
    taskDto.setDueDate(LocalDate.now().plusDays(5));
    taskDto.setProjectId(1L);

    when(projectRepository.findById(1L)).thenReturn(Optional.empty());

    Exception exception = assertThrows(
        ProjectNotFoundException.class, () -> taskService.createAndAssignTask(taskDto, 1L));

    String expectedMessage = "Project not found.";
    String actualMessage = exception.getMessage();
    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void createAndAssignTask_ProjectCompleted() {
    Project project = new Project();
    project.setId(1L);
    project.setName("Test Project");
    project.setStatus(Status.COMPLETED);
    project.setStartDate(LocalDate.now().plusDays(1));
    project.setDueDate(LocalDate.now().plusDays(10));

    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

    TaskDto taskDto = new TaskDto();
    taskDto.setName("New Task");
    taskDto.setDescription("Task description");
    taskDto.setPriority("HIGH");
    taskDto.setStartDate(LocalDate.now().plusDays(2));
    taskDto.setDueDate(LocalDate.now().plusDays(5));
    taskDto.setProjectId(1L);

    Exception exception = assertThrows(IllegalStateException.class,
        () -> taskService.createAndAssignTask(taskDto, 1L));

    String expectedMessage = "Cannot create a task for a completed or cancelled project.";
    String actualMessage = exception.getMessage();
    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void createAndAssignTask_ProjectCancelled() {
    Project project = new Project();
    project.setId(1L);
    project.setName("Test Project");
    project.setStatus(Status.CANCELLED);
    project.setStartDate(LocalDate.now().plusDays(1));
    project.setDueDate(LocalDate.now().plusDays(10));

    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

    TaskDto taskDto = new TaskDto();
    taskDto.setName("New Task");
    taskDto.setDescription("Task description");
    taskDto.setPriority("HIGH");
    taskDto.setStartDate(LocalDate.now().plusDays(2));
    taskDto.setDueDate(LocalDate.now().plusDays(5));
    taskDto.setProjectId(1L);

    Exception exception = assertThrows(IllegalStateException.class,
        () -> taskService.createAndAssignTask(taskDto, 1L));

    String expectedMessage = "Cannot create a task for a completed or cancelled project.";
    String actualMessage = exception.getMessage();
    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void createAndAssignTask_UpdateProjectStatusToInProgress() {
    Project projectNotInProgress = new Project();
    projectNotInProgress.setId(1L);
    projectNotInProgress.setName("Test Project");
    projectNotInProgress.setStatus(Status.NOT_STARTED);
    projectNotInProgress.setStartDate(LocalDate.now().plusDays(1));
    projectNotInProgress.setDueDate(LocalDate.now().plusDays(10));

    Team team = new Team();
    team.setActive(true);
    team.setTeamMembers(new ArrayList<>());
    projectNotInProgress.setTeam(team);

    when(projectRepository.findById(1L)).thenReturn(Optional.of(projectNotInProgress));

    User user = new User();
    user.setId(1L);
    user.setUsername("t.member.tm1@progresso.com");
    user.setActive(true);
    user.setRole(Role.TEAMMEMBER);
    user.setAssignedTasks(new ArrayList<>());

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    team.getTeamMembers().add(user);

    TaskDto taskDto = new TaskDto();
    taskDto.setName("New Task");
    taskDto.setDescription("Task description");
    taskDto.setPriority("HIGH");
    taskDto.setStartDate(LocalDate.now().plusDays(2));
    taskDto.setDueDate(LocalDate.now().plusDays(5));
    taskDto.setProjectId(1L);

    Task savedTask = new Task();
    savedTask.setId(1L);
    savedTask.setName(taskDto.getName());
    savedTask.setDescription(taskDto.getDescription());
    savedTask.setPriority(Priority.HIGH);
    savedTask.setStartDate(taskDto.getStartDate());
    savedTask.setDueDate(taskDto.getDueDate());
    savedTask.setProject(projectNotInProgress);
    savedTask.setAssignedUser(user);
    savedTask.setStatus(Status.IN_PROGRESS);

    when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

    TaskDto createdTask = taskService.createAndAssignTask(taskDto, user.getId());

    assertNotNull(createdTask);
    assertEquals(savedTask.getId(), createdTask.getId());
    assertEquals(savedTask.getName(), createdTask.getName());
    assertEquals(savedTask.getDescription(), createdTask.getDescription());
    assertEquals(savedTask.getPriority().name(), createdTask.getPriority());
    assertEquals(savedTask.getStartDate(), createdTask.getStartDate());
    assertEquals(savedTask.getDueDate(), createdTask.getDueDate());
    assertEquals(savedTask.getAssignedUser().getId(), user.getId());

    verify(taskRepository, times(1)).save(any(Task.class));

    verify(projectRepository, times(1)).save(projectNotInProgress);
  }

  @Test
  void createAndAssignTask_ProjectAlreadyInProgress() {
    Project projectInProgress = new Project();
    projectInProgress.setId(1L);
    projectInProgress.setName("Test Project");
    projectInProgress.setStatus(Status.IN_PROGRESS);
    projectInProgress.setStartDate(LocalDate.now().plusDays(1));
    projectInProgress.setDueDate(LocalDate.now().plusDays(10));

    Team team = new Team();
    team.setActive(true);
    team.setTeamMembers(new ArrayList<>());
    projectInProgress.setTeam(team);

    when(projectRepository.findById(1L)).thenReturn(Optional.of(projectInProgress));

    User user = new User();
    user.setId(1L);
    user.setUsername("t.member.tm1@progresso.com");
    user.setActive(true);
    user.setRole(Role.TEAMMEMBER);
    user.setAssignedTasks(new ArrayList<>());

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    team.getTeamMembers().add(user);

    TaskDto taskDto = new TaskDto();
    taskDto.setName("New Task");
    taskDto.setDescription("Task description");
    taskDto.setPriority("HIGH");
    taskDto.setStartDate(LocalDate.now().plusDays(2));
    taskDto.setDueDate(LocalDate.now().plusDays(5));
    taskDto.setProjectId(1L);

    Task savedTask = new Task();
    savedTask.setId(1L);
    savedTask.setName(taskDto.getName());
    savedTask.setDescription(taskDto.getDescription());
    savedTask.setPriority(Priority.HIGH);
    savedTask.setStartDate(taskDto.getStartDate());
    savedTask.setDueDate(taskDto.getDueDate());
    savedTask.setProject(projectInProgress);
    savedTask.setAssignedUser(user);
    savedTask.setStatus(Status.IN_PROGRESS);

    when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

    TaskDto createdTask = taskService.createAndAssignTask(taskDto, user.getId());

    assertNotNull(createdTask);
    assertEquals(savedTask.getId(), createdTask.getId());
    assertEquals(savedTask.getName(), createdTask.getName());
    assertEquals(savedTask.getDescription(), createdTask.getDescription());
    assertEquals(savedTask.getPriority().name(), createdTask.getPriority());
    assertEquals(savedTask.getStartDate(), createdTask.getStartDate());
    assertEquals(savedTask.getDueDate(), createdTask.getDueDate());
    assertEquals(savedTask.getAssignedUser().getId(), user.getId());

    verify(taskRepository, times(1)).save(any(Task.class));

    verify(projectRepository, times(0)).save(projectInProgress);
  }


  @Test
  void createAndAssignTask_InactiveTeam() {
    Project project = new Project();
    project.setId(1L);
    project.setName("Test Project");
    project.setStatus(Status.NOT_STARTED);
    project.setStartDate(LocalDate.now().plusDays(1));
    project.setDueDate(LocalDate.now().plusDays(10));

    Team team = new Team();
    team.setActive(false);
    project.setTeam(team);

    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

    TaskDto taskDto = new TaskDto();
    taskDto.setName("New Task");
    taskDto.setDescription("Task description");
    taskDto.setPriority("HIGH");
    taskDto.setStartDate(LocalDate.now().plusDays(2));
    taskDto.setDueDate(LocalDate.now().plusDays(5));
    taskDto.setProjectId(1L);

    Exception exception = assertThrows(IllegalStateException.class,
        () -> taskService.createAndAssignTask(taskDto, 1L));

    String expectedMessage = "Cannot assign task for members in an inactive team.";
    String actualMessage = exception.getMessage();
    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void createAndAssignTask_UserNotFound() {
    Project project = new Project();
    project.setId(1L);
    project.setName("Test Project");
    project.setStatus(Status.NOT_STARTED);
    project.setStartDate(LocalDate.now().plusDays(1));
    project.setDueDate(LocalDate.now().plusDays(10));

    Team team = new Team();
    team.setActive(true);
    project.setTeam(team);

    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

    TaskDto taskDto = new TaskDto();
    taskDto.setName("New Task");
    taskDto.setDescription("Task description");
    taskDto.setPriority("HIGH");
    taskDto.setStartDate(LocalDate.now().plusDays(2));
    taskDto.setDueDate(LocalDate.now().plusDays(5));
    taskDto.setProjectId(1L);

    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    Exception exception = assertThrows(UserNotFoundException.class,
        () -> taskService.createAndAssignTask(taskDto, 1L));

    String expectedMessage = "User not found.";
    String actualMessage = exception.getMessage();
    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void createAndAssignTask_UserNotActive() {
    Project project = new Project();
    project.setId(1L);
    project.setName("Test Project");
    project.setStatus(Status.NOT_STARTED);
    project.setStartDate(LocalDate.now().plusDays(1));
    project.setDueDate(LocalDate.now().plusDays(10));

    Team team = new Team();
    team.setActive(true);
    project.setTeam(team);

    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

    User user = new User();
    user.setId(1L);
    user.setUsername("t.member.tm1@progresso.com");
    user.setActive(false);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    TaskDto taskDto = new TaskDto();
    taskDto.setName("New Task");
    taskDto.setDescription("Task description");
    taskDto.setPriority("HIGH");
    taskDto.setStartDate(LocalDate.now().plusDays(2));
    taskDto.setDueDate(LocalDate.now().plusDays(5));
    taskDto.setProjectId(1L);

    Exception exception = assertThrows(UserNotActiveException.class,
        () -> taskService.createAndAssignTask(taskDto, 1L));

    String expectedMessage = "User t.member.tm1@progresso.com is not active.";
    String actualMessage = exception.getMessage();
    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void createAndAssignTask_UserNotTeamMember() {
    Project project = new Project();
    project.setId(1L);
    project.setName("Test Project");
    project.setStatus(Status.NOT_STARTED);
    project.setStartDate(LocalDate.now().plusDays(1));
    project.setDueDate(LocalDate.now().plusDays(10));

    Team team = new Team();
    team.setActive(true);
    project.setTeam(team);

    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

    User user = new User();
    user.setId(1L);
    user.setUsername("p.manager.pm1@progresso.com");
    user.setActive(true);
    user.setRole(Role.PROJECTMANAGER);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    TaskDto taskDto = new TaskDto();
    taskDto.setName("New Task");
    taskDto.setDescription("Task description");
    taskDto.setPriority("HIGH");
    taskDto.setStartDate(LocalDate.now().plusDays(2));
    taskDto.setDueDate(LocalDate.now().plusDays(5));
    taskDto.setProjectId(1L);

    Exception exception = assertThrows(InvalidRoleException.class,
        () -> taskService.createAndAssignTask(taskDto, 1L));

    String expectedMessage = "This user is not a team member: p.manager.pm1@progresso.com";
    String actualMessage = exception.getMessage();
    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void createAndAssignTask_UserNotInTeam() {
    Project project = new Project();
    project.setId(1L);
    project.setName("Test Project");
    project.setStatus(Status.NOT_STARTED);
    project.setStartDate(LocalDate.now().plusDays(1));
    project.setDueDate(LocalDate.now().plusDays(10));

    Team team = new Team();
    team.setActive(true);
    team.setTeamMembers(new ArrayList<>());
    project.setTeam(team);

    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

    User user = new User();
    user.setId(1L);
    user.setUsername("t.member.tm1@progresso.com");
    user.setActive(true);
    user.setRole(Role.TEAMMEMBER);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    TaskDto taskDto = new TaskDto();
    taskDto.setName("New Task");
    taskDto.setDescription("Task description");
    taskDto.setPriority("HIGH");
    taskDto.setStartDate(LocalDate.now().plusDays(2));
    taskDto.setDueDate(LocalDate.now().plusDays(5));
    taskDto.setProjectId(1L);

    Exception exception = assertThrows(IllegalArgumentException.class,
        () -> taskService.createAndAssignTask(taskDto, 1L));

    String expectedMessage = "User t.member.tm1@progresso.com not found in this team.";
    String actualMessage = exception.getMessage();
    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void createAndAssignTask_NullTeam() {
    Project project = new Project();
    project.setId(1L);
    project.setName("Test Project");
    project.setStatus(Status.NOT_STARTED);
    project.setStartDate(LocalDate.now().plusDays(1));
    project.setDueDate(LocalDate.now().plusDays(10));
    project.setTeam(null);

    when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

    User user = new User();
    user.setId(1L);
    user.setUsername("user.notinteam@progresso.com");
    user.setActive(true);
    user.setRole(Role.TEAMMEMBER);
    user.setAssignedTasks(new ArrayList<>());

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    TaskDto taskDto = new TaskDto();
    taskDto.setName("New Task");
    taskDto.setDescription("Task description");
    taskDto.setPriority("HIGH");
    taskDto.setStartDate(LocalDate.now().plusDays(2));
    taskDto.setDueDate(LocalDate.now().plusDays(5));
    taskDto.setProjectId(1L);

    assertThrows(NullPointerException.class, () -> taskService.createAndAssignTask(taskDto, 1L));
  }


  @Test
  void createAndAssignTask_Success() {
    Project project = new Project();
    project.setId(1L);
    project.setName("Test Project");
    project.setStatus(Status.NOT_STARTED);
    project.setStartDate(LocalDate.now().plusDays(1));
    project.setDueDate(LocalDate.now().plusDays(10));

    Team team = new Team();
    team.setActive(true);
    project.setTeam(team);

    User user = new User();
    user.setId(1L);
    user.setActive(true);
    user.setRole(Role.TEAMMEMBER);
    user.setUsername("t.member.tm1@progresso.com");
    user.setAssignedTasks(new ArrayList<>());

    team.setTeamMembers(List.of(user));

    TaskDto taskDto = new TaskDto();
    taskDto.setName("New Task");
    taskDto.setDescription("Task description");
    taskDto.setPriority("HIGH");
    taskDto.setStartDate(LocalDate.now().plusDays(2));
    taskDto.setDueDate(LocalDate.now().plusDays(5));
    taskDto.setProjectId(project.getId());

    Task savedTask = new Task();
    savedTask.setId(1L);
    savedTask.setName(taskDto.getName());
    savedTask.setDescription(taskDto.getDescription());
    savedTask.setPriority(Priority.HIGH);
    savedTask.setStartDate(taskDto.getStartDate());
    savedTask.setDueDate(taskDto.getDueDate());
    savedTask.setProject(project);
    savedTask.setAssignedUser(user);
    savedTask.setStatus(Status.IN_PROGRESS);

    when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

    when(taskRepository.existsByProjectIdAndName(project.getId(), taskDto.getName()))
        .thenReturn(false);

    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

    when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

    when(projectRepository.save(any(Project.class))).thenReturn(project);

    TaskDto createdTask = taskService.createAndAssignTask(taskDto, user.getId());

    assertNotNull(createdTask);
    assertEquals(savedTask.getId(), createdTask.getId());
    assertEquals(savedTask.getName(), createdTask.getName());
    assertEquals(savedTask.getDescription(), createdTask.getDescription());
    assertEquals(savedTask.getPriority().name(), createdTask.getPriority());
    assertEquals(savedTask.getStartDate(), createdTask.getStartDate());
    assertEquals(savedTask.getDueDate(), createdTask.getDueDate());
    assertEquals(savedTask.getAssignedUser().getId(), user.getId());

    verify(taskRepository, times(1)).save(any(Task.class));

    verify(projectRepository, times(1)).save(project);

    assertEquals(Status.IN_PROGRESS, project.getStatus());
  }

  @Test
  void createAndAssignTask_NameAlreadyExists() {
    Project project = new Project();
    project.setId(1L);
    project.setName("Test Project");
    project.setStatus(Status.NOT_STARTED);
    project.setStartDate(LocalDate.now().plusDays(1));
    project.setDueDate(LocalDate.now().plusDays(10));

    Team team = new Team();
    team.setActive(true);
    project.setTeam(team);

    User user = new User();
    user.setId(1L);
    user.setActive(true);
    user.setRole(Role.TEAMMEMBER);
    user.setUsername("t.member.tm1@progresso.com");
    user.setAssignedTasks(new ArrayList<>());

    team.setTeamMembers(List.of(user));

    TaskDto taskDto = new TaskDto();
    taskDto.setName("Existing Task");
    taskDto.setDescription("Task description");
    taskDto.setPriority("HIGH");
    taskDto.setStartDate(LocalDate.now().plusDays(2));
    taskDto.setDueDate(LocalDate.now().plusDays(5));
    taskDto.setProjectId(project.getId());

    when(taskRepository.existsByProjectIdAndName(project.getId(), "Existing Task")).thenReturn(
        true);
    when(taskRepository.existsByProjectIdAndName(project.getId(), "Existing Task (1)")).thenReturn(
        false);

    Task savedTask = new Task();
    savedTask.setId(1L);
    savedTask.setName("Existing Task (1)");
    savedTask.setDescription(taskDto.getDescription());
    savedTask.setPriority(Priority.HIGH);
    savedTask.setStartDate(taskDto.getStartDate());
    savedTask.setDueDate(taskDto.getDueDate());
    savedTask.setProject(project);
    savedTask.setAssignedUser(user);
    savedTask.setStatus(Status.IN_PROGRESS);

    when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

    when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

    when(projectRepository.save(any(Project.class))).thenReturn(project);

    TaskDto createdTask = taskService.createAndAssignTask(taskDto, user.getId());

    assertNotNull(createdTask);
    assertEquals(savedTask.getId(), createdTask.getId());
    assertEquals(savedTask.getName(), createdTask.getName());
    assertEquals(savedTask.getDescription(), createdTask.getDescription());
    assertEquals(savedTask.getPriority().name(), createdTask.getPriority());
    assertEquals(savedTask.getStartDate(), createdTask.getStartDate());
    assertEquals(savedTask.getDueDate(), createdTask.getDueDate());
    assertEquals(savedTask.getAssignedUser().getId(), user.getId());

    verify(taskRepository, times(1)).save(any(Task.class));

    verify(projectRepository, times(1)).save(project);

    assertEquals(Status.IN_PROGRESS, project.getStatus());
  }

  @Test
  void createAndAssignTask_LongNameExceeds100Characters() {
    Project project = new Project();
    project.setId(1L);
    project.setName("Test Project");
    project.setStatus(Status.NOT_STARTED);
    project.setStartDate(LocalDate.now().plusDays(1));
    project.setDueDate(LocalDate.now().plusDays(10));

    Team team = new Team();
    team.setActive(true);
    project.setTeam(team);

    User user = new User();
    user.setId(1L);
    user.setActive(true);
    user.setRole(Role.TEAMMEMBER);
    user.setUsername("t.member.tm1@progresso.com");
    user.setAssignedTasks(new ArrayList<>());

    team.setTeamMembers(List.of(user));

    String longProjectName = "This is a very long task name that will definitely exceed one hundred characters in total and needs to be truncated properly to ensure correctness";

    TaskDto taskDto = new TaskDto();
    taskDto.setName(longProjectName);
    taskDto.setDescription("Task description");
    taskDto.setPriority("HIGH");
    taskDto.setStartDate(LocalDate.now().plusDays(2));
    taskDto.setDueDate(LocalDate.now().plusDays(5));
    taskDto.setProjectId(project.getId());

    when(taskRepository.existsByProjectIdAndName(project.getId(), longProjectName)).thenReturn(
        true);
    String s = longProjectName.substring(0, 100 - " (1)".length()) + " (1)";

    Task savedTask = new Task();
    savedTask.setId(1L);
    savedTask.setName(s);
    savedTask.setDescription(taskDto.getDescription());
    savedTask.setPriority(Priority.HIGH);
    savedTask.setStartDate(taskDto.getStartDate());
    savedTask.setDueDate(taskDto.getDueDate());
    savedTask.setProject(project);
    savedTask.setAssignedUser(user);
    savedTask.setStatus(Status.IN_PROGRESS);

    when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

    when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

    when(projectRepository.save(any(Project.class))).thenReturn(project);

    TaskDto createdTask = taskService.createAndAssignTask(taskDto, user.getId());

    assertNotNull(createdTask);
    assertEquals(savedTask.getId(), createdTask.getId());
    assertEquals(savedTask.getName(), createdTask.getName());
    assertEquals(savedTask.getDescription(), createdTask.getDescription());
    assertEquals(savedTask.getPriority().name(), createdTask.getPriority());
    assertEquals(savedTask.getStartDate(), createdTask.getStartDate());
    assertEquals(savedTask.getDueDate(), createdTask.getDueDate());
    assertEquals(savedTask.getAssignedUser().getId(), user.getId());

    verify(taskRepository, times(1)).save(any(Task.class));

    verify(projectRepository, times(1)).save(project);

    assertEquals(Status.IN_PROGRESS, project.getStatus());
  }
}
