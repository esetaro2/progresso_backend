package com.progresso.backend.service;

import com.progresso.backend.dto.UserResponseDto;
import com.progresso.backend.model.Team;
import com.progresso.backend.model.User;
import java.util.ArrayList;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private UserResponseDto convertToDtoCommon(User user) {
    UserResponseDto dto = new UserResponseDto();
    dto.setFirstName(user.getFirstName());
    dto.setLastName(user.getLastName());
    dto.setUsername(user.getUsername());
    dto.setRole(user.getRole().toString());
    dto.setManagedTeamIds(
        user.getManagedTeams() != null ? user.getManagedTeams().stream().map(Team::getId).toList()
            : new ArrayList<>());
    dto.setTeamIds(user.getTeamMemberships() != null ? user.getTeamMemberships().stream()
        .map(tm -> tm.getTeam().getId()).toList() : new ArrayList<>());
    return dto;
  }

  public UserResponseDto convertToDtoToken(User user, String token) {
    UserResponseDto dto = convertToDtoCommon(user);
    dto.setToken(token);
    return dto;
  }

  public UserResponseDto convertToDto(User user) {
    return convertToDtoCommon(user);
  }
}

