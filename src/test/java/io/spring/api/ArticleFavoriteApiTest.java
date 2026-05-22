package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.TestHelper;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.readservice.R2dbcUserReadService;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class ArticleFavoriteApiTest {
  @Autowired private WebTestClient client;

  @MockBean private ArticleQueryService articleQueryService;
  @MockBean private ArticleRepository articleRepository;
  @MockBean private ArticleFavoriteRepository articleFavoriteRepository;
  @MockBean private UserRepository userRepository;
  @MockBean private JwtService jwtService;
  @MockBean private R2dbcUserReadService userReadService;

  private User user;
  private Article article;
  private String token;

  @BeforeEach
  public void setUp() {
    user = new User("john@jacob.com", "johnjacob", "123", "", "default");
    when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

    article = new Article("test title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    token = "token";
    when(jwtService.getSubFromToken(eq(token))).thenReturn(Optional.of(user.getId()));
  }

  @Test
  public void should_favorite_article() {
    ArticleData articleData = TestHelper.getArticleDataFromArticleAndUser(article, user);
    when(articleQueryService.findBySlug(eq(article.getSlug()), any()))
        .thenReturn(Optional.of(articleData));

    client
        .post()
        .uri("/articles/" + article.getSlug() + "/favorite")
        .header("Authorization", "Token " + token)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.article.title")
        .isEqualTo("test title");
  }
}
