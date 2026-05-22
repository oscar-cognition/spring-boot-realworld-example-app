package io.spring.infrastructure.readservice;

import io.spring.application.data.UserData;
import lombok.AllArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class R2dbcUserReadService {
  private final DatabaseClient db;

  public UserData findByUsername(String username) {
    return db.sql("SELECT id, email, username, bio, image FROM users WHERE username = :username")
        .bind("username", username)
        .map(row -> mapUserData(row))
        .one()
        .block();
  }

  public UserData findById(String id) {
    return db.sql("SELECT id, email, username, bio, image FROM users WHERE id = :id")
        .bind("id", id)
        .map(row -> mapUserData(row))
        .one()
        .block();
  }

  private UserData mapUserData(io.r2dbc.spi.Readable row) {
    return new UserData(
        row.get("id", String.class),
        row.get("email", String.class),
        row.get("username", String.class),
        row.get("bio", String.class),
        row.get("image", String.class));
  }
}
