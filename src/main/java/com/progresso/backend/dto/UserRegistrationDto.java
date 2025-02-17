package com.progresso.backend.dto;

import com.progresso.backend.enumeration.Role;
import com.progresso.backend.model.User;
import com.progresso.backend.validation.Age;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {

  @NotBlank(message = "The first name cannot be empty.")
  @Size(max = 50, message = "The first name must be between 2 and 50 characters.")
  @Pattern(
      regexp = "^[a-zA-ZÀ-ÿ\\s]+$",
      message = "The first name can only contain letters and spaces.")
  private String firstName;

  @NotBlank(message = "The last name cannot be empty.")
  @Size(max = 50, message = "The last name must be between 2 and 50 characters.")
  @Pattern(
      regexp = "^[a-zA-ZÀ-ÿ\\s]+$",
      message = "The last name can only contain letters and spaces.")
  private String lastName;

  @Past(message = "The birth date must be in the past.")
  @Age(message = "User must be at least 18 years old.")
  private LocalDate birthDate;

  @NotBlank(message = "The phone number cannot be empty.")
  private String phoneNumber;

  @NotBlank(message = "The street address cannot be empty.")
  @Size(max = 100, message = "The street address must not exceed 100 characters.")
  @Pattern(
      regexp = "^[a-zA-Z0-9\\s,.-]+$",
      message = "The street address contains invalid characters.")
  private String streetAddress;

  @NotBlank(message = "The city cannot be empty.")
  @Size(max = 50, message = "The city name must not exceed 50 characters.")
  @Pattern(
      regexp = "^[a-zA-ZÀ-ÿ\\s]+$",
      message = "The city name can only contain letters and spaces.")
  private String city;

  @NotBlank(message = "The state/province/region cannot be empty.")
  @Size(max = 50, message = "The state/province/region name must not exceed 50 characters.")
  @Pattern(
      regexp = "^[a-zA-ZÀ-ÿ\\s]+$",
      message = "The state/province/region name can only contain letters and spaces.")
  private String stateProvinceRegion;

  @NotBlank(message = "The country cannot be empty.")
  @Size(max = 50, message = "The country name must not exceed 50 characters.")
  @Pattern(
      regexp = "^[a-zA-ZÀ-ÿ\\s]+$",
      message = "The country name can only contain letters and spaces.")
  private String country;

  @NotBlank(message = "The zip code cannot be empty.")
  @Pattern(
      regexp = "^[0-9]{5}(?:-[0-9]{4})?$",
      message = "The zip code must be in a valid US format (e.g., 12345 or 12345-6789).")
  private String zipCode;

  @NotBlank(message = "The email cannot be empty.")
  @Email
  @Pattern(
      regexp = "^[a-zA-Z0-9._%+-]+@([a-zA-Z0-9.-]+\\.(com|org|net|edu|gov))$",
      message = "Email must belong to a valid domain such as example.com or domain.org.")
  private String email;

  @NotBlank(message = "The role cannot be empty.")
  private String role;

  public User toEntity(User user) {
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setBirthDate(birthDate);
    user.setPhoneNumber(phoneNumber);
    user.setStreetAddress(streetAddress);
    user.setCity(city);
    user.setStateProvinceRegion(stateProvinceRegion);
    user.setCountry(country);
    user.setZipCode(zipCode);
    user.setEmail(email);
    user.setRole(Role.valueOf(role.toUpperCase()));
    return user;
  }
}