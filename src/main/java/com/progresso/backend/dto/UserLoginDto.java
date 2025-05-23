package com.progresso.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserLoginDto {

  @NotBlank(message = "Username cannot be empty.")
  @Pattern(
      regexp = "^[a-zA-Z]\\.[a-zA-Z_]+\\.(am|pm|tm)[0-9]+@progresso\\.com$",
      message = "Username must follow the format: "
          + "firstInitial.lastname(_if_composed).roleOccurrence@progresso.com"
  )
  private String username;

  @NotBlank(message = "Password cannot be empty.")
  @Size(min = 8, message = "Password must be at least 8 characters long.")
  private String password;
}
