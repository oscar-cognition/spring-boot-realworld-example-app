package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.TestHelper;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.readservice.R2dbcUserReadService;
import java.util.Arrays;
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
public class ArticleApiTest {
  @Autowired private WebTestClient client;

  @MockBean private ArticleQueryService articleQueryService;
  @MockBean private ArticleCommandService articleCommandService;
  @MockBean private ArticleRepository articleRepository;
  @MockBean private UserRepository userRepository;
  @MockBean private JwtService jwtService;
  @MockBean private R2dbcUserReadService userReadService;

  private User user;
  private Article article;
  private ArticleData articleData;
  private String token;

  @BeforeEach
  public void setUp() {
    user = new User("john@jacob.com", "johnjacob", "123", "", "default");
    when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

    article = new Article("test title", "desc", "body", Arrays.asList("java"), user.getId());
    articleData = TestHelper.getArticleDataFromArticleAndUser(article, user);

    token = "token";
    when(jwtService.getSubFromToken(eq(token))).thenReturn(Optional.of(user.getId()));
  }

  @Test
  public void should_get_article_by_slug() {
    when(articleQueryService.findBySlug(eq(article.getSlug()), any()))
        .thenReturn(Optional.of(articleData));

    client
        .get()
        .uri("/articles/" + article.getSlug())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.article.title")
        .isEqualTo("test title");
  }

  @Test
  public void should_update_article() {
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleQueryService.findBySlug(eq(article.getSlug()), any()))
        .thenReturn(Optional.of(articleData));

    Map<String, Object> param = Map.of("article", Map.of("title", "new title"));

    client
        .put()
        .uri("/articles/" + article.getSlug())
        .header("Authorization", "Token " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(param)
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  public void should_delete_article() {
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    client
        .delete()
        .uri("/articles/" + article.getSlug())
        .header("Authorization", "Token " + token)
        .exchange()
        .expectStatus()
        .isOk();
  }
}
