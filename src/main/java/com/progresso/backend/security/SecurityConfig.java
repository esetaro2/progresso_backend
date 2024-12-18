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
            .requestMatchers("/auth/login").permitAll()
            .requestMatchers("/auth/register").hasAuthority("ADMIN")
//            .requestMatchers("/auth/register").permitAll()

            .requestMatchers("/admin/**").hasAuthority("ADMIN")

            .requestMatchers("/projectmanager/**").hasAuthority("PROJECTMANAGER")

            .requestMatchers("/teammember/**").hasAuthority("TEAMMEMBER")

            .requestMatchers(HttpMethod.GET, "/team").permitAll()
            .requestMatchers(HttpMethod.GET, "/team/{id}").permitAll()
            .requestMatchers(HttpMethod.GET, "/team/name").permitAll()
            .requestMatchers(HttpMethod.GET, "/team/projectManager/{id}").permitAll()
            .requestMatchers(HttpMethod.GET, "/team/teamMember/{id}").permitAll()

            .requestMatchers(HttpMethod.POST, "/team").hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.POST, "/team/{id}/members")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PUT, "/team/{id}")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PUT, "/team/{id}/removeMembers")
            .hasAnyAuthority("ADMIN", "PROJECTMANAGER")
            .requestMatchers(HttpMethod.PUT, "/team/{id}/projectManager").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/team/{id}/deactivate").hasAuthority("ADMIN")

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
