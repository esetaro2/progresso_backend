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
          + "fnameFirstInitial.lname.roleOccurency@progresso.com"
  )
  private String username;


  @NotEmpty(message = "Password cannot be empty.")
  @Size(min = 8, message = "The password must be at least 8 characters long.")
  private String password;
}
