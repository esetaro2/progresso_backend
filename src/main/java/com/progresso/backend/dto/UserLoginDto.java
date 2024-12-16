package com.progresso.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserLoginDto {

  @NotEmpty(message = "Username cannot be empty.")
  @Pattern(
      regexp = "^[a-zA-Z]+\\.[a-zA-Z]+\\"
          + ".(admin|projectManager|teamMember)\\.[0-9]+@progresso\\.com$",
      message = "Username must follow the format: fname.lname.role.OCCURRENCY@progresso.com"
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

  public UserLoginDto() {
  }

  public UserLoginDto(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public String toString() {
    return "UserLoginDto{"
        + "username='" + username + '\''
        + ", password='" + password + '\''
        + '}';
  }
}
