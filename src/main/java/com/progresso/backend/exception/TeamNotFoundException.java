package com.progresso.backend.exception;

public class TeamNotFoundException extends RuntimeException {

  public TeamNotFoundException(String message) {
    super(message);
  }
}
