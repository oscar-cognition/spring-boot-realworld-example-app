package io.spring.application.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import io.spring.application.TagsQueryService;
import io.spring.infrastructure.readservice.R2dbcTagReadService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TagsQueryServiceTest {

  @Mock private R2dbcTagReadService tagReadService;

  @InjectMocks private TagsQueryService tagsQueryService;

  @Test
  public void should_return_all_tags() {
    when(tagReadService.all()).thenReturn(List.of("java", "spring", "react"));

    List<String> tags = tagsQueryService.allTags();
    assertEquals(3, tags.size());
  }
}
