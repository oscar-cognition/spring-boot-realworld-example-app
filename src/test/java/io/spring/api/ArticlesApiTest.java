package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.TestHelper;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.core.article.Article;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.readservice.R2dbcUserReadService;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class ArticlesApiTest {
  @Autowired private WebTestClient client;

  @MockBean private ArticleQueryService articleQueryService;
  @MockBean private ArticleCommandService articleCommandService;
  @MockBean private UserRepository userRepository;
  @MockBean private JwtService jwtService;
  @MockBean private R2dbcUserReadService userReadService;

  private User user;
  private String token;

  @BeforeEach
  public void setUp() {
    String email = "john@jacob.com";
    String username = "johnjacob";
    String defaultAvatar = "https://static.productionready.io/images/smiley-cyrus.jpg";

    user = new User(email, username, "123", "", defaultAvatar);
    when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

    token = "token";
    when(jwtService.getSubFromToken(eq(token))).thenReturn(Optional.of(user.getId()));
  }

  @Test
  public void should_create_article_success() {
    Article article =
        new Article("test title", "desc", "body", Arrays.asList("java"), user.getId());
    ArticleData articleData = TestHelper.getArticleDataFromArticleAndUser(article, user);

    when(articleCommandService.createArticle(any(), any())).thenReturn(article);
    when(articleQueryService.findById(any(), any())).thenReturn(Optional.of(articleData));

    Map<String, Object> param =
        Map.of(
            "article",
            Map.of(
                "title", "test title",
                "description", "desc",
                "body", "body",
                "tagList", List.of("java")));

    client
        .post()
        .uri("/articles")
        .header("Authorization", "Token " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(param)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.article.title")
        .isEqualTo("test title");
  }

  @Test
  public void should_get_articles() {
    ArticleData articleData = TestHelper.articleDataFixture("1", user);
    ArticleDataList list = new ArticleDataList(List.of(articleData), 1);
    when(articleQueryService.findRecentArticles(any(), any(), any(), any(), any()))
        .thenReturn(list);

    client
        .get()
        .uri("/articles")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.articlesCount")
        .isEqualTo(1);
  }
}
