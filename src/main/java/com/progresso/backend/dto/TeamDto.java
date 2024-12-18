package com.progresso.backend.dto;

import com.progresso.backend.model.Team;
import com.progresso.backend.model.User;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class TeamDto {

  @NotEmpty(message = "Name cannot be empty.")
  @Size(max = 100, message = "Name cannot exceed 100 characters.")
  private String name;

  @NotNull(message = "Is Active cannot be null.")
  private Boolean isActive;

  @NotNull(message = "Project Manager ID cannot be null.")
  private Long projectManagerId;

  private List<Long> memberIds;

  public Team toEntity(User projectManager) {
    Team team = new Team();
    team.setName(this.name);
    team.setIsActive(this.isActive);
    team.setProjectManager(projectManager);
    return team;
  }
}
