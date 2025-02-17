package com.progresso.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {

  private Long id;

  @NotBlank(message = "Project name cannot be empty")
  @Size(max = 100, message = "Project name must be between 1 and 100 characters")
  private String name;

  @NotBlank(message = "Project description cannot be empty")
  @Size(max = 255, message = "Project description must be between 1 and 255 characters")
  private String description;

  private String priority;

  @NotNull(message = "Start date cannot be null")
  private LocalDate startDate;

  @NotNull(message = "Due date cannot be null")
  private LocalDate dueDate;

  private LocalDate completionDate;

  private String status;

  @NotNull(message = "Project manager ID cannot be null")
  private Long projectManagerId;

  private List<Long> taskIds;

  private Long teamId;

  private List<Long> commentIds;
}