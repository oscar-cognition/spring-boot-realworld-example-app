package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.application.data.UserData;
import io.spring.application.user.UserService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class UsersApiTest {
  @Autowired private WebTestClient client;

  @MockBean private UserRepository userRepository;
  @MockBean private JwtService jwtService;
  @MockBean private R2dbcUserReadService userReadService;
  @MockBean private UserService userService;
  @Autowired private PasswordEncoder passwordEncoder;

  private String defaultAvatar;

  @BeforeEach
  public void setUp() {
    defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";
  }

  @Test
  public void should_create_user_success() {
    String email = "john@jacob.com";
    String username = "johnjacob";

    when(jwtService.toToken(any())).thenReturn("123");
    User user = new User(email, username, "123", "", defaultAvatar);
    UserData userData = new UserData(user.getId(), email, username, "", defaultAvatar);
    when(userReadService.findById(any())).thenReturn(userData);
    when(userService.createUser(any())).thenReturn(user);
    when(userRepository.findByUsername(eq(username))).thenReturn(Optional.empty());
    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.empty());

    Map<String, Object> param =
        Map.of("user", Map.of("email", email, "password", "johnnyjacob", "username", username));

    client
        .post()
        .uri("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(param)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.user.email")
        .isEqualTo(email)
        .jsonPath("$.user.username")
        .isEqualTo(username)
        .jsonPath("$.user.token")
        .isEqualTo("123");

    verify(userService).createUser(any());
  }

  @Test
  public void should_login_success() {
    String email = "john@jacob.com";
    String username = "johnjacob2";
    String password = "123";

    User user = new User(email, username, passwordEncoder.encode(password), "", defaultAvatar);
    UserData userData = new UserData(user.getId(), email, username, "", defaultAvatar);

    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(user));
    when(userReadService.findByUsername(eq(username))).thenReturn(userData);
    when(userReadService.findById(eq(user.getId()))).thenReturn(userData);
    when(jwtService.toToken(any())).thenReturn("123");

    Map<String, Object> param = Map.of("user", Map.of("email", email, "password", password));

    client
        .post()
        .uri("/users/login")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(param)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.user.email")
        .isEqualTo(email)
        .jsonPath("$.user.username")
        .isEqualTo(username)
        .jsonPath("$.user.token")
        .isEqualTo("123");
  }

  @Test
  public void should_fail_login_with_wrong_password() {
    String email = "john@jacob.com";
    String username = "johnjacob2";
    String password = "123";

    User user = new User(email, username, password, "", defaultAvatar);
    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(user));

    Map<String, Object> param = Map.of("user", Map.of("email", email, "password", "123123"));

    client
        .post()
        .uri("/users/login")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(param)
        .exchange()
        .expectStatus()
        .isEqualTo(422);
  }
}
