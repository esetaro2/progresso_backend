package com.progresso.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

  private Long id;

  @NotBlank(message = "Content cannot be empty.")
  @Size(max = 500, message = "Content must be between 1 and 500 characters.")
  private String content;

  private LocalDateTime creationDate;

  @NotNull(message = "User ID cannot be null")
  private Long userId;

  @NotNull(message = "Project ID cannot be null.")
  private Long projectId;

  private Long parentId;

  private List<CommentDto> replies;

  private Boolean modified;

  private LocalDateTime modifiedDate;

  private Boolean deleted;
}
