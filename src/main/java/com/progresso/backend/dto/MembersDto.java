package com.progresso.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class MembersDto {

  @NotEmpty(message = "Team must have at least one member.")
  private List<Long> memberIds;
}
