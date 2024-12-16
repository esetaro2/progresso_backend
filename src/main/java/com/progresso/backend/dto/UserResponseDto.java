package com.progresso.backend.dto;

public class UserResponseDto {

  private String firstName;

  private String lastName;

  private String username;

  private String role;

  private String token;

  public UserResponseDto() {
  }

  public UserResponseDto(String firstName, String lastName, String username, String role,
      String token) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.username = username;
    this.role = role;
    this.token = token;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  @Override
  public String toString() {
    return "UserResponseDto{"
        + "firstName='" + firstName + '\''
        + ", lastName='" + lastName + '\''
        + ", username='" + username + '\''
        + ", role='" + role + '\''
        + ", token='" + token + '\''
        + '}';
  }
}
