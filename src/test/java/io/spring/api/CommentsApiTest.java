package io.spring.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.readservice.R2dbcUserReadService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
public class CommentsApiTest {
  @Autowired private WebTestClient client;

  @MockBean private ArticleRepository articleRepository;
  @MockBean private CommentRepository commentRepository;
  @MockBean private CommentQueryService commentQueryService;
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
  public void should_create_comment() {
    Comment comment = new Comment("comment body", user.getId(), article.getId());
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    CommentData commentData = new CommentData();
    commentData.setId(comment.getId());
    commentData.setBody("comment body");
    commentData.setArticleId(comment.getArticleId());
    commentData.setCreatedAt(now);
    commentData.setProfileData(new ProfileData(user.getId(), user.getUsername(), "", "", false));
    when(commentQueryService.findById(any(), any())).thenReturn(Optional.of(commentData));

    Map<String, Object> param = Map.of("comment", Map.of("body", "comment body"));

    client
        .post()
        .uri("/articles/" + article.getSlug() + "/comments")
        .header("Authorization", "Token " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(param)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.comment.body")
        .isEqualTo("comment body");
  }

  @Test
  public void should_get_comments() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    CommentData commentData = new CommentData();
    commentData.setId("1");
    commentData.setBody("comment body");
    commentData.setArticleId(article.getId());
    commentData.setCreatedAt(now);
    commentData.setProfileData(new ProfileData(user.getId(), user.getUsername(), "", "", false));
    when(commentQueryService.findByArticleId(any(), any())).thenReturn(List.of(commentData));

    client
        .get()
        .uri("/articles/" + article.getSlug() + "/comments")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.comments[0].body")
        .isEqualTo("comment body");
  }
}
