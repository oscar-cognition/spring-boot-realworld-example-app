package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.application.DateTimeCursor;
import io.spring.application.Node;
import io.spring.application.PageCursor;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleData implements Node {
  private String id;
  private String slug;
  private String title;
  private String description;
  private String body;
  private boolean favorited;
  private int favoritesCount;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private List<String> tagList;

  @JsonProperty("author")
  private ProfileData profileData;

  @JsonIgnore
  @Override
  public PageCursor getCursor() {
    return new DateTimeCursor(createdAt);
  }
}
