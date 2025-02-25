package com.progresso.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

  @NotBlank(message = "Task name cannot be empty. Please provide a name.")
  @Size(max = 100, message = "Task name is too long. It must be between 1 and 100 characters.")
  @Pattern(regexp = "^\\S.*", message = "Task name must not start with a space. "
      + "Please remove the leading space.")
  private String name;

  @NotBlank(message = "Task description cannot be empty. Please provide a description.")
  @Size(max = 255, message = "Task description is too long. "
      + "It must be between 1 and 255 characters.")
  @Pattern(regexp = "^\\S.*", message = "Task description must not start with a space. "
      + "Please remove the leading space.")
  private String description;

  @NotNull(message = "Priority cannot be null. Please specify the priority.")
  private String priority;

  @NotNull(message = "Start date cannot be null. Please specify the start date.")
  private LocalDate startDate;

  @NotNull(message = "Due date cannot be null. Please specify the due date.")
  private LocalDate dueDate;

  private LocalDate completionDate;

  private String status;

  @NotNull(message = "Project ID cannot be null. Please specify the project ID.")
  private Long projectId;

  private Long assignedUserId;

  private String assignedUserUsername;
}
