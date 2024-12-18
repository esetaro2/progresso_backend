package com.progresso.backend.exception;

public class TeamMemberAlreadyActiveException extends RuntimeException {

  public TeamMemberAlreadyActiveException(String message) {
    super(message);
  }
}
