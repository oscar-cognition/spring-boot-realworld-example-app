package io.spring.application;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.core.user.User;
import io.spring.infrastructure.readservice.R2dbcArticleFavoritesReadService;
import io.spring.infrastructure.readservice.R2dbcArticleReadService;
import io.spring.infrastructure.readservice.R2dbcUserRelationshipQueryService;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleQueryService {
  private R2dbcArticleReadService articleReadService;
  private R2dbcArticleFavoritesReadService articleFavoritesReadService;
  private R2dbcUserRelationshipQueryService userRelationshipQueryService;

  public Optional<ArticleData> findById(String id, User user) {
    ArticleData articleData = articleReadService.findById(id);
    if (articleData == null) {
      return Optional.empty();
    }
    fillExtraInfo(articleData, user);
    return Optional.of(articleData);
  }

  public Optional<ArticleData> findBySlug(String slug, User user) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    }
    fillExtraInfo(articleData, user);
    return Optional.of(articleData);
  }

  public CursorPager<ArticleData> findRecentArticlesWithCursor(
      String tag,
      String author,
      String favoritedBy,
      User currentUser,
      CursorPageParameter<OffsetDateTime> page) {
    List<String> articleIds =
        articleReadService.findArticlesWithCursor(tag, author, favoritedBy, page);
    if (articleIds.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }

    boolean hasExtra = articleIds.size() > page.getLimit();
    if (hasExtra) {
      articleIds = articleIds.subList(0, page.getLimit());
    }

    List<ArticleData> articles = articleReadService.findArticles(articleIds);
    fillExtraInfo(articles, currentUser);

    if (!page.isNext()) {
      Collections.reverse(articles);
    }
    return new CursorPager<>(articles, page.getDirection(), hasExtra);
  }

  public CursorPager<ArticleData> findUserFeedWithCursor(
      User user, CursorPageParameter<OffsetDateTime> page) {
    List<String> followedUsers = userRelationshipQueryService.followedUsers(user.getId());
    if (followedUsers.isEmpty()) {
      return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
    }

    List<ArticleData> articles =
        articleReadService.findArticlesOfAuthorsWithCursor(followedUsers, page);
    boolean hasExtra = articles.size() > page.getLimit();
    if (hasExtra) {
      articles = articles.subList(0, page.getLimit());
    }

    fillExtraInfo(articles, user);

    if (!page.isNext()) {
      Collections.reverse(articles);
    }
    return new CursorPager<>(articles, page.getDirection(), hasExtra);
  }

  public ArticleDataList findRecentArticles(
      String tag, String author, String favoritedBy, Page page, User currentUser) {
    List<String> articleIds = articleReadService.queryArticles(tag, author, favoritedBy, page);
    int count = articleReadService.countArticle(tag, author, favoritedBy);

    if (articleIds.isEmpty()) {
      return new ArticleDataList(new ArrayList<>(), count);
    }

    List<ArticleData> articles = articleReadService.findArticles(articleIds);
    fillExtraInfo(articles, currentUser);

    return new ArticleDataList(articles, count);
  }

  public ArticleDataList findUserFeed(User user, Page page) {
    List<String> followedUsers = userRelationshipQueryService.followedUsers(user.getId());
    if (followedUsers.isEmpty()) {
      return new ArticleDataList(new ArrayList<>(), 0);
    }

    List<ArticleData> articles = articleReadService.findArticlesOfAuthors(followedUsers, page);
    int count = articleReadService.countFeedSize(followedUsers);
    fillExtraInfo(articles, user);

    return new ArticleDataList(articles, count);
  }

  private void fillExtraInfo(ArticleData articleData, User currentUser) {
    fillExtraInfo(List.of(articleData), currentUser);
  }

  private void fillExtraInfo(List<ArticleData> articles, User currentUser) {
    if (articles.isEmpty()) return;

    List<String> ids = articles.stream().map(ArticleData::getId).collect(Collectors.toList());

    List<ArticleFavoriteCount> favCounts = articleFavoritesReadService.articlesFavoriteCount(ids);
    Map<String, Integer> favCountMap =
        favCounts.stream()
            .collect(Collectors.toMap(ArticleFavoriteCount::getId, ArticleFavoriteCount::getCount));

    Set<String> userFavorites =
        currentUser != null
            ? articleFavoritesReadService.userFavorites(ids, currentUser)
            : Set.of();

    Set<String> followingAuthors =
        currentUser != null
            ? userRelationshipQueryService.followingAuthors(
                currentUser.getId(),
                articles.stream().map(a -> a.getProfileData().getId()).collect(Collectors.toList()))
            : Set.of();

    for (ArticleData article : articles) {
      article.setFavoritesCount(favCountMap.getOrDefault(article.getId(), 0));
      article.setFavorited(userFavorites.contains(article.getId()));
      article
          .getProfileData()
          .setFollowing(followingAuthors.contains(article.getProfileData().getId()));
    }
  }
}
