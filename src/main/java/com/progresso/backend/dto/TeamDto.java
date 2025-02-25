package com.progresso.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class TeamDto {

  private Long id;

  @NotBlank(message = "Team name cannot be empty. Please provide a name.")
  @Size(max = 100, message = "Team name is too long. It must be between 1 and 100 characters.")
  @Pattern(regexp = "^\\S.*", message = "Name must not start with a space. "
      + "Please remove the leading space.")
  private String name;

  private Boolean active;

  private List<Long> teamMemberIds;

  private List<Long> projectIds;
}
