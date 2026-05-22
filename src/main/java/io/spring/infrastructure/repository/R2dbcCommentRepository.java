package io.spring.infrastructure.repository;

import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class R2dbcCommentRepository implements CommentRepository {
  private final DatabaseClient db;

  @Override
  public void save(Comment comment) {
    db.sql(
            "INSERT INTO comments (id, body, article_id, user_id, created_at, updated_at) VALUES"
                + " (:id, :body, :articleId, :userId, :createdAt, :createdAt)")
        .bind("id", comment.getId())
        .bind("body", comment.getBody())
        .bind("articleId", comment.getArticleId())
        .bind("userId", comment.getUserId())
        .bind("createdAt", comment.getCreatedAt())
        .fetch()
        .rowsUpdated()
        .block();
  }

  @Override
  public Optional<Comment> findById(String articleId, String id) {
    return Optional.ofNullable(
        db.sql(
                "SELECT id, body, article_id, user_id, created_at FROM comments WHERE id = :id AND"
                    + " article_id = :articleId")
            .bind("id", id)
            .bind("articleId", articleId)
            .map(row -> mapComment(row))
            .one()
            .block());
  }

  @Override
  public void remove(Comment comment) {
    db.sql("DELETE FROM comments WHERE id = :id")
        .bind("id", comment.getId())
        .fetch()
        .rowsUpdated()
        .block();
  }

  private Comment mapComment(io.r2dbc.spi.Readable row) {
    Comment comment = new Comment();
    comment.setId(row.get("id", String.class));
    comment.setBody(row.get("body", String.class));
    comment.setArticleId(row.get("article_id", String.class));
    comment.setUserId(row.get("user_id", String.class));
    comment.setCreatedAt(row.get("created_at", OffsetDateTime.class));
    return comment;
  }
}
