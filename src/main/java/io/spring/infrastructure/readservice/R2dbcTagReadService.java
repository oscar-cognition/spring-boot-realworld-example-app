package io.spring.infrastructure.readservice;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class R2dbcTagReadService {
  private final DatabaseClient db;

  public List<String> all() {
    return db.sql("SELECT DISTINCT name FROM tags ORDER BY name")
        .map(row -> row.get("name", String.class))
        .all()
        .collectList()
        .block();
  }
}
