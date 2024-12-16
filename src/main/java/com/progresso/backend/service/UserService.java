package com.progresso.backend.service;

import com.progresso.backend.dto.UserResponseDto;
import com.progresso.backend.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  public UserResponseDto convertToDtoToken(User user, String token) {
    UserResponseDto dto = new UserResponseDto();
    dto.setFirstName(user.getFirstName());
    dto.setLastName(user.getLastName());
    dto.setUsername(user.getUsername());
    dto.setRole(user.getRole().getName());
    dto.setToken(token);
    return dto;
  }

  public UserResponseDto convertToDto(User user) {
    UserResponseDto dto = new UserResponseDto();
    dto.setFirstName(user.getFirstName());
    dto.setLastName(user.getLastName());
    dto.setUsername(user.getUsername());
    dto.setRole(user.getRole().getName());
    return dto;
  }

}
