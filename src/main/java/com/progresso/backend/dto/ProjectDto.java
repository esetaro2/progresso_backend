package com.progresso.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

  @NotBlank(message = "Project name cannot be empty. Please provide a name.")
  @Size(max = 100, message = "Project name is too long. It must be between 1 and 100 characters.")
  @Pattern(regexp = "^\\S.*", message = "Project name must not start with a space. "
      + "Please remove the leading space.")
  private String name;

  @NotBlank(message = "Project description cannot be empty. Please provide a description.")
  @Size(max = 255, message = "Project description is too long. "
      + "It must be between 1 and 255 characters.")
  @Pattern(regexp = "^\\S.*", message = "Project description must not start with a space. "
      + "Please remove the leading space.")
  private String description;

  private String priority;

  @NotNull(message = "Project start date cannot be null. Please specify the start date.")
  private LocalDate startDate;

  @NotNull(message = "Project due date cannot be null. Please specify the due date.")
  private LocalDate dueDate;

  private LocalDate completionDate;

  private Long completionPercentage;

  private String status;

  @NotNull(message = "Project manager ID cannot be null. Please specify the project manager ID.")
  private Long projectManagerId;

  private String projectManagerFirstName;

  private String projectManagerLastName;

  private String projectManagerUsername;

  private List<Long> taskIds;

  private Long teamId;

  private String teamName;

  private List<Long> commentIds;
}
