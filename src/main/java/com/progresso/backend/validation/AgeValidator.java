package com.progresso.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;

public class AgeValidator implements ConstraintValidator<Age, LocalDate> {

  @Override
  public void initialize(Age constraintAnnotation) {
  }

  @Override
  public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
    if (birthDate == null) {
      return false;
    }

    int age = Period.between(birthDate, LocalDate.now()).getYears();
    return age >= 17;
  }
}
