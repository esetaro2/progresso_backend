package com.progresso.backend.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserChangePasswordDto {

  private String currentPassword;

  @Pattern(
      regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$^&*()\\-_+=<>?\\[\\]{}|]).{8,}$",
      message = "Password must be at least 8 characters long, "
          + "contain at least one uppercase letter, "
          + "one lowercase letter, one digit, and one special character."
  )
  private String newPassword;

  private String confirmPassword;
}
