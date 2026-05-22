package io.spring.infrastructure.repository;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@AllArgsConstructor
public class R2dbcArticleRepository implements ArticleRepository {
  private final DatabaseClient db;

  @Override
  @Transactional
  public void save(Article article) {
    Boolean exists =
        db.sql("SELECT COUNT(*) FROM articles WHERE id = :id")
            .bind("id", article.getId())
            .map(row -> row.get(0, Long.class) > 0)
            .one()
            .block();

    if (Boolean.TRUE.equals(exists)) {
      db.sql(
              "UPDATE articles SET slug = :slug, title = :title, description = :description,"
                  + " body = :body, updated_at = :updatedAt WHERE id = :id")
          .bind("slug", article.getSlug())
          .bind("title", article.getTitle())
          .bind("description", article.getDescription())
          .bind("body", article.getBody())
          .bind("updatedAt", article.getUpdatedAt())
          .bind("id", article.getId())
          .fetch()
          .rowsUpdated()
          .block();
    } else {
      createNew(article);
    }
  }

  private void createNew(Article article) {
    for (Tag tag : article.getTags()) {
      Tag existing =
          db.sql("SELECT id, name FROM tags WHERE name = :name")
              .bind("name", tag.getName())
              .map(
                  row -> {
                    Tag t = new Tag(row.get("name", String.class));
                    t.setId(row.get("id", String.class));
                    return t;
                  })
              .one()
              .block();

      if (existing == null) {
        db.sql("INSERT INTO tags (id, name) VALUES (:id, :name)")
            .bind("id", tag.getId())
            .bind("name", tag.getName())
            .fetch()
            .rowsUpdated()
            .block();
      } else {
        tag.setId(existing.getId());
      }

      db.sql("INSERT INTO article_tags (article_id, tag_id) VALUES (:articleId, :tagId)")
          .bind("articleId", article.getId())
          .bind("tagId", tag.getId())
          .fetch()
          .rowsUpdated()
          .block();
    }

    db.sql(
            "INSERT INTO articles (id, user_id, slug, title, description, body, created_at,"
                + " updated_at) VALUES (:id, :userId, :slug, :title, :description, :body,"
                + " :createdAt, :updatedAt)")
        .bind("id", article.getId())
        .bind("userId", article.getUserId())
        .bind("slug", article.getSlug())
        .bind("title", article.getTitle())
        .bind("description", article.getDescription())
        .bind("body", article.getBody())
        .bind("createdAt", article.getCreatedAt())
        .bind("updatedAt", article.getUpdatedAt())
        .fetch()
        .rowsUpdated()
        .block();
  }

  @Override
  public Optional<Article> findById(String id) {
    return Optional.ofNullable(
        db.sql(
                "SELECT id, user_id, slug, title, description, body, created_at, updated_at FROM"
                    + " articles WHERE id = :id")
            .bind("id", id)
            .map(row -> mapArticle(row))
            .one()
            .block());
  }

  @Override
  public Optional<Article> findBySlug(String slug) {
    return Optional.ofNullable(
        db.sql(
                "SELECT id, user_id, slug, title, description, body, created_at, updated_at FROM"
                    + " articles WHERE slug = :slug")
            .bind("slug", slug)
            .map(row -> mapArticle(row))
            .one()
            .block());
  }

  @Override
  public void remove(Article article) {
    db.sql("DELETE FROM articles WHERE id = :id")
        .bind("id", article.getId())
        .fetch()
        .rowsUpdated()
        .block();
  }

  private Article mapArticle(io.r2dbc.spi.Readable row) {
    Article article = new Article();
    article.setId(row.get("id", String.class));
    article.setUserId(row.get("user_id", String.class));
    article.setSlug(row.get("slug", String.class));
    article.setTitle(row.get("title", String.class));
    article.setDescription(row.get("description", String.class));
    article.setBody(row.get("body", String.class));
    article.setCreatedAt(row.get("created_at", OffsetDateTime.class));
    article.setUpdatedAt(row.get("updated_at", OffsetDateTime.class));

    List<Tag> tags =
        db.sql(
                "SELECT t.id, t.name FROM tags t JOIN article_tags at ON t.id = at.tag_id WHERE"
                    + " at.article_id = :articleId")
            .bind("articleId", article.getId())
            .map(
                tagRow -> {
                  Tag tag = new Tag(tagRow.get("name", String.class));
                  tag.setId(tagRow.get("id", String.class));
                  return tag;
                })
            .all()
            .collectList()
            .block();
    article.setTags(tags != null ? tags : Collections.emptyList());

    return article;
  }
}
