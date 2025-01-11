package com.progresso.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

  private Long id;

  private String firstName;

  private String lastName;

  private String username;

  private String role;

  private List<Long> assignedTaskIds;

  private List<Long> managedProjectIds;

  private List<Long> teamIds;

  private List<Long> commentIds;

  private Boolean active;

  private LocalDateTime deactivatedAt;
}
