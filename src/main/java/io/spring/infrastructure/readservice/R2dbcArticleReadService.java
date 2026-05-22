package io.spring.infrastructure.readservice;

import io.spring.application.CursorPageParameter;
import io.spring.application.Page;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class R2dbcArticleReadService {
  private final DatabaseClient db;

  public ArticleData findById(String id) {
    return db.sql(
            "SELECT a.id, a.slug, a.title, a.description, a.body, a.created_at, a.updated_at,"
                + " u.id as user_id, u.username, u.bio, u.image FROM articles a JOIN users u ON"
                + " a.user_id = u.id WHERE a.id = :id")
        .bind("id", id)
        .map(row -> mapArticleData(row))
        .one()
        .block();
  }

  public ArticleData findBySlug(String slug) {
    return db.sql(
            "SELECT a.id, a.slug, a.title, a.description, a.body, a.created_at, a.updated_at,"
                + " u.id as user_id, u.username, u.bio, u.image FROM articles a JOIN users u ON"
                + " a.user_id = u.id WHERE a.slug = :slug")
        .bind("slug", slug)
        .map(row -> mapArticleData(row))
        .one()
        .block();
  }

  public List<String> queryArticles(String tag, String author, String favoritedBy, Page page) {
    StringBuilder sql = new StringBuilder("SELECT DISTINCT a.id FROM articles a");
    List<String> conditions = new ArrayList<>();

    if (tag != null && !tag.isEmpty()) {
      sql.append(" JOIN article_tags at ON a.id = at.article_id JOIN tags t ON at.tag_id = t.id");
      conditions.add("t.name = :tag");
    }
    if (author != null && !author.isEmpty()) {
      sql.append(" JOIN users u ON a.user_id = u.id");
      conditions.add("u.username = :author");
    }
    if (favoritedBy != null && !favoritedBy.isEmpty()) {
      sql.append(
          " JOIN article_favorites af ON a.id = af.article_id JOIN users fu ON af.user_id ="
              + " fu.id");
      conditions.add("fu.username = :favoritedBy");
    }

    if (!conditions.isEmpty()) {
      sql.append(" WHERE ").append(String.join(" AND ", conditions));
    }
    sql.append(" ORDER BY a.created_at DESC LIMIT :limit OFFSET :offset");

    DatabaseClient.GenericExecuteSpec spec = db.sql(sql.toString());
    if (tag != null && !tag.isEmpty()) spec = spec.bind("tag", tag);
    if (author != null && !author.isEmpty()) spec = spec.bind("author", author);
    if (favoritedBy != null && !favoritedBy.isEmpty()) spec = spec.bind("favoritedBy", favoritedBy);
    spec = spec.bind("limit", page.getLimit());
    spec = spec.bind("offset", page.getOffset());

    return spec.map(row -> row.get("id", String.class)).all().collectList().block();
  }

  public int countArticle(String tag, String author, String favoritedBy) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT a.id) FROM articles a");
    List<String> conditions = new ArrayList<>();

    if (tag != null && !tag.isEmpty()) {
      sql.append(" JOIN article_tags at ON a.id = at.article_id JOIN tags t ON at.tag_id = t.id");
      conditions.add("t.name = :tag");
    }
    if (author != null && !author.isEmpty()) {
      sql.append(" JOIN users u ON a.user_id = u.id");
      conditions.add("u.username = :author");
    }
    if (favoritedBy != null && !favoritedBy.isEmpty()) {
      sql.append(
          " JOIN article_favorites af ON a.id = af.article_id JOIN users fu ON af.user_id ="
              + " fu.id");
      conditions.add("fu.username = :favoritedBy");
    }

    if (!conditions.isEmpty()) {
      sql.append(" WHERE ").append(String.join(" AND ", conditions));
    }

    DatabaseClient.GenericExecuteSpec spec = db.sql(sql.toString());
    if (tag != null && !tag.isEmpty()) spec = spec.bind("tag", tag);
    if (author != null && !author.isEmpty()) spec = spec.bind("author", author);
    if (favoritedBy != null && !favoritedBy.isEmpty()) spec = spec.bind("favoritedBy", favoritedBy);

    Long count = spec.map(row -> row.get(0, Long.class)).one().block();
    return count != null ? count.intValue() : 0;
  }

  public List<ArticleData> findArticles(List<String> articleIds) {
    if (articleIds == null || articleIds.isEmpty()) return List.of();
    return db.sql(
            "SELECT a.id, a.slug, a.title, a.description, a.body, a.created_at, a.updated_at,"
                + " u.id as user_id, u.username, u.bio, u.image FROM articles a JOIN users u ON"
                + " a.user_id = u.id WHERE a.id IN (:ids) ORDER BY a.created_at DESC")
        .bind("ids", articleIds)
        .map(row -> mapArticleData(row))
        .all()
        .collectList()
        .block();
  }

  public List<ArticleData> findArticlesOfAuthors(List<String> authors, Page page) {
    if (authors == null || authors.isEmpty()) return List.of();
    return db.sql(
            "SELECT a.id, a.slug, a.title, a.description, a.body, a.created_at, a.updated_at,"
                + " u.id as user_id, u.username, u.bio, u.image FROM articles a JOIN users u ON"
                + " a.user_id = u.id WHERE a.user_id IN (:authors) ORDER BY a.created_at DESC"
                + " LIMIT :limit OFFSET :offset")
        .bind("authors", authors)
        .bind("limit", page.getLimit())
        .bind("offset", page.getOffset())
        .map(row -> mapArticleData(row))
        .all()
        .collectList()
        .block();
  }

  public List<ArticleData> findArticlesOfAuthorsWithCursor(
      List<String> authors, CursorPageParameter<OffsetDateTime> page) {
    if (authors == null || authors.isEmpty()) return List.of();
    StringBuilder sql =
        new StringBuilder(
            "SELECT a.id, a.slug, a.title, a.description, a.body, a.created_at, a.updated_at,"
                + " u.id as user_id, u.username, u.bio, u.image FROM articles a JOIN users u ON"
                + " a.user_id = u.id WHERE a.user_id IN (:authors)");

    if (page.getCursor() != null) {
      if (page.isNext()) {
        sql.append(" AND a.created_at < :cursor");
      } else {
        sql.append(" AND a.created_at > :cursor");
      }
    }
    sql.append(" ORDER BY a.created_at DESC LIMIT :limit");

    DatabaseClient.GenericExecuteSpec spec =
        db.sql(sql.toString()).bind("authors", authors).bind("limit", page.getLimit() + 1);
    if (page.getCursor() != null) {
      spec = spec.bind("cursor", page.getCursor());
    }

    return spec.map(row -> mapArticleData(row)).all().collectList().block();
  }

  public int countFeedSize(List<String> authors) {
    if (authors == null || authors.isEmpty()) return 0;
    Long count =
        db.sql("SELECT COUNT(*) FROM articles WHERE user_id IN (:authors)")
            .bind("authors", authors)
            .map(row -> row.get(0, Long.class))
            .one()
            .block();
    return count != null ? count.intValue() : 0;
  }

  public List<String> findArticlesWithCursor(
      String tag, String author, String favoritedBy, CursorPageParameter<OffsetDateTime> page) {
    StringBuilder sql = new StringBuilder("SELECT DISTINCT a.id, a.created_at FROM articles a");
    List<String> conditions = new ArrayList<>();

    if (tag != null && !tag.isEmpty()) {
      sql.append(" JOIN article_tags at ON a.id = at.article_id JOIN tags t ON at.tag_id = t.id");
      conditions.add("t.name = :tag");
    }
    if (author != null && !author.isEmpty()) {
      sql.append(" JOIN users u ON a.user_id = u.id");
      conditions.add("u.username = :author");
    }
    if (favoritedBy != null && !favoritedBy.isEmpty()) {
      sql.append(
          " JOIN article_favorites af ON a.id = af.article_id JOIN users fu ON af.user_id ="
              + " fu.id");
      conditions.add("fu.username = :favoritedBy");
    }
    if (page.getCursor() != null) {
      if (page.isNext()) {
        conditions.add("a.created_at < :cursor");
      } else {
        conditions.add("a.created_at > :cursor");
      }
    }
    if (!conditions.isEmpty()) {
      sql.append(" WHERE ").append(String.join(" AND ", conditions));
    }
    sql.append(" ORDER BY a.created_at DESC LIMIT :limit");

    DatabaseClient.GenericExecuteSpec spec = db.sql(sql.toString());
    if (tag != null && !tag.isEmpty()) spec = spec.bind("tag", tag);
    if (author != null && !author.isEmpty()) spec = spec.bind("author", author);
    if (favoritedBy != null && !favoritedBy.isEmpty()) spec = spec.bind("favoritedBy", favoritedBy);
    if (page.getCursor() != null) spec = spec.bind("cursor", page.getCursor());
    spec = spec.bind("limit", page.getLimit() + 1);

    return spec.map(row -> row.get("id", String.class)).all().collectList().block();
  }

  private ArticleData mapArticleData(io.r2dbc.spi.Readable row) {
    String articleId = row.get("id", String.class);

    List<String> tagNames =
        db.sql(
                "SELECT t.name FROM tags t JOIN article_tags at ON t.id = at.tag_id WHERE"
                    + " at.article_id = :articleId")
            .bind("articleId", articleId)
            .map(r -> r.get("name", String.class))
            .all()
            .collectList()
            .block();

    ProfileData profileData =
        new ProfileData(
            row.get("user_id", String.class),
            row.get("username", String.class),
            row.get("bio", String.class),
            row.get("image", String.class),
            false);

    ArticleData articleData = new ArticleData();
    articleData.setId(articleId);
    articleData.setSlug(row.get("slug", String.class));
    articleData.setTitle(row.get("title", String.class));
    articleData.setDescription(row.get("description", String.class));
    articleData.setBody(row.get("body", String.class));
    articleData.setCreatedAt(row.get("created_at", OffsetDateTime.class));
    articleData.setUpdatedAt(row.get("updated_at", OffsetDateTime.class));
    articleData.setTagList(tagNames != null ? tagNames : List.of());
    articleData.setProfileData(profileData);
    articleData.setFavorited(false);
    articleData.setFavoritesCount(0);

    return articleData;
  }
}
