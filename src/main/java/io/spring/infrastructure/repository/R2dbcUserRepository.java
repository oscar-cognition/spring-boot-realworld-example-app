package io.spring.infrastructure.repository;

import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class R2dbcUserRepository implements UserRepository {
  private final DatabaseClient db;

  @Override
  public void save(User user) {
    Boolean exists =
        db.sql("SELECT COUNT(*) FROM users WHERE id = :id")
            .bind("id", user.getId())
            .map(row -> row.get(0, Long.class) > 0)
            .one()
            .block();

    if (Boolean.TRUE.equals(exists)) {
      db.sql(
              "UPDATE users SET email = :email, username = :username, password = :password,"
                  + " bio = :bio, image = :image WHERE id = :id")
          .bind("email", user.getEmail())
          .bind("username", user.getUsername())
          .bind("password", user.getPassword())
          .bind("bio", user.getBio())
          .bind("image", user.getImage())
          .bind("id", user.getId())
          .fetch()
          .rowsUpdated()
          .block();
    } else {
      db.sql(
              "INSERT INTO users (id, email, username, password, bio, image) VALUES"
                  + " (:id, :email, :username, :password, :bio, :image)")
          .bind("id", user.getId())
          .bind("email", user.getEmail())
          .bind("username", user.getUsername())
          .bind("password", user.getPassword())
          .bind("bio", user.getBio())
          .bind("image", user.getImage())
          .fetch()
          .rowsUpdated()
          .block();
    }
  }

  @Override
  public Optional<User> findById(String id) {
    return Optional.ofNullable(
        db.sql("SELECT id, email, username, password, bio, image FROM users WHERE id = :id")
            .bind("id", id)
            .map(row -> mapUser(row))
            .one()
            .block());
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return Optional.ofNullable(
        db.sql(
                "SELECT id, email, username, password, bio, image FROM users WHERE username ="
                    + " :username")
            .bind("username", username)
            .map(row -> mapUser(row))
            .one()
            .block());
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return Optional.ofNullable(
        db.sql("SELECT id, email, username, password, bio, image FROM users WHERE email = :email")
            .bind("email", email)
            .map(row -> mapUser(row))
            .one()
            .block());
  }

  @Override
  public void saveRelation(FollowRelation followRelation) {
    Boolean exists =
        db.sql("SELECT COUNT(*) FROM follows WHERE user_id = :userId AND follow_id = :followId")
            .bind("userId", followRelation.getUserId())
            .bind("followId", followRelation.getTargetId())
            .map(row -> row.get(0, Long.class) > 0)
            .one()
            .block();

    if (!Boolean.TRUE.equals(exists)) {
      db.sql("INSERT INTO follows (user_id, follow_id) VALUES (:userId, :followId)")
          .bind("userId", followRelation.getUserId())
          .bind("followId", followRelation.getTargetId())
          .fetch()
          .rowsUpdated()
          .block();
    }
  }

  @Override
  public Optional<FollowRelation> findRelation(String userId, String targetId) {
    return Optional.ofNullable(
        db.sql(
                "SELECT user_id, follow_id FROM follows WHERE user_id = :userId AND follow_id ="
                    + " :targetId")
            .bind("userId", userId)
            .bind("targetId", targetId)
            .map(
                row -> {
                  FollowRelation r = new FollowRelation();
                  r.setUserId(row.get("user_id", String.class));
                  r.setTargetId(row.get("follow_id", String.class));
                  return r;
                })
            .one()
            .block());
  }

  @Override
  public void removeRelation(FollowRelation followRelation) {
    db.sql("DELETE FROM follows WHERE user_id = :userId AND follow_id = :followId")
        .bind("userId", followRelation.getUserId())
        .bind("followId", followRelation.getTargetId())
        .fetch()
        .rowsUpdated()
        .block();
  }

  private User mapUser(io.r2dbc.spi.Readable row) {
    User user =
        new User(
            row.get("email", String.class),
            row.get("username", String.class),
            row.get("password", String.class),
            row.get("bio", String.class),
            row.get("image", String.class));
    user.setId(row.get("id", String.class));
    return user;
  }
}
