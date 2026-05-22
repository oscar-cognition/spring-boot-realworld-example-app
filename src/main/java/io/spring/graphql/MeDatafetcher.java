package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.core.service.JwtService;
import io.spring.graphql.DgsConstants.QUERY;
import io.spring.graphql.DgsConstants.USERPAYLOAD;
import io.spring.graphql.types.User;
import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class MeDatafetcher {
  private UserQueryService userQueryService;
  private JwtService jwtService;

  @DgsData(parentType = DgsConstants.QUERY_TYPE, field = QUERY.Me)
  public DataFetcherResult<User> getMe(DataFetchingEnvironment dataFetchingEnvironment) {
    io.spring.core.user.User currentUser = SecurityUtil.getCurrentUser().orElse(null);
    if (currentUser == null) {
      return null;
    }
    UserData userData =
        userQueryService.findById(currentUser.getId()).orElseThrow(ResourceNotFoundException::new);
    String token = jwtService.toToken(currentUser);
    User result =
        User.newBuilder()
            .email(userData.getEmail())
            .username(userData.getUsername())
            .token(token)
            .build();
    return DataFetcherResult.<User>newResult().data(result).localContext(currentUser).build();
  }

  @DgsData(parentType = USERPAYLOAD.TYPE_NAME, field = USERPAYLOAD.User)
  public DataFetcherResult<User> getUserPayloadUser(
      DataFetchingEnvironment dataFetchingEnvironment) {
    io.spring.core.user.User user = dataFetchingEnvironment.getLocalContext();
    User result =
        User.newBuilder()
            .email(user.getEmail())
            .username(user.getUsername())
            .token(jwtService.toToken(user))
            .build();
    return DataFetcherResult.<User>newResult().data(result).localContext(user).build();
  }
}
