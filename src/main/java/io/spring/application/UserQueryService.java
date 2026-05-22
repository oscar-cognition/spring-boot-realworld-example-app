package io.spring.application;

import io.spring.application.data.UserData;
import io.spring.infrastructure.readservice.R2dbcUserReadService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserQueryService {
  private R2dbcUserReadService userReadService;

  public Optional<UserData> findById(String id) {
    return Optional.ofNullable(userReadService.findById(id));
  }
}
