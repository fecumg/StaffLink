package fpt.edu.user_service.configurations.redis;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * @author Truong Duc Duong
 */

@Log4j2
public class RedisCacheErrorHandler implements CacheErrorHandler {
    @Override
    public void handleCacheGetError(@NotNull RuntimeException exception, Cache cache, @NotNull Object key) {
        handleTimeOutException(exception);
        log.error("Unable to get from cache " + cache.getName() + " : " + exception.getMessage());
    }

    @Override
    public void handleCachePutError(@NotNull RuntimeException exception, Cache cache, @NotNull Object key, Object value) {
        handleTimeOutException(exception);
        log.error("Unable to put into cache " + cache.getName() + " : " + exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(@NotNull RuntimeException exception, Cache cache, @NotNull Object key) {
        handleTimeOutException(exception);
        log.error("Unable to evict from cache " + cache.getName() + " : " + exception.getMessage());
    }

    @Override
    public void handleCacheClearError(@NotNull RuntimeException exception, Cache cache) {
        handleTimeOutException(exception);
        log.error("Unable to clear cache " + cache.getName() + " : " + exception.getMessage());
    }

    private void handleTimeOutException(RuntimeException exception) {

    }
}
