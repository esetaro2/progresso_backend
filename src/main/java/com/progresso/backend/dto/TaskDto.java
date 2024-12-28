package com.progresso.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {

  private Long id;

  @NotEmpty(message = "Task name cannot be empty")
  @Size(max = 100, message = "Task name must be between 1 and 100 characters")
  private String name;

  @NotEmpty(message = "Task description cannot be empty")
  @Size(max = 255, message = "Task description must be between 1 and 255 characters")
  private String description;

  @NotNull(message = "Priority cannot be null")
  private String priority;

  @NotNull(message = "Start date cannot be null")
  private LocalDate startDate;

  @NotNull(message = "Due date cannot be null")
  private LocalDate dueDate;

  private LocalDate completionDate;

  @NotNull(message = "Status cannot be null")
  private String status;

  @NotNull(message = "Project ID cannot be null")
  private Long projectId;

  private Long assignedUserId;
}
