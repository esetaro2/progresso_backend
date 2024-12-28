package com.progresso.backend.model;

import com.progresso.backend.enumeration.Priority;
import com.progresso.backend.enumeration.Status;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String description;

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
  private User projectManager;

  @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Task> tasks;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(nullable = false)
  private Team team;
}
