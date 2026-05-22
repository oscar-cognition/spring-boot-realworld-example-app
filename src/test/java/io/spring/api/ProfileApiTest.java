package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.readservice.R2dbcUserReadService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class ProfileApiTest {
  @Autowired private WebTestClient client;

  @MockBean private ProfileQueryService profileQueryService;
  @MockBean private UserRepository userRepository;
  @MockBean private JwtService jwtService;
  @MockBean private R2dbcUserReadService userReadService;

  private User user;
  private String token;

  @BeforeEach
  public void setUp() {
    user = new User("john@jacob.com", "johnjacob", "123", "", "default");
    when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

    token = "token";
    when(jwtService.getSubFromToken(eq(token))).thenReturn(Optional.of(user.getId()));
  }

  @Test
  public void should_get_profile() {
    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    when(profileQueryService.findByUsername(eq(user.getUsername()), any()))
        .thenReturn(Optional.of(profileData));

    client
        .get()
        .uri("/profiles/" + user.getUsername())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.profile.username")
        .isEqualTo(user.getUsername());
  }

  @Test
  public void should_follow_user() {
    User target = new User("target@example.com", "targetuser", "123", "", "");
    when(userRepository.findByUsername(eq(target.getUsername()))).thenReturn(Optional.of(target));
    ProfileData profileData =
        new ProfileData(
            target.getId(), target.getUsername(), target.getBio(), target.getImage(), true);
    when(profileQueryService.findByUsername(eq(target.getUsername()), any()))
        .thenReturn(Optional.of(profileData));

    client
        .post()
        .uri("/profiles/" + target.getUsername() + "/follow")
        .header("Authorization", "Token " + token)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.profile.following")
        .isEqualTo(true);
  }
}
