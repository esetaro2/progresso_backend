package com.progresso.backend.exception;

public class TeamNameAlreadyExistsException extends RuntimeException {

  public TeamNameAlreadyExistsException(String message) {
    super(message);
  }
}
