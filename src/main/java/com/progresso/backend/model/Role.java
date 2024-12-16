package com.progresso.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Role {

  public static final String ROLE_ADMIN = "admin";
  public static final String ROLE_PROJECT_MANAGER = "projectManager";
  public static final String ROLE_TEAM_MEMBER = "teamMember";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @NotEmpty(message = "The name must not be empty.")
  @Column(nullable = false, unique = true)
  private String name;

  @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<User> users = new HashSet<>();

  public Role() {
  }

  public Role(String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<User> getUsers() {
    return users;
  }

  public void setUsers(Set<User> users) {
    this.users = users;
  }

  @Override
  public String toString() {
    return "Role{"
        + "id=" + id
        + ", name='" + name + '\''
        + '}';
  }
}
