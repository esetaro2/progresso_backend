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

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers(HttpMethod.OPTIONS, "/**")
            .permitAll() // Permetti le richieste OPTIONS per CORS
            .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
            .requestMatchers("/api/projectmanager/**").hasAuthority("PROJECTMANAGER")
            .requestMatchers("/api/teammember/**").hasAuthority("TEAMMEMBER")

            // AUTH
            .requestMatchers("/api/auth/login").permitAll()
            .requestMatchers("/api/auth/logout").permitAll()
            .requestMatchers("/api/auth/register").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/auth/{userId}/deactivate").hasAuthority("ADMIN")

            // TEAM
            .requestMatchers(HttpMethod.POST, "/api/teams")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PUT, "/api/teams/{teamId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.POST, "/api/teams/{teamId}/add-member/{userId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.DELETE, "/api/teams/{teamId}/remove-member/{userId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.DELETE, "/api/teams/{teamId}").hasAuthority("ADMIN")

            // TASK
            .requestMatchers(HttpMethod.POST, "/api/tasks")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PUT, "/api/tasks/{taskId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PATCH, "/api/tasks/{taskId}/complete")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.POST, "/api/tasks/{taskId}/assign/{userId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.POST, "/api/tasks/{taskId}/reassign/{userId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.POST, "/api/tasks/project/{projectId}/task/{taskId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.DELETE, "/api/tasks/project/{projectId}/task/{taskId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")

            // PROJECT
            .requestMatchers(HttpMethod.POST, "/api/projects")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PUT, "/api/projects/{projectId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PUT, "/api/projects/{projectId}/remove")
            .hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.PUT,
                "/api/projects/{projectId}/update-manager/{projectManagerId}").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/projects/{projectId}/assign-team/{teamId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PUT, "/api/projects/{projectId}/reassign-team/{teamId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PUT, "/api/projects/{projectId}/complete")
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
    config.setAllowedMethods(
        List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
    config.setExposedHeaders(List.of("Authorization"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }
}
