package io.spring.application.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.infrastructure.readservice.R2dbcUserReadService;
import io.spring.infrastructure.readservice.R2dbcUserRelationshipQueryService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProfileQueryServiceTest {

  @Mock private R2dbcUserReadService userReadService;
  @Mock private R2dbcUserRelationshipQueryService userRelationshipQueryService;

  @InjectMocks private ProfileQueryService profileQueryService;

  @Test
  public void should_find_profile_by_username() {
    UserData userData = new UserData("userId", "email@test.com", "testuser", "bio", "image");
    when(userReadService.findByUsername(eq("testuser"))).thenReturn(userData);

    Optional<ProfileData> result = profileQueryService.findByUsername("testuser", null);
    assertTrue(result.isPresent());
    assertEquals("testuser", result.get().getUsername());
  }
}
