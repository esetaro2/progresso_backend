package com.progresso.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class TeamDto {

  private Long id;

  @NotBlank(message = "Name cannot be empty.")
  @Size(max = 100, message = "Name cannot exceed 100 characters.")
  private String name;

  private Boolean active;

  private List<Long> teamMemberIds;

  private List<Long> projectIds;
}
