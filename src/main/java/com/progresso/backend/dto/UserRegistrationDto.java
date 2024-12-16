package com.progresso.backend.dto;

import com.progresso.backend.validation.Age;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

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

  public UserRegistrationDto() {
  }

  public UserRegistrationDto(String firstName, String lastName, LocalDate birthDate,
      String phoneNumber, String streetAddress, String city, String stateProvinceRegion,
      String country, String zipCode, String email, String role) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.birthDate = birthDate;
    this.phoneNumber = phoneNumber;
    this.streetAddress = streetAddress;
    this.city = city;
    this.stateProvinceRegion = stateProvinceRegion;
    this.country = country;
    this.zipCode = zipCode;
    this.email = email;
    this.role = role;
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

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getStreetAddress() {
    return streetAddress;
  }

  public void setStreetAddress(String streetAddress) {
    this.streetAddress = streetAddress;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getStateProvinceRegion() {
    return stateProvinceRegion;
  }

  public void setStateProvinceRegion(String stateProvinceRegion) {
    this.stateProvinceRegion = stateProvinceRegion;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getZipCode() {
    return zipCode;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  @Override
  public String toString() {
    return "UserRegistrationDto{"
        +
        "firstName='" + firstName + '\''
        + ", lastName='" + lastName + '\''
        + ", birthDate=" + birthDate
        + ", phoneNumber='" + phoneNumber + '\''
        + ", streetAddress='" + streetAddress + '\''
        + ", city='" + city + '\''
        + ", stateProvinceRegion='" + stateProvinceRegion + '\''
        + ", country='" + country + '\''
        + ", zipCode='" + zipCode + '\''
        + ", email='" + email + '\''
        + ", role=" + role
        + '}';
  }
}