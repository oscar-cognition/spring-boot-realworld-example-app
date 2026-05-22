package io.spring.core.comment;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table("comments")
public class Comment {
  @Id private String id;
  private String body;

  @Column("user_id")
  private String userId;

  @Column("article_id")
  private String articleId;

  @Column("created_at")
  private OffsetDateTime createdAt;

  public Comment(String body, String userId, String articleId) {
    this.id = UUID.randomUUID().toString();
    this.body = body;
    this.userId = userId;
    this.articleId = articleId;
    this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
  }
}
