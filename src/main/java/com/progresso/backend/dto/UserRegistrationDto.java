package com.progresso.backend.dto;

import com.progresso.backend.enumeration.RoleType;
import com.progresso.backend.model.User;
import com.progresso.backend.validation.Age;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
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

  @NotEmpty(message = "The first name cannot be empty.")
  @Size(max = 50, message = "The first name must be between 2 and 50 characters.")
  @Pattern(
      regexp = "^[a-zA-ZÀ-ÿ\\s]+$",
      message = "The first name can only contain letters and spaces.")
  private String firstName;

  @NotEmpty(message = "The last name cannot be empty.")
  @Size(max = 50, message = "The last name must be between 2 and 50 characters.")
  @Pattern(
      regexp = "^[a-zA-ZÀ-ÿ\\s]+$",
      message = "The last name can only contain letters and spaces.")
  private String lastName;

  @Past(message = "The birth date must be in the past.")
  @Age(message = "User must be at least 18 years old.")
  private LocalDate birthDate;

  @NotEmpty(message = "The phone number cannot be empty.")
  @Pattern(
      regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10}$",
      message = "The phone number must be in a valid international format.")
  private String phoneNumber;

  @NotEmpty(message = "The street address cannot be empty.")
  @Size(max = 100, message = "The street address must not exceed 100 characters.")
  @Pattern(
      regexp = "^[a-zA-Z0-9\\s,.-]+$",
      message = "The street address contains invalid characters.")
  private String streetAddress;

  @NotEmpty(message = "The city cannot be empty.")
  @Size(max = 50, message = "The city name must not exceed 50 characters.")
  @Pattern(
      regexp = "^[a-zA-ZÀ-ÿ\\s]+$",
      message = "The city name can only contain letters and spaces.")
  private String city;

  @NotEmpty(message = "The state/province/region cannot be empty.")
  @Size(max = 50, message = "The state/province/region name must not exceed 50 characters.")
  @Pattern(
      regexp = "^[a-zA-ZÀ-ÿ\\s]+$",
      message = "The state/province/region name can only contain letters and spaces.")
  private String stateProvinceRegion;

  @NotEmpty(message = "The country cannot be empty.")
  @Size(max = 50, message = "The country name must not exceed 50 characters.")
  @Pattern(
      regexp = "^[a-zA-ZÀ-ÿ\\s]+$",
      message = "The country name can only contain letters and spaces.")
  private String country;

  @NotEmpty(message = "The zip code cannot be empty.")
  @Pattern(
      regexp = "^[0-9]{5}(?:-[0-9]{4})?$",
      message = "The zip code must be in a valid US format (e.g., 12345 or 12345-6789).")
  private String zipCode;

  @NotEmpty(message = "The email cannot be empty.")
  @Email
  @Pattern(
      regexp = "^[a-zA-Z0-9._%+-]+@([a-zA-Z0-9.-]+\\.(com|org|net|edu|gov))$",
      message = "Email must belong to a valid domain such as example.com or domain.org.")
  private String email;

  @NotEmpty(message = "The role cannot be empty.")
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
    user.setRole(RoleType.valueOf(role.toUpperCase()));
    return user;
  }
}