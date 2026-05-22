package io.spring.api;

import io.spring.application.TagsQueryService;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "tags")
@AllArgsConstructor
public class TagsApi {
  private TagsQueryService tagsQueryService;

  @GetMapping
  public Mono<ResponseEntity<?>> getTags() {
    return Mono.fromCallable(() -> ResponseEntity.ok(Map.of("tags", tagsQueryService.allTags())));
  }
}
