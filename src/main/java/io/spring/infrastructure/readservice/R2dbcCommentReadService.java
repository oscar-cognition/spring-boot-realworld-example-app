package io.spring.infrastructure.readservice;

import io.spring.application.CursorPageParameter;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class R2dbcCommentReadService {
  private final DatabaseClient db;

  public CommentData findById(String id) {
    return db.sql(
            "SELECT c.id, c.body, c.article_id, c.created_at, u.id as user_id, u.username, u.bio,"
                + " u.image FROM comments c JOIN users u ON c.user_id = u.id WHERE c.id = :id")
        .bind("id", id)
        .map(row -> mapCommentData(row))
        .one()
        .block();
  }

  public List<CommentData> findByArticleId(String articleId) {
    return db.sql(
            "SELECT c.id, c.body, c.article_id, c.created_at, u.id as user_id, u.username, u.bio,"
                + " u.image FROM comments c JOIN users u ON c.user_id = u.id WHERE c.article_id ="
                + " :articleId ORDER BY c.created_at DESC")
        .bind("articleId", articleId)
        .map(row -> mapCommentData(row))
        .all()
        .collectList()
        .block();
  }

  public List<CommentData> findByArticleIdWithCursor(
      String articleId, CursorPageParameter<OffsetDateTime> page) {
    StringBuilder sql =
        new StringBuilder(
            "SELECT c.id, c.body, c.article_id, c.created_at, u.id as user_id, u.username, u.bio,"
                + " u.image FROM comments c JOIN users u ON c.user_id = u.id WHERE c.article_id ="
                + " :articleId");

    if (page.getCursor() != null) {
      if (page.isNext()) {
        sql.append(" AND c.created_at < :cursor");
      } else {
        sql.append(" AND c.created_at > :cursor");
      }
    }
    sql.append(" ORDER BY c.created_at DESC LIMIT :limit");

    DatabaseClient.GenericExecuteSpec spec =
        db.sql(sql.toString()).bind("articleId", articleId).bind("limit", page.getLimit() + 1);
    if (page.getCursor() != null) {
      spec = spec.bind("cursor", page.getCursor());
    }

    return spec.map(row -> mapCommentData(row)).all().collectList().block();
  }

  private CommentData mapCommentData(io.r2dbc.spi.Readable row) {
    ProfileData profileData =
        new ProfileData(
            row.get("user_id", String.class),
            row.get("username", String.class),
            row.get("bio", String.class),
            row.get("image", String.class),
            false);

    CommentData commentData = new CommentData();
    commentData.setId(row.get("id", String.class));
    commentData.setArticleId(row.get("article_id", String.class));
    commentData.setBody(row.get("body", String.class));
    commentData.setProfileData(profileData);
    commentData.setCreatedAt(row.get("created_at", OffsetDateTime.class));
    return commentData;
  }
}
