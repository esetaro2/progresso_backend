package com.progresso.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserLoginDto {

  @NotEmpty(message = "Username cannot be empty.")
  @Pattern(
      regexp = "^[a-zA-Z]\\.[a-zA-Z]+\\.(am|pm|tm)[0-9]+@progresso\\.com$",
      message = "Username must follow the format: "
          + "fnameFirstInitial.lname.role.OCCURRENCY@progresso.com"
  )
  private String username;


  @NotEmpty(message = "Password cannot be empty.")
  @Size(min = 8, max = 8, message = "The password must be 8 characters long.")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)"
          + "(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8}$",
      message = "The password must contain at least one uppercase letter, "
          + "one lowercase letter, one digit, and one special character.")
  private String password;
}
