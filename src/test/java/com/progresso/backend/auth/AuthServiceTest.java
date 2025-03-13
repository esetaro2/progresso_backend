package com.progresso.backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.progresso.backend.dto.UserRegistrationDto;
import com.progresso.backend.dto.UserResponseDto;
import com.progresso.backend.enumeration.Role;
import com.progresso.backend.exception.EmailAlreadyExistsException;
import com.progresso.backend.model.User;
import com.progresso.backend.user.UserRepository;
import com.progresso.backend.security.PasswordGenerator;
import com.progresso.backend.user.UserService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

  @InjectMocks
  private AuthService authService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private UserService userService;

  @Test
  void registerUser_EmailAlreadyExists() {
    UserRegistrationDto dto = mock(UserRegistrationDto.class);
    when(dto.getEmail()).thenReturn("existing@example.com");

    when(userRepository.findByEmail("existing@example.com"))
        .thenReturn(Optional.of(new User()));

    assertThrows(EmailAlreadyExistsException.class, () -> authService.registerUser(dto));

    verify(userRepository, times(1)).findByEmail("existing@example.com");
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void registerUser_Success() {
    UserRegistrationDto dto = new UserRegistrationDto();
    dto.setFirstName("Alice");
    dto.setLastName("Smith");
    dto.setBirthDate(LocalDate.of(1990, 1, 1));
    dto.setPhoneNumber("+1 123 456 7890");
    dto.setStreetAddress("123 Main St.");
    dto.setCity("Springfield");
    dto.setStateProvinceRegion("IL");
    dto.setCountry("USA");
    dto.setZipCode("12345");
    dto.setEmail("alice@example.com");
    dto.setRole("TEAMMEMBER");

    when(userRepository.findByEmail("alice@example.com"))
        .thenReturn(Optional.empty());
    when(userRepository.countByRole(Role.TEAMMEMBER)).thenReturn(0);

    try (MockedStatic<PasswordGenerator> mockedPasswordGen = Mockito.mockStatic(
        PasswordGenerator.class)) {
      String generatedPassword = "plainPassword";
      mockedPasswordGen.when(PasswordGenerator::generateSecurePassword)
          .thenReturn(generatedPassword);

      when(passwordEncoder.encode(generatedPassword)).thenReturn("encodedPassword");

      when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
        User user = invocation.getArgument(0);
        user.setId(1L);
        return user;
      });

      UserResponseDto responseDto = getUserResponseDto();
      when(userService.convertToDto(any(User.class))).thenReturn(responseDto);

      UserResponseDto result = authService.registerUser(dto);

      assertNotNull(result);
      assertEquals(1L, result.getId());
      assertEquals("Alice", result.getFirstName());
      assertEquals("Smith", result.getLastName());
      assertEquals("a.smith.tm1@progresso.com", result.getUsername());
      assertEquals("TEAMMEMBER", result.getRole());
      assertTrue(result.getAssignedTaskIds().isEmpty());
      assertTrue(result.getManagedProjectIds().isEmpty());
      assertTrue(result.getTeamIds().isEmpty());
      assertTrue(result.getCommentIds().isEmpty());
      assertTrue(result.getActive());

      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      User savedUser = userCaptor.getValue();

      assertEquals("Alice", savedUser.getFirstName());
      assertEquals("Smith", savedUser.getLastName());
      assertEquals(LocalDate.of(1990, 1, 1), savedUser.getBirthDate());
      assertEquals("+1 123 456 7890", savedUser.getPhoneNumber());
      assertEquals("123 Main St.", savedUser.getStreetAddress());
      assertEquals("Springfield", savedUser.getCity());
      assertEquals("IL", savedUser.getStateProvinceRegion());
      assertEquals("USA", savedUser.getCountry());
      assertEquals("12345", savedUser.getZipCode());
      assertEquals("alice@example.com", savedUser.getEmail());
      assertEquals(Role.TEAMMEMBER, savedUser.getRole());

      assertTrue(savedUser.getActive());
      assertEquals("encodedPassword", savedUser.getPassword());
      assertEquals("a.smith.tm1@progresso.com", savedUser.getUsername());

      verify(userRepository, times(1)).findByEmail("alice@example.com");
      verify(userRepository, times(1)).countByRole(Role.TEAMMEMBER);
      verify(passwordEncoder, times(1)).encode(generatedPassword);
      verify(userService, times(1)).convertToDto(any(User.class));
    }
  }

  private static UserResponseDto getUserResponseDto() {
    UserResponseDto responseDto = new UserResponseDto();
    responseDto.setId(1L);
    responseDto.setFirstName("Alice");
    responseDto.setLastName("Smith");
    responseDto.setUsername("a.smith.tm1@progresso.com");
    responseDto.setRole("TEAMMEMBER");
    responseDto.setAssignedTaskIds(Collections.emptyList());
    responseDto.setManagedProjectIds(Collections.emptyList());
    responseDto.setTeamIds(Collections.emptyList());
    responseDto.setCommentIds(Collections.emptyList());
    responseDto.setActive(true);
    return responseDto;
  }
}
