package com.progresso.backend.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberDto {

  private Long id;
  private Long userId;
  private String username;
  private Long teamId;
  private LocalDate joinDate;
  private LocalDate removeDate;
  private Boolean isActive;
}
