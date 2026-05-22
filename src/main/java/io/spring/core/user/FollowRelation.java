package io.spring.core.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@Data
@Table("follows")
public class FollowRelation {
  @Column("user_id")
  private String userId;

  @Column("follow_id")
  private String targetId;

  public FollowRelation(String userId, String targetId) {
    this.userId = userId;
    this.targetId = targetId;
  }
}
