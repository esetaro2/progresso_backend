package com.progresso.backend.exception;

public class UserNotActiveException extends RuntimeException {

  public UserNotActiveException(String message) {
    super(message);
  }
}
