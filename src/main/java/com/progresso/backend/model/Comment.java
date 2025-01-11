package com.progresso.backend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.List;
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

  @Column(nullable = false)
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

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<Comment> replies;

  @Column(nullable = false)
  private Boolean modified;

  private LocalDateTime modifiedDate;

  @Column(nullable = false)
  private Boolean deleted;
}
