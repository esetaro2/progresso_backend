package com.progresso.backend.security;

import com.progresso.backend.exception.UserNotFoundException;
import com.progresso.backend.model.User;
import com.progresso.backend.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  private final SecretKey key;
  private final UserRepository userRepository;

  public JwtUtil(@Value("${jwt.secret}") String secret, UserRepository userRepository) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
    this.userRepository = userRepository;
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public String generateToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("id", user.getId());
    claims.put("firstName", user.getFirstName());
    claims.put("lastName", user.getLastName());
    claims.put("username", user.getUsername());
    claims.put("role", user.getRole());
    claims.put("tokenVersion", user.getTokenVersion());
    return createToken(claims, user.getUsername());
  }

  private String createToken(Map<String, Object> claims, String subject) {
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
        .signWith(key)
        .compact();
  }

  public Boolean validateToken(String token, String username) {
    final String extractedUsername = extractUsername(token);

    if (!extractedUsername.equals(username) || isTokenExpired(token)) {
      return false;
    }

    User user = userRepository.findByUsername(username).orElseThrow(
        () -> new UserNotFoundException("User not found with username: " + username));

    if (!user.getActive()) {
      return false;
    }

    int tokenVersion = extractClaim(token, claims -> claims.get("tokenVersion", Integer.class));

    return user.getTokenVersion() == tokenVersion;
  }
}