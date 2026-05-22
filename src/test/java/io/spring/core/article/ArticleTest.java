package io.spring.core.article;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ArticleTest {

  @Test
  public void should_create_article_with_correct_slug() {
    Article article = new Article("Hello World", "desc", "body", Arrays.asList("java"), "userId");
    assertEquals("hello-world", article.getSlug());
    assertNotNull(article.getCreatedAt());
    assertNotNull(article.getId());
  }

  @Test
  public void should_update_article() {
    Article article = new Article("Hello World", "desc", "body", Arrays.asList("java"), "userId");
    article.update("New Title", "", "");
    assertEquals("new-title", article.getSlug());
  }

  @Test
  public void should_generate_slug() {
    assertEquals("hello-world", Article.toSlug("Hello World"));
  }
}
