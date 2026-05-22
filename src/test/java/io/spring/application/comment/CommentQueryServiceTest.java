package io.spring.application.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.infrastructure.readservice.R2dbcCommentReadService;
import io.spring.infrastructure.readservice.R2dbcUserRelationshipQueryService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentQueryServiceTest {

  @Mock private R2dbcCommentReadService commentReadService;
  @Mock private R2dbcUserRelationshipQueryService userRelationshipQueryService;

  @InjectMocks private CommentQueryService commentQueryService;

  @Test
  public void should_find_comments_by_article_id() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    CommentData commentData = new CommentData();
    commentData.setId("comment1");
    commentData.setArticleId("article1");
    commentData.setBody("comment body");
    commentData.setCreatedAt(now);
    commentData.setProfileData(new ProfileData("userId", "username", "", "", false));

    when(commentReadService.findByArticleId(eq("article1"))).thenReturn(List.of(commentData));

    List<CommentData> result = commentQueryService.findByArticleId("article1", null);
    assertEquals(1, result.size());
    assertEquals("comment body", result.get(0).getBody());
  }

  @Test
  public void should_find_comment_by_id() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    CommentData commentData = new CommentData();
    commentData.setId("comment1");
    commentData.setArticleId("article1");
    commentData.setBody("comment body");
    commentData.setCreatedAt(now);
    commentData.setProfileData(new ProfileData("userId", "username", "", "", false));

    when(commentReadService.findById(eq("comment1"))).thenReturn(commentData);

    Optional<CommentData> result = commentQueryService.findById("comment1", null);
    assertTrue(result.isPresent());
    assertEquals("comment body", result.get().getBody());
  }
}
