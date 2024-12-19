package com.progresso.backend.security;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

  private final JwtRequestFilter jwtRequestFilter;

  @Autowired
  public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
    this.jwtRequestFilter = jwtRequestFilter;
  }

  @SuppressWarnings("checkstyle:CommentsIndentation")
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
            .requestMatchers("/api/projectmanager/**").hasAuthority("PROJECTMANAGER")
            .requestMatchers("/api/teammember/**").hasAuthority("TEAMMEMBER")

            .requestMatchers("/api/auth/login").permitAll()
            .requestMatchers("/api/auth/register").hasAuthority("ADMIN")
//            .requestMatchers("/api/auth/register").permitAll()

            .requestMatchers(HttpMethod.GET, "/api/users").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/users/role/{roleName}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/users/search").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/users/{userId}/members").permitAll()

            .requestMatchers(HttpMethod.GET, "/api/teams").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/teams/{teamId}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/teams/search/by-name/{name}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/teams/project-managers/{projectManagerId}")
            .permitAll()
            .requestMatchers(HttpMethod.GET, "/api/teams/members/{memberId}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/teams/filter/by-status").permitAll()
            .requestMatchers(HttpMethod.GET,
                "/api/teams/project-managers/{projectManagerId}/filter/by-status").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/teams/search/advanced").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/teams/{teamId}/members/active/count").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/teams")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PUT, "/api/teams/{teamId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PUT, "/api/teams/{teamId}/project-manager")
            .hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/teams/{teamId}/deactivate")
            .hasAuthority("ADMIN")

            .requestMatchers(HttpMethod.GET, "/api/teams/{teamId}/members/active").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/teams/{teamId}/members")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PUT, "/api/teams/{teamId}/removeMembers")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")

            .anyRequest().authenticated())
        .cors(withDefaults());

    http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }


  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:4200"));
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }
}
