package com.progresso.backend;

import com.progresso.backend.commentmanagement.CommentRepository;
import com.progresso.backend.entity.Comment;
import com.progresso.backend.entity.Project;
import com.progresso.backend.entity.Task;
import com.progresso.backend.entity.Team;
import com.progresso.backend.entity.User;
import com.progresso.backend.enumeration.Priority;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.enumeration.Status;
import com.progresso.backend.projectmanagement.ProjectRepository;
import com.progresso.backend.taskmanagement.TaskRepository;
import com.progresso.backend.teammanagement.TeamRepository;
import com.progresso.backend.usermanagement.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {


  @Profile("!test")
  @Bean
  public CommandLineRunner initData(
      UserRepository userRepository,
      TeamRepository teamRepository,
      ProjectRepository projectRepository,
      TaskRepository taskRepository,
      CommentRepository commentRepository,
      PasswordEncoder passwordEncoder) {
    return args -> {

      // Admin
      if (userRepository.findByUsername("a.superuser.am1@progresso.com").isEmpty()) {
        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("Superuser");
        admin.setUsername("a.superuser.am1@progresso.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin.superuser@example.com");
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin.setBirthDate(LocalDate.of(1990, 1, 1));
        admin.setCity("Admin City");
        admin.setCountry("Admin Country");
        admin.setPhoneNumber("+1 123 456 7890");
        admin.setStateProvinceRegion("Admin State");
        admin.setStreetAddress("Admin Street");
        admin.setZipCode("12345");
        userRepository.save(admin);
      }

      // Project Managers
      createUserIfNotExists(userRepository, passwordEncoder, "John", "Doe",
          "j.doe.pm1@progresso.com", "projectManager123", Role.PROJECTMANAGER);
      createUserIfNotExists(userRepository, passwordEncoder, "Emily", "Stone",
          "e.stone.pm2@progresso.com", "projectManager123", Role.PROJECTMANAGER);
      createUserIfNotExists(userRepository, passwordEncoder, "Michael", "Taylor",
          "m.taylor.pm3@progresso.com", "projectManager123", Role.PROJECTMANAGER);

      // Team Members
      createUserIfNotExists(userRepository, passwordEncoder, "James", "Smith",
          "j.smith.tm1@progresso.com", "teamMember123", Role.TEAMMEMBER);
      createUserIfNotExists(userRepository, passwordEncoder, "Sophia", "Johnson",
          "s.johnson.tm2@progresso.com", "teamMember123", Role.TEAMMEMBER);
      createUserIfNotExists(userRepository, passwordEncoder, "William", "Brown",
          "w.brown.tm3@progresso.com", "teamMember123", Role.TEAMMEMBER);
      createUserIfNotExists(userRepository, passwordEncoder, "Olivia", "Davis",
          "o.davis.tm4@progresso.com", "teamMember123", Role.TEAMMEMBER);
      createUserIfNotExists(userRepository, passwordEncoder, "Benjamin", "Miller",
          "b.miller.tm5@progresso.com", "teamMember123", Role.TEAMMEMBER);
      createUserIfNotExists(userRepository, passwordEncoder, "Isabella", "Wilson",
          "i.wilson.tm6@progresso.com", "teamMember123", Role.TEAMMEMBER);
      createUserIfNotExists(userRepository, passwordEncoder, "Lucas", "Moore",
          "l.moore.tm7@progresso.com", "teamMember123", Role.TEAMMEMBER);
      createUserIfNotExists(userRepository, passwordEncoder, "Mia", "Taylor",
          "m.taylor.tm8@progresso.com", "teamMember123", Role.TEAMMEMBER);
      createUserIfNotExists(userRepository, passwordEncoder, "Henry", "Anderson",
          "h.anderson.tm9@progresso.com", "teamMember123", Role.TEAMMEMBER);
      createUserIfNotExists(userRepository, passwordEncoder, "Charlotte", "Thomas",
          "c.thomas.tm10@progresso.com", "teamMember123", Role.TEAMMEMBER);
      createUserIfNotExists(userRepository, passwordEncoder, "Alexander", "Jackson",
          "a.jackson.tm11@progresso.com", "teamMember123", Role.TEAMMEMBER);
      createUserIfNotExists(userRepository, passwordEncoder, "Amelia", "White",
          "a.white.tm12@progresso.com", "teamMember123", Role.TEAMMEMBER);
      createUserIfNotExists(userRepository, passwordEncoder, "Daniel", "Harris",
          "d.harris.tm13@progresso.com", "teamMember123", Role.TEAMMEMBER);
      createUserIfNotExists(userRepository, passwordEncoder, "Harper", "Martin",
          "h.martin.tm14@progresso.com", "teamMember123", Role.TEAMMEMBER);
      createUserIfNotExists(userRepository, passwordEncoder, "Matthew", "Thompson",
          "m.thompson.tm15@progresso.com", "teamMember123", Role.TEAMMEMBER);
      createUserIfNotExists(userRepository, passwordEncoder, "Evelyn", "Garcia",
          "e.garcia.tm16@progresso.com", "teamMember123", Role.TEAMMEMBER);
      createUserIfNotExists(userRepository, passwordEncoder, "David", "Martinez",
          "d.martinez.tm17@progresso.com", "teamMember123", Role.TEAMMEMBER);
      createUserIfNotExists(userRepository, passwordEncoder, "Abigail", "Robinson",
          "a.robinson.tm18@progresso.com", "teamMember123", Role.TEAMMEMBER);

      // Teams
      User tm1 = userRepository.findByUsername("j.smith.tm1@progresso.com").orElseThrow();
      User tm2 = userRepository.findByUsername("s.johnson.tm2@progresso.com").orElseThrow();
      User tm3 = userRepository.findByUsername("w.brown.tm3@progresso.com").orElseThrow();
      User tm4 = userRepository.findByUsername("o.davis.tm4@progresso.com").orElseThrow();
      User tm5 = userRepository.findByUsername("b.miller.tm5@progresso.com").orElseThrow();
      User tm6 = userRepository.findByUsername("i.wilson.tm6@progresso.com").orElseThrow();
      User tm7 = userRepository.findByUsername("l.moore.tm7@progresso.com").orElseThrow();
      User tm8 = userRepository.findByUsername("m.taylor.tm8@progresso.com").orElseThrow();
      User tm9 = userRepository.findByUsername("h.anderson.tm9@progresso.com").orElseThrow();
      User tm10 = userRepository.findByUsername("c.thomas.tm10@progresso.com").orElseThrow();
      User tm11 = userRepository.findByUsername("a.jackson.tm11@progresso.com").orElseThrow();
      User tm12 = userRepository.findByUsername("a.white.tm12@progresso.com").orElseThrow();
      User tm13 = userRepository.findByUsername("d.harris.tm13@progresso.com").orElseThrow();
      User tm14 = userRepository.findByUsername("h.martin.tm14@progresso.com").orElseThrow();
      User tm15 = userRepository.findByUsername("m.thompson.tm15@progresso.com").orElseThrow();
      User tm16 = userRepository.findByUsername("e.garcia.tm16@progresso.com").orElseThrow();
      User tm17 = userRepository.findByUsername("d.martinez.tm17@progresso.com").orElseThrow();
      User tm18 = userRepository.findByUsername("a.robinson.tm18@progresso.com").orElseThrow();

      createTeamIfNotExists("Alpha Team", teamRepository, List.of(tm1, tm2, tm3));
      createTeamIfNotExists("Beta Team", teamRepository, List.of(tm4, tm5, tm6));
      createTeamIfNotExists("Gamma Team", teamRepository, List.of(tm7, tm8, tm9));
      createTeamIfNotExists("Delta Team", teamRepository, List.of(tm10, tm11, tm12));
      createTeamIfNotExists("Epsilon Team", teamRepository, List.of(tm13, tm14, tm15));
      createTeamIfNotExists("Zeta Team", teamRepository, List.of(tm16, tm17, tm18));

      userRepository.saveAll(List.of(
          tm1, tm2, tm3, tm4, tm5, tm6, tm7, tm8, tm9,
          tm10, tm11, tm12, tm13, tm14, tm15, tm16, tm17, tm18
      ));

      // Projects
      createProjectIfNotExists(
          projectRepository, userRepository, teamRepository,
          "Website Redesign",
          "Revamping the company website for better UX.",
          LocalDate.now().minusDays(3),
          LocalDate.now().plusDays(5),
          Priority.HIGH,
          "j.doe.pm1@progresso.com",
          "Alpha Team"
      );

      createProjectIfNotExists(
          projectRepository, userRepository, teamRepository,
          "Mobile App Development",
          "Developing a cross-platform mobile app.",
          LocalDate.now().minusDays(10),
          LocalDate.now().plusDays(20),
          Priority.MEDIUM,
          "e.stone.pm2@progresso.com",
          "Beta Team"
      );

      createProjectIfNotExists(
          projectRepository, userRepository, teamRepository,
          "Marketing Automation Setup",
          "Implementing automation tools for marketing campaigns.",
          LocalDate.now().minusDays(1),
          LocalDate.now().plusDays(40),
          Priority.LOW,
          "m.taylor.pm3@progresso.com",
          "Gamma Team"
      );

      createProjectIfNotExists(
          projectRepository, userRepository, teamRepository,
          "Data Migration",
          "Migrating legacy data to the new system.",
          LocalDate.now().minusDays(2),
          LocalDate.now().plusDays(28),
          Priority.MEDIUM,
          "j.doe.pm1@progresso.com",
          "Delta Team"
      );

      createProjectIfNotExists(
          projectRepository, userRepository, teamRepository,
          "Cloud Infrastructure Setup",
          "Setting up scalable cloud infrastructure.",
          LocalDate.now(),
          LocalDate.now().plusDays(6),
          Priority.HIGH,
          "e.stone.pm2@progresso.com",
          "Epsilon Team"
      );

      createProjectIfNotExists(
          projectRepository, userRepository, teamRepository,
          "Customer Feedback Integration",
          "Integrating feedback collection tools into the platform.",
          LocalDate.now().minusDays(20),
          LocalDate.now().plusDays(10),
          Priority.MEDIUM,
          "m.taylor.pm3@progresso.com",
          "Zeta Team"
      );

      // Tasks
      for (Project project : projectRepository.findAll()) {
        String projectName = project.getName();

        Page<User> membersPage = userRepository.findUsersByTeamId(
            project.getTeam().getId(), null, Pageable.unpaged()
        );
        List<User> teamMembers = membersPage.getContent();

        // Design Phase
        createRandomizedTask(taskRepository,
            projectName + " - Design Phase",
            "Initial design and planning.",
            Priority.HIGH,
            project.getStartDate(),
            project.getStartDate().plusDays(3),
            project,
            teamMembers.get(0)
        );

        // Development Phase
        createRandomizedTask(taskRepository,
            projectName + " - Development Phase",
            "Core feature implementation.",
            Priority.MEDIUM,
            project.getStartDate().plusDays(4),
            project.getStartDate().plusDays(10),
            project,
            teamMembers.get(1)
        );

        // Testing & Deployment
        createRandomizedTask(taskRepository,
            projectName + " - Testing & Deployment",
            "Testing and final deployment.",
            Priority.LOW,
            project.getStartDate().plusDays(11),
            project.getDueDate(),
            project,
            teamMembers.get(2)
        );
      }

      //Comments
      for (Project project : projectRepository.findAll()) {

        List<User> teamMembers = userRepository.findUsersByTeamId(
            project.getTeam().getId(), null, Pageable.unpaged()
        ).getContent();

        User projectManager = project.getProjectManager();

        createCommentsForProject(project, teamMembers, projectManager, commentRepository);
      }
    };
  }

  private void createUserIfNotExists(
      UserRepository userRepository,
      PasswordEncoder encoder,
      String firstName,
      String lastName,
      String username,
      String rawPassword,
      Role role) {
    if (userRepository.findByUsername(username).isEmpty()) {
      User user = new User();
      user.setFirstName(firstName);
      user.setLastName(lastName);
      user.setUsername(username);
      user.setPassword(encoder.encode(rawPassword));
      user.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com");
      user.setRole(role);
      user.setActive(true);
      user.setBirthDate(LocalDate.of(1995, 1, 1));
      user.setCity("City");
      user.setCountry("Country");
      user.setPhoneNumber("+1 000 000 0000");
      user.setStateProvinceRegion("State");
      user.setStreetAddress("Street 123");
      user.setZipCode("00000");
      userRepository.save(user);
    }
  }

  private void createTeamIfNotExists(
      String name,
      TeamRepository teamRepository,
      List<User> members) {
    if (!teamRepository.existsByNameIgnoreCase(name)) {
      Team team = new Team();
      team.setName(name);
      team.setActive(true);
      team.setTeamMembers(members);

      teamRepository.save(team);

      for (User user : members) {
        user.getTeams().add(team);
      }
    }
  }

  private void createProjectIfNotExists(
      ProjectRepository projectRepository,
      UserRepository userRepository,
      TeamRepository teamRepository,
      String name,
      String description,
      LocalDate startDate,
      LocalDate dueDate,
      Priority priority,
      String projectManagerUsername,
      String teamName) {

    if (!projectRepository.existsByNameIgnoreCase(name)) {
      User projectManager = userRepository.findByUsername(projectManagerUsername)
          .orElseThrow(() -> new IllegalStateException(
              "Project manager not found: " + projectManagerUsername));

      Team team = teamRepository.findByNameIgnoreCase(teamName)
          .orElseThrow(() -> new IllegalStateException("Team not found: " + teamName));

      Project project = new Project();
      project.setName(name);
      project.setDescription(description);
      project.setStartDate(startDate);
      project.setDueDate(dueDate);
      project.setStatus(Status.IN_PROGRESS);
      project.setPriority(priority);
      project.setProjectManager(projectManager);
      project.setTeam(team);

      projectRepository.save(project);
    }
  }

  private void createRandomizedTask(
      TaskRepository taskRepository,
      String name,
      String description,
      Priority priority,
      LocalDate startDate,
      LocalDate dueDate,
      Project project,
      User assignedUser) {

    if (!taskRepository.existsByProjectIdAndName(project.getId(), name)) {
      LocalDate today = LocalDate.now();

      boolean canBeCompleted = !startDate.isAfter(today);
      boolean completed = canBeCompleted && ThreadLocalRandom.current().nextBoolean();
      Status status = completed ? Status.COMPLETED : Status.IN_PROGRESS;

      LocalDate completionDate = null;
      if (completed) {
        long daysBetween = dueDate.toEpochDay() - startDate.toEpochDay();
        if (daysBetween <= 0) {
          completionDate = dueDate;
        } else {
          long randomDays = ThreadLocalRandom.current().nextLong(0, daysBetween + 1);
          completionDate = startDate.plusDays(randomDays);
        }
      }

      Task task = new Task();
      task.setName(name);
      task.setDescription(description);
      task.setPriority(priority);
      task.setStartDate(startDate);
      task.setDueDate(dueDate);
      task.setCompletionDate(completionDate);
      task.setStatus(status);
      task.setProject(project);
      task.setAssignedUser(assignedUser);

      taskRepository.save(task);
    }
  }

  private void createCommentsForProject(
      Project project,
      List<User> teamMembers,
      User projectManager,
      CommentRepository commentRepository
  ) {
    if (teamMembers.size() < 3) {
      return;
    }

    LocalDateTime taskStart = project.getStartDate().atStartOfDay();
    LocalDateTime taskEnd = project.getDueDate().atTime(23, 59);

    Comment comment1 = createRandomComment(
        "Discussion on the design phase.",
        teamMembers.get(0),
        project,
        taskStart,
        taskEnd,
        commentRepository
    );

    Comment comment2 = createRandomComment(
        "Deadline definition and scheduling.",
        teamMembers.get(1),
        project,
        taskStart,
        taskEnd,
        commentRepository
    );

    Comment comment3 = createRandomComment(
        "Finalizing UI components.",
        teamMembers.get(2),
        project,
        taskStart,
        taskEnd,
        commentRepository
    );

    Comment[] parentComments = {comment1, comment2, comment3};
    Comment parentComment = parentComments[ThreadLocalRandom.current().nextInt(0, 3)];

    Comment reply = new Comment();
    reply.setContent("Thanks for the update. Let's discuss this tomorrow.");
    reply.setCreationDate(randomDateTimeBetween(taskStart, taskEnd));
    reply.setUser(projectManager);
    reply.setProject(project);
    reply.setParent(parentComment);
    reply.setModified(false);
    reply.setModifiedDate(null);
    reply.setDeleted(false);

    commentRepository.save(reply);
  }

  private Comment createRandomComment(
      String content,
      User user,
      Project project,
      LocalDateTime start,
      LocalDateTime end,
      CommentRepository commentRepository
  ) {
    Comment comment = new Comment();
    comment.setContent(content);
    comment.setCreationDate(randomDateTimeBetween(start, end));
    comment.setUser(user);
    comment.setProject(project);
    comment.setParent(null);
    comment.setModified(false);
    comment.setModifiedDate(null);
    comment.setDeleted(false);
    return commentRepository.save(comment);
  }

  private LocalDateTime randomDateTimeBetween(LocalDateTime startInclusive,
      LocalDateTime endExclusive) {
    long startEpochSecond = startInclusive.toEpochSecond(ZoneOffset.UTC);
    long endEpochSecond = endExclusive.toEpochSecond(ZoneOffset.UTC);
    long randomEpochSecond = ThreadLocalRandom.current().nextLong(startEpochSecond, endEpochSecond);
    return LocalDateTime.ofEpochSecond(randomEpochSecond, 0, ZoneOffset.UTC);
  }
}