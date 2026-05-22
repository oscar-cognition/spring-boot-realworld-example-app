package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.readservice.R2dbcUserReadService;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class CurrentUserApiTest {
  @Autowired private WebTestClient client;

  @MockBean private UserRepository userRepository;
  @MockBean private JwtService jwtService;
  @MockBean private R2dbcUserReadService userReadService;

  private User user;
  private UserData userData;
  private String token;

  @BeforeEach
  public void setUp() {
    String email = "john@jacob.com";
    String username = "johnjacob";
    String defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";

    user = new User(email, username, "123", "", defaultAvatar);
    when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

    userData = new UserData(user.getId(), email, username, "", defaultAvatar);
    when(userReadService.findById(eq(user.getId()))).thenReturn(userData);

    token = "token";
    when(jwtService.getSubFromToken(eq(token))).thenReturn(Optional.of(user.getId()));
    when(jwtService.toToken(any())).thenReturn(token);
  }

  @Test
  public void should_get_current_user_with_token() {
    client
        .get()
        .uri("/user")
        .header("Authorization", "Token " + token)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.user.email")
        .isEqualTo(user.getEmail())
        .jsonPath("$.user.username")
        .isEqualTo(user.getUsername());
  }

  @Test
  public void should_update_current_user() {
    Map<String, Object> param = Map.of("user", Map.of("email", "newemail@example.com"));

    client
        .put()
        .uri("/user")
        .header("Authorization", "Token " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(param)
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  public void should_get_401_without_token() {
    client.get().uri("/user").exchange().expectStatus().isUnauthorized();
  }
}
