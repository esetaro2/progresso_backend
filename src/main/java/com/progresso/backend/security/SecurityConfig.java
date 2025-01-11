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

            //AUTH
            .requestMatchers("/api/auth/login").permitAll()
            .requestMatchers("/api/auth/register").hasAuthority("ADMIN")
//            .requestMatchers("/api/auth/register").permitAll()

            //USER
            .requestMatchers(HttpMethod.GET, "/api/users").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/users/role/{roleName}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/users/search").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/users/teams/{teamId}/users").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/users/teams/{teamId}/user/{userId}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/users/projects/{projectId}/users").permitAll()
            .requestMatchers(HttpMethod.PUT, "/api/users/{userId}/deactivate").hasAuthority("ADMIN")

            //TEAM
            .requestMatchers(HttpMethod.GET, "/api/teams").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/teams/{teamId}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/teams/search/by-name/{name}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/teams/active").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/teams/user/{userId}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/teams/with-projects").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/teams/min-members/{size}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/teams/without-members").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/teams/by-project/{projectId}").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/teams")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PUT, "/api/teams/{teamId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.POST, "/api/teams/{teamId}/add-member/{userId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.DELETE, "/api/teams/{teamId}/remove-member/{userId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.DELETE, "/api/teams/{teamId}").hasAuthority("ADMIN")

            //TASK
            .requestMatchers(HttpMethod.GET, "/api/tasks/status/{status}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/tasks/priority/{priority}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/tasks/dueDateBefore/{dueDate}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/tasks/completionDateAfter/{completionDate}")
            .permitAll()
            .requestMatchers(HttpMethod.GET, "/api/tasks/project/{projectId}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/tasks/project/{projectId}/status/{status}")
            .permitAll()
            .requestMatchers(HttpMethod.GET, "/api/tasks/project/{projectId}/name/{name}")
            .permitAll()
            .requestMatchers(HttpMethod.GET, "/api/tasks/project/{projectId}/completed").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/tasks/user/{userId}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/tasks/user/{userId}/status/{status}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/tasks/user/{userId}/overdue").permitAll()
            .requestMatchers(HttpMethod.GET,
                "/api/tasks/assigned-user/{userId}/completion-date-before/{completionDate}")
            .permitAll()
            .requestMatchers(HttpMethod.GET,
                "/api/tasks/assigned-user/{userId}/start-date-after/{startDate}").permitAll()
            .requestMatchers(HttpMethod.GET,
                "/api/tasks/project/{projectId}/completion-date-before/{completionDate}")
            .permitAll()
            .requestMatchers(HttpMethod.GET, "/api/tasks/start-date-between/{startDate}/{endDate}")
            .permitAll()
            .requestMatchers(HttpMethod.POST, "/api/tasks")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PUT, "/api/tasks/{taskId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PATCH, "/api/tasks/{taskId}/complete")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.POST, "/api/tasks/{taskId}/assign/{userId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
//            .permitAll()
            .requestMatchers(HttpMethod.POST, "/api/tasks/{taskId}/reassign/{userId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.POST, "/api/tasks/project/{projectId}/task/{taskId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.DELETE, "/api/tasks/project/{projectId}/task/{taskId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")

            //PROJECT
            .requestMatchers(HttpMethod.GET, "/api/projects").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/projects/{id}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/projects/status/{status}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/projects/manager/{managerId}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/projects/priority/{priority}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/projects/due-before/{date}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/projects/completion-after/{date}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/projects/manager/{managerId}/active").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/projects/task-status/{taskStatus}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/projects/team/{teamId}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/projects/team/{teamId}/status/{status}")
            .permitAll()
            .requestMatchers(HttpMethod.GET,
                "/api/projects/team/{teamId}/due-date-before/{dueDate}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/projects/team/{teamId}/active").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/projects/status/{status}/priority/{priority}")
            .permitAll()
            .requestMatchers(HttpMethod.GET,
                "/api/projects/start-date-between/{startDate}/{endDate}").permitAll()
            .requestMatchers(HttpMethod.GET,
                "/api/projects/completion-date-before/{completionDate}").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/projects")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PUT, "/api/projects/{projectId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PUT, "/api/projects/{projectId}/remove")
            .hasAuthority("ADMIN").requestMatchers(HttpMethod.PUT,
                "/api/projects/{projectId}/update-manager/{projectManagerId}").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/projects/{projectId}/assign-team/{teamId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PUT, "/api/projects/{projectId}/reassign-team/{teamId}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PUT, "/api/projects/{projectId}/complete")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")

            // COMMENTS
            .requestMatchers(HttpMethod.GET, "/api/comments/{id}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/comments/project/{projectId}/root-comments")
            .permitAll()
            .requestMatchers(HttpMethod.GET, "/api/comments/parent/{parentId}/replies").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/comments/project/{projectId}/comments")
            .permitAll()
            .requestMatchers(HttpMethod.GET, "/api/comments/user/{userId}/comments").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/comments/project/{projectId}/content")
            .permitAll()
            .requestMatchers(HttpMethod.GET,
                "/api/comments/project/{projectId}/active-root-comments").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/comments/parent/{parentId}/active-replies")
            .permitAll()
            .requestMatchers(HttpMethod.GET,
                "/api/comments/project/{projectId}/user/{userId}/comments").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/comments/project/{projectId}/active-comments")
            .permitAll()
            .requestMatchers(HttpMethod.GET, "/api/comments/user/{userId}/active-comments")
            .permitAll()
            .requestMatchers(HttpMethod.GET,
                "/api/comments/project/{projectId}/active-comments/content").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/comments").authenticated()
            .requestMatchers(HttpMethod.PUT, "/api/comments/{id}").authenticated()
            .requestMatchers(HttpMethod.DELETE, "/api/comments/{id}").authenticated()

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
