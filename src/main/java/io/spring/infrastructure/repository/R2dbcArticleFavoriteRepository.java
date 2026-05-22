package io.spring.infrastructure.repository;

import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class R2dbcArticleFavoriteRepository implements ArticleFavoriteRepository {
  private final DatabaseClient db;

  @Override
  public void save(ArticleFavorite articleFavorite) {
    Boolean exists =
        db.sql(
                "SELECT COUNT(*) FROM article_favorites WHERE article_id = :articleId AND user_id ="
                    + " :userId")
            .bind("articleId", articleFavorite.getArticleId())
            .bind("userId", articleFavorite.getUserId())
            .map(row -> row.get(0, Long.class) > 0)
            .one()
            .block();

    if (!Boolean.TRUE.equals(exists)) {
      db.sql("INSERT INTO article_favorites (article_id, user_id) VALUES (:articleId, :userId)")
          .bind("articleId", articleFavorite.getArticleId())
          .bind("userId", articleFavorite.getUserId())
          .fetch()
          .rowsUpdated()
          .block();
    }
  }

  @Override
  public Optional<ArticleFavorite> find(String articleId, String userId) {
    return Optional.ofNullable(
        db.sql(
                "SELECT article_id, user_id FROM article_favorites WHERE article_id = :articleId"
                    + " AND user_id = :userId")
            .bind("articleId", articleId)
            .bind("userId", userId)
            .map(
                row ->
                    new ArticleFavorite(
                        row.get("article_id", String.class), row.get("user_id", String.class)))
            .one()
            .block());
  }

  @Override
  public void remove(ArticleFavorite favorite) {
    db.sql("DELETE FROM article_favorites WHERE article_id = :articleId AND user_id = :userId")
        .bind("articleId", favorite.getArticleId())
        .bind("userId", favorite.getUserId())
        .fetch()
        .rowsUpdated()
        .block();
  }

  @Override
  public int count(String articleId) {
    Long count =
        db.sql("SELECT COUNT(*) FROM article_favorites WHERE article_id = :articleId")
            .bind("articleId", articleId)
            .map(row -> row.get(0, Long.class))
            .one()
            .block();
    return count != null ? count.intValue() : 0;
  }

  @Override
  public boolean isUserFavorite(String userId, String articleId) {
    Long count =
        db.sql(
                "SELECT COUNT(*) FROM article_favorites WHERE article_id = :articleId AND user_id"
                    + " = :userId")
            .bind("articleId", articleId)
            .bind("userId", userId)
            .map(row -> row.get(0, Long.class))
            .one()
            .block();
    return count != null && count > 0;
  }
}
