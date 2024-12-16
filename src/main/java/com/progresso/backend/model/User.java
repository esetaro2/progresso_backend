package com.progresso.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;

@Entity
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = false)
  private String lastName;

  @Column(nullable = false)
  private LocalDate birthDate;

  @Column(nullable = false)
  private String phoneNumber;

  @Column(nullable = false)
  private String streetAddress;

  @Column(nullable = false)
  private String city;

  @Column(nullable = false)
  private String stateProvinceRegion;

  @Column(nullable = false)
  private String country;

  @Column(nullable = false)
  private String zipCode;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String password;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "role_id", nullable = false)
  private Role role;

  public User() {
  }

  public User(String firstName, String lastName, LocalDate birthDate, String phoneNumber,
      String streetAddress, String city, String stateProvinceRegion, String country,
      String zipCode, String email, String username, String password, Role role) {
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
    this.username = username;
    this.password = password;
    this.role = role;
  }

  public Long getId() {
    return id;
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

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  @Override
  public String toString() {
    return "User{"
        + "id=" + id
        + ", firstName='" + firstName + '\''
        + ", lastName='" + lastName + '\''
        + ", birthDate=" + birthDate
        + ", phoneNumber='" + phoneNumber + '\''
        + ", streetAddress='" + streetAddress + '\''
        + ", city='" + city + '\''
        + ", stateProvinceRegion='" + stateProvinceRegion + '\''
        + ", country='" + country + '\''
        + ", zipCode='" + zipCode + '\''
        + ", email='" + email + '\''
        + ", username='" + username + '\''
        + ", password='" + password + '\''
        + ", role=" + role
        + '}';
  }
}
