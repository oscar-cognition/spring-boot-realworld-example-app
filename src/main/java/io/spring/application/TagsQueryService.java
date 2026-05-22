package io.spring.application;

import io.spring.infrastructure.readservice.R2dbcTagReadService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TagsQueryService {
  private R2dbcTagReadService tagReadService;

  public List<String> allTags() {
    return tagReadService.all();
  }
}
