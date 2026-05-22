package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.spring.TestHelper;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.readservice.R2dbcUserReadService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class ListArticleApiTest {
  @Autowired private WebTestClient client;

  @MockBean private ArticleQueryService articleQueryService;
  @MockBean private UserRepository userRepository;
  @MockBean private JwtService jwtService;
  @MockBean private R2dbcUserReadService userReadService;

  @Test
  public void should_list_articles_with_default_params() {
    User user = new User("john@jacob.com", "johnjacob", "123", "", "default");
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

  @Test
  public void should_list_articles_by_tag() {
    User user = new User("john@jacob.com", "johnjacob", "123", "", "default");
    ArticleData articleData = TestHelper.articleDataFixture("1", user);
    ArticleDataList list = new ArticleDataList(List.of(articleData), 1);
    when(articleQueryService.findRecentArticles(any(), any(), any(), any(), any()))
        .thenReturn(list);

    client
        .get()
        .uri("/articles?tag=java")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.articlesCount")
        .isEqualTo(1);
  }
}
