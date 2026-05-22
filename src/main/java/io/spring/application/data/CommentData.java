package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.application.DateTimeCursor;
import io.spring.application.Node;
import io.spring.application.PageCursor;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentData implements Node {
  private String id;
  @JsonIgnore private String articleId;
  private String body;

  @JsonProperty("author")
  private ProfileData profileData;

  private OffsetDateTime createdAt;

  @JsonIgnore
  @Override
  public PageCursor getCursor() {
    return new DateTimeCursor(createdAt);
  }
}
