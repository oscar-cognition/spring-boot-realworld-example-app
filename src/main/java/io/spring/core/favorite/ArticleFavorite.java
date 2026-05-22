package io.spring.core.favorite;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@Getter
@EqualsAndHashCode
@Table("article_favorites")
public class ArticleFavorite {
  @Column("article_id")
  private String articleId;

  @Column("user_id")
  private String userId;

  public ArticleFavorite(String articleId, String userId) {
    this.articleId = articleId;
    this.userId = userId;
  }
}
