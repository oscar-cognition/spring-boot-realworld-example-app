package io.spring.api.security;

import io.spring.core.service.JwtService;
import io.spring.core.user.UserRepository;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class JwtTokenFilter implements WebFilter {

  @Autowired private UserRepository userRepository;
  @Autowired private JwtService jwtService;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    return getTokenString(header)
        .flatMap(
            token ->
                Mono.justOrEmpty(jwtService.getSubFromToken(token))
                    .flatMap(id -> Mono.justOrEmpty(userRepository.findById(id))))
        .flatMap(
            user -> {
              UsernamePasswordAuthenticationToken auth =
                  new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
              return chain
                  .filter(exchange)
                  .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
            })
        .switchIfEmpty(chain.filter(exchange));
  }

  private Mono<String> getTokenString(String header) {
    if (header != null) {
      String[] split = header.split(" ");
      if (split.length == 2 && split[0].equals("Token")) {
        return Mono.just(split[1]);
      }
    }
    return Mono.empty();
  }
}
