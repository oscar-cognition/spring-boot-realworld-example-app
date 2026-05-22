package io.spring.api.security;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class WebSecurityConfig {

  @Bean
  public JwtTokenFilter jwtTokenFilter() {
    return new JwtTokenFilter();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(
                    (exchange, e) ->
                        Mono.fromRunnable(
                            () -> exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED))))
        .authorizeExchange(
            auth ->
                auth.pathMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .pathMatchers("/graphiql")
                    .permitAll()
                    .pathMatchers("/graphql")
                    .permitAll()
                    .pathMatchers(HttpMethod.GET, "/articles/feed")
                    .authenticated()
                    .pathMatchers(HttpMethod.POST, "/users", "/users/login")
                    .permitAll()
                    .pathMatchers(HttpMethod.GET, "/articles/**", "/profiles/**", "/tags")
                    .permitAll()
                    .anyExchange()
                    .authenticated())
        .addFilterBefore(jwtTokenFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
        .build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("*"));
    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setExposedHeaders(Arrays.asList("*"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
