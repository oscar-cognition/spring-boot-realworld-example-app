package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.ArticleQueryService;
import io.spring.application.Page;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ProfileData;
import io.spring.infrastructure.readservice.R2dbcArticleFavoritesReadService;
import io.spring.infrastructure.readservice.R2dbcArticleReadService;
import io.spring.infrastructure.readservice.R2dbcUserReadService;
import io.spring.infrastructure.readservice.R2dbcUserRelationshipQueryService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleQueryServiceTest {

  @Mock private R2dbcArticleReadService articleReadService;
  @Mock private R2dbcUserReadService userReadService;
  @Mock private R2dbcArticleFavoritesReadService articleFavoritesReadService;
  @Mock private R2dbcUserRelationshipQueryService userRelationshipQueryService;

  @InjectMocks private ArticleQueryService articleQueryService;

  @Test
  public void should_find_recent_articles() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    ArticleData article =
        new ArticleData(
            "id1",
            "title-1",
            "title 1",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            new ArrayList<>(),
            new ProfileData("userId", "username", "", "", false));

    when(articleReadService.queryArticles(any(), any(), any(), any())).thenReturn(List.of("id1"));
    when(articleReadService.countArticle(any(), any(), any())).thenReturn(1);
    when(articleReadService.findArticles(any())).thenReturn(List.of(article));
    when(articleFavoritesReadService.articlesFavoriteCount(any())).thenReturn(List.of());

    ArticleDataList result =
        articleQueryService.findRecentArticles(null, null, null, new Page(0, 20), null);
    assertNotNull(result);
    assertEquals(1, result.getCount());
    assertEquals(1, result.getArticleDatas().size());
  }

  @Test
  public void should_find_article_by_slug() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    ArticleData article =
        new ArticleData(
            "id1",
            "title-1",
            "title 1",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            new ArrayList<>(),
            new ProfileData("userId", "username", "", "", false));

    when(articleReadService.findBySlug(eq("title-1"))).thenReturn(article);
    when(articleFavoritesReadService.articlesFavoriteCount(any())).thenReturn(List.of());

    Optional<ArticleData> result = articleQueryService.findBySlug("title-1", null);
    assertTrue(result.isPresent());
    assertEquals("title 1", result.get().getTitle());
  }
}
