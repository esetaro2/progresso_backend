package com.progresso.backend.security;

import java.security.SecureRandom;

public class PasswordGenerator {

  private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
  private static final String DIGITS = "0123456789";
  private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_+=<>?[]{}|";

  private static final int MIN_PASSWORD_LENGTH = 12;

  private static final SecureRandom random = new SecureRandom();

  public static String generateSecurePassword() {
    StringBuilder password = new StringBuilder();

    password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
    password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
    password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
    password.append(SPECIAL_CHARACTERS.charAt(random.nextInt(SPECIAL_CHARACTERS.length())));

    String allCharacters = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARACTERS;
    for (int i = password.length(); i < MIN_PASSWORD_LENGTH; i++) {
      password.append(allCharacters.charAt(random.nextInt(allCharacters.length())));
    }

    return shuffleString(password.toString());
  }

  private static String shuffleString(String input) {
    char[] characters = input.toCharArray();
    for (int i = 0; i < characters.length; i++) {
      int randomIndex = random.nextInt(characters.length);
      char temp = characters[i];
      characters[i] = characters[randomIndex];
      characters[randomIndex] = temp;
    }
    return new String(characters);
  }
}
