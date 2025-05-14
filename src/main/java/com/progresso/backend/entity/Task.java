package com.progresso.backend.entity;

import com.progresso.backend.enumeration.Priority;
import com.progresso.backend.enumeration.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Priority priority;

  @Column(nullable = false)
  private LocalDate startDate;

  @Column(nullable = false)
  private LocalDate dueDate;

  private LocalDate completionDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Status status;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(nullable = false)
  private Project project;

  @ManyToOne(fetch = FetchType.EAGER)
  private User assignedUser;

  @Override
  public String toString() {
    return "Task{"
        + "id=" + id
        + ", name='" + name + '\''
        + ", description='" + description + '\''
        + ", priority=" + priority
        + ", startDate=" + startDate
        + ", dueDate=" + dueDate
        + ", completionDate=" + completionDate
        + ", status=" + status
        + ", project=" + (project != null ? "Project{id=" + project.getId() + "}" : "null")
        + ", assignedUser=" + (assignedUser != null ? "User{id=" + assignedUser.getId() + "}"
        : "null")
        + '}';
  }
}
