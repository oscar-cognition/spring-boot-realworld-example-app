package io.spring;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Type;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.support.SerializableGraphQlRequest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers a Jackson message converter for Spring GraphQL requests that does not use
 * UNWRAP_ROOT_VALUE. The global UNWRAP_ROOT_VALUE setting (required by the RealWorld REST API spec
 * for @JsonRootName DTOs) causes Spring GraphQL's SerializableGraphQlRequest deserialization to
 * fail because it expects a root wrapper that GraphQL clients never send.
 */
@Configuration
public class GraphQlWebMvcConfig implements WebMvcConfigurer {

  @Autowired private ObjectMapper objectMapper;

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    ObjectMapper graphQlMapper = objectMapper.copy();
    graphQlMapper.disable(DeserializationFeature.UNWRAP_ROOT_VALUE);

    MappingJackson2HttpMessageConverter graphQlConverter =
        new MappingJackson2HttpMessageConverter(graphQlMapper) {
          @Override
          public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
            return SerializableGraphQlRequest.class.isAssignableFrom(clazz)
                && super.canRead(clazz, mediaType);
          }

          @Override
          public boolean canRead(
              Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType) {
            if (type instanceof Class<?> clz
                && SerializableGraphQlRequest.class.isAssignableFrom(clz)) {
              return super.canRead(type, contextClass, mediaType);
            }
            return false;
          }

          @Override
          public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
            return false;
          }
        };

    converters.add(0, graphQlConverter);
  }
}
