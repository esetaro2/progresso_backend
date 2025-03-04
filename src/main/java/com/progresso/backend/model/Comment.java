package com.progresso.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 500)
  private String content;

  @Column(nullable = false)
  private LocalDateTime creationDate;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(nullable = false)
  private Project project;

  @ManyToOne(fetch = FetchType.EAGER)
  private Comment parent;

  @Column(nullable = false)
  private Boolean modified;

  private LocalDateTime modifiedDate;

  @Column(nullable = false)
  private Boolean deleted;

  @Override
  public String toString() {
    return "Comment{"
        + "id=" + id
        + ", content='" + content + '\''
        + ", creationDate=" + creationDate
        + ", user=" + (user != null ? "User{id=" + user.getId() + "}" : "null")
        + ", project=" + (project != null ? "Project{id=" + project.getId() + "}" : "null")
        + ", parent=" + (parent != null ? "Comment{id=" + parent.getId() + "}" : "null")
        + ", modified=" + modified
        + ", modifiedDate=" + modifiedDate
        + ", deleted=" + deleted
        + '}';
  }
}
