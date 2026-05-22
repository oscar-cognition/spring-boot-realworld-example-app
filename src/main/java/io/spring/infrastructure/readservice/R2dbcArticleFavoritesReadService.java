package io.spring.infrastructure.readservice;

import io.spring.application.data.ArticleFavoriteCount;
import io.spring.core.user.User;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class R2dbcArticleFavoritesReadService {
  private final DatabaseClient db;

  public boolean isUserFavorite(String userId, String articleId) {
    if (userId == null || articleId == null) return false;
    Long count =
        db.sql(
                "SELECT COUNT(*) FROM article_favorites WHERE user_id = :userId AND article_id ="
                    + " :articleId")
            .bind("userId", userId)
            .bind("articleId", articleId)
            .map(row -> row.get(0, Long.class))
            .one()
            .block();
    return count != null && count > 0;
  }

  public int articleFavoriteCount(String articleId) {
    Long count =
        db.sql("SELECT COUNT(*) FROM article_favorites WHERE article_id = :articleId")
            .bind("articleId", articleId)
            .map(row -> row.get(0, Long.class))
            .one()
            .block();
    return count != null ? count.intValue() : 0;
  }

  public List<ArticleFavoriteCount> articlesFavoriteCount(List<String> ids) {
    if (ids == null || ids.isEmpty()) return List.of();
    return db.sql(
            "SELECT article_id, COUNT(*) as cnt FROM article_favorites WHERE article_id IN (:ids)"
                + " GROUP BY article_id")
        .bind("ids", ids)
        .map(
            row ->
                new ArticleFavoriteCount(
                    row.get("article_id", String.class), row.get("cnt", Long.class).intValue()))
        .all()
        .collectList()
        .block();
  }

  public Set<String> userFavorites(List<String> ids, User currentUser) {
    if (ids == null || ids.isEmpty() || currentUser == null) return Set.of();
    List<String> result =
        db.sql(
                "SELECT article_id FROM article_favorites WHERE article_id IN (:ids) AND user_id ="
                    + " :userId")
            .bind("ids", ids)
            .bind("userId", currentUser.getId())
            .map(row -> row.get("article_id", String.class))
            .all()
            .collectList()
            .block();
    return result != null ? new HashSet<>(result) : Set.of();
  }
}
