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

  @NotBlank(message = "First name cannot be empty. Please provide the first name.")
  @Size(max = 50, message = "The first name must be between 2 and 50 characters.")
  @Pattern(
      regexp = "^\\S[a-zA-ZÀ-ÿ\\s]+$",
      message = "First name cannot start with a space. It can only contain letters and spaces.")
  private String firstName;

  @NotBlank(message = "Last name cannot be empty. Please provide the last name.")
  @Size(max = 50, message = "Last name must be between 2 and 50 characters.")
  @Pattern(
      regexp = "^\\S[a-zA-ZÀ-ÿ\\s]+$",
      message = "Last name cannot start with a space. It can only contain letters and spaces.")
  private String lastName;

  @Past(message = "Birth date must be in the past.")
  @Age(message = "User must be at least 18 years old.")
  private LocalDate birthDate;

  @NotBlank(message = "Phone number cannot be empty. Please provide the phone number.")
  private String phoneNumber;

  @NotBlank(message = "Street address cannot be empty. Please provide the street address.")
  @Size(max = 100, message = "The street address must not exceed 100 characters.")
  @Pattern(
      regexp = "^\\S[a-zA-Z0-9\\s,.-]+$",
      message = "Street address cannot start with a space and must contain valid characters.")
  private String streetAddress;

  @NotBlank(message = "City cannot be empty. Please provide the city name.")
  @Size(max = 50, message = "The city name must not exceed 50 characters.")
  @Pattern(
      regexp = "^\\S[a-zA-ZÀ-ÿ\\s]+$",
      message = "City name cannot start with a space. It can only contain letters and spaces.")
  private String city;

  @NotBlank(message = "State/province/region cannot be empty. "
      + "Please provide the state/province/region.")
  @Size(max = 50, message = "State/province/region name must not exceed 50 characters.")
  @Pattern(
      regexp = "^\\S[a-zA-ZÀ-ÿ\\s]+$",
      message = "State/province/region name cannot start with a space. "
          + "It can only contain letters and spaces.")
  private String stateProvinceRegion;

  @NotBlank(message = "Country cannot be empty. Please provide the country name.")
  @Size(max = 50, message = "Country name must not exceed 50 characters.")
  @Pattern(
      regexp = "^\\S[a-zA-ZÀ-ÿ\\s]+$",
      message = "Country name cannot start with a space. It can only contain letters and spaces.")
  private String country;

  @NotBlank(message = "Zip code cannot be empty. Please provide the zip code.")
  @Pattern(
      regexp = "^[0-9]{5}(?:-[0-9]{4})?$",
      message = "Zip code must be in a valid US format (e.g., 12345 or 12345-6789).")
  private String zipCode;

  @NotBlank(message = "Email cannot be empty. Please provide the email address.")
  @Email(message = "Email must be a valid email address.")
  @Pattern(
      regexp = "^[a-zA-Z0-9._%+-]+@([a-zA-Z0-9.-]+\\.(com|org|net|edu|gov))$",
      message = "Email must belong to a valid domain such as example.com or domain.org.")
  private String email;

  @NotBlank(message = "Role cannot be empty. Please provide the role.")
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
