package com.progresso.backend.exception;

public class TaskNameAlreadyExistsException extends RuntimeException {

  public TaskNameAlreadyExistsException(String message) {
    super(message);
  }
}
