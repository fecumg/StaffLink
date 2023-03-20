package fpt.edu.auth_gateway.reactiveRedis;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Truong Duc Duong
 */

@Component
public interface ReactiveRedisRepository {
    Mono<Object> set(@NotNull String key, @NotNull Object hashKey, @NotNull Object object);
    Flux<Object> getAll(@NotNull String key);
    Mono<Object> get(@NotNull String key, @NotNull Object hashKey);
    Mono<Boolean> hasKey(@NotNull String key, @NotNull Object hashKey);
    Mono<Long> remove(@NotNull String key, @NotNull Object hashKey);


}
