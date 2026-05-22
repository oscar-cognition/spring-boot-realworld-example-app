package io.spring.core.article;

import static java.util.stream.Collectors.toList;

import io.spring.Util;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Table("articles")
public class Article {
  @Column("user_id")
  @Setter
  private String userId;

  @Id @Setter private String id;
  @Setter private String slug;
  @Setter private String title;
  @Setter private String description;
  @Setter private String body;

  @Transient @Setter private List<Tag> tags = new ArrayList<>();

  @Column("created_at")
  @Setter
  private OffsetDateTime createdAt;

  @Column("updated_at")
  @Setter
  private OffsetDateTime updatedAt;

  public Article(
      String title, String description, String body, List<String> tagList, String userId) {
    this(title, description, body, tagList, userId, OffsetDateTime.now(ZoneOffset.UTC));
  }

  public Article(
      String title,
      String description,
      String body,
      List<String> tagList,
      String userId,
      OffsetDateTime createdAt) {
    this.id = UUID.randomUUID().toString();
    this.slug = toSlug(title);
    this.title = title;
    this.description = description;
    this.body = body;
    this.tags = new HashSet<>(tagList).stream().map(Tag::new).collect(toList());
    this.userId = userId;
    this.createdAt = createdAt;
    this.updatedAt = createdAt;
  }

  public void update(String title, String description, String body) {
    if (!Util.isEmpty(title)) {
      this.title = title;
      this.slug = toSlug(title);
      this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
    if (!Util.isEmpty(description)) {
      this.description = description;
      this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
    if (!Util.isEmpty(body)) {
      this.body = body;
      this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
  }

  public static String toSlug(String title) {
    return title.toLowerCase().replaceAll("[&|\\uFE30-\\uFFA0'\"\\s?,\\.]+", "-");
  }
}
