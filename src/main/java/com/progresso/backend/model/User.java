package com.progresso.backend.model;

import com.progresso.backend.enumeration.Role;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
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
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = false)
  private String lastName;

  @Column(nullable = false)
  private LocalDate birthDate;

  @Column(nullable = false)
  private String phoneNumber;

  @Column(nullable = false)
  private String streetAddress;

  @Column(nullable = false)
  private String city;

  @Column(nullable = false)
  private String stateProvinceRegion;

  @Column(nullable = false)
  private String country;

  @Column(nullable = false)
  private String zipCode;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column(nullable = false)
  private Boolean active;

  @ManyToMany(fetch = FetchType.EAGER)
  private List<Team> teams;

  @OneToMany(mappedBy = "assignedUser", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  List<Task> assignedTasks;

  @OneToMany(mappedBy = "projectManager", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<Project> managedProjects;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<Comment> comments;
}
