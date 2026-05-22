package io.spring.infrastructure.readservice;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class R2dbcUserRelationshipQueryService {
  private final DatabaseClient db;

  public boolean isUserFollowing(String userId, String anotherUserId) {
    if (userId == null || anotherUserId == null) return false;
    Long count =
        db.sql(
                "SELECT COUNT(*) FROM follows WHERE user_id = :userId AND follow_id ="
                    + " :anotherUserId")
            .bind("userId", userId)
            .bind("anotherUserId", anotherUserId)
            .map(row -> row.get(0, Long.class))
            .one()
            .block();
    return count != null && count > 0;
  }

  public Set<String> followingAuthors(String userId, List<String> ids) {
    if (userId == null || ids == null || ids.isEmpty()) return Set.of();
    List<String> result =
        db.sql("SELECT follow_id FROM follows WHERE user_id = :userId AND follow_id IN (:ids)")
            .bind("userId", userId)
            .bind("ids", ids)
            .map(row -> row.get("follow_id", String.class))
            .all()
            .collectList()
            .block();
    return result != null ? new HashSet<>(result) : Set.of();
  }

  public List<String> followedUsers(String userId) {
    if (userId == null) return List.of();
    List<String> result =
        db.sql("SELECT follow_id FROM follows WHERE user_id = :userId")
            .bind("userId", userId)
            .map(row -> row.get("follow_id", String.class))
            .all()
            .collectList()
            .block();
    return result != null ? result : List.of();
  }
}
