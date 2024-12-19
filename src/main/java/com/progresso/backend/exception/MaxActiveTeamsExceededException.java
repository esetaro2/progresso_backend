package com.progresso.backend.exception;

public class MaxActiveTeamsExceededException extends RuntimeException {

  public MaxActiveTeamsExceededException(String message) {
    super(message);
  }
}
