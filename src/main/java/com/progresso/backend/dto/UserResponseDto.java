package com.progresso.backend.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

  private String firstName;
  private String lastName;
  private String username;
  private String role;
  private String token;
  private List<Long> managedTeamIds;
  private List<Long> teamIds;
}
