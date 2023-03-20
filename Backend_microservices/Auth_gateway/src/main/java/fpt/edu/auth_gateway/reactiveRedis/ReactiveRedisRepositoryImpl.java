package fpt.edu.auth_gateway.reactiveRedis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * @author Truong Duc Duong
 */

@Component
public class ReactiveRedisRepositoryImpl implements ReactiveRedisRepository {

    @Autowired
    private ReactiveRedisOperations<String, Object> reactiveRedisOperations;

    @Override
    public Mono<Object> set(String key, Object hashKey, Object object) {
        return reactiveRedisOperations.opsForHash()
                .put(key, hashKey, object)
                .thenReturn(object);
    }

    @Override
    public Flux<Object> getAll(String key) {
        return reactiveRedisOperations.opsForHash().values(key);
    }

    @Override
    public Mono<Object> get(String key, Object hashKey) {
        return reactiveRedisOperations.opsForHash().get(key, hashKey);
    }

    @Override
    public Mono<Boolean> hasKey(String key, Object hashKey) {
        return reactiveRedisOperations.opsForHash().hasKey(key, hashKey);
    }

    @Override
    public Mono<Long> remove(String key, Object hashKey) {
        return reactiveRedisOperations.opsForHash().remove(key, hashKey);
    }
}
