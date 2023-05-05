package fpt.edu.auth_gateway.security.services;

import fpt.edu.auth_gateway.dtos.ExchangeGuardedPath;
import fpt.edu.auth_gateway.dtos.ExchangeUser;
import fpt.edu.auth_gateway.reactiveRedis.ReactiveRedisRepository;
import fpt.edu.auth_gateway.security.authObjects.AuthenticatedUser;
import fpt.edu.auth_gateway.security.jwt.JwtUtilities;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author Truong Duc Duong
 */

@Service
@EnableScheduling
@Log4j2
public class AuthServiceImpl implements AuthService {

    private static final String AUTH_KEY = "authenticatedUser";
    private static final String PATHS_KEY = "allGuardedPaths";

    @Autowired
    private WebClient.Builder webClientBuilder;
    @Autowired
    private JwtUtilities jwtUtilities;
    @Autowired
    private ReactiveRedisRepository reactiveRedisRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private Environment env;

    @Override
    public Mono<AuthenticatedUser> loadUserByToken(String token) {
        if (jwtUtilities.validateToken(token)) {
            String username = jwtUtilities.getUsernameFromToken(token);
            return reactiveRedisRepository.get(AUTH_KEY, username)
                    .map(object -> modelMapper.map(object, AuthenticatedUser.class))
                    .doOnNext(authenticatedUser ->
                            log.info("Authentication info of user with username '{}' retrieved from redis cache", authenticatedUser.getUsername()))
                    .switchIfEmpty(
                            this.getUserFromUserService(username)
                                    .flatMap(authenticatedUser ->
                                            reactiveRedisRepository.set(AUTH_KEY, username, authenticatedUser)
                                                    .thenReturn(authenticatedUser))
                                    .doOnNext(authenticatedUser ->
                                            log.info("Authentication info of user with username '{}' has been added to redis cache", authenticatedUser.getUsername()))
                                    .onErrorResume(throwable -> {
                                            log.error(throwable.getMessage());
                                            return this.getUserFromUserService(username);
                                    })
                    )
                    .onErrorResume(throwable -> {
                            log.error(throwable.getMessage());
                            return this.getUserFromUserService(username);
                    });
        } else {
            return Mono.empty();
        }
    }

    private Mono<AuthenticatedUser> getUserFromUserService(String username) {
        return webClientBuilder.build()
                .get()
                .uri("http://user-service/userByUsername?username=" + username)
                .retrieve()
                .bodyToMono(ExchangeUser.class)
                .map(AuthenticatedUser::new)
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    private Flux<String> getAllGuardedPaths() {
        return reactiveRedisRepository.getAll(PATHS_KEY)
                .map(object -> modelMapper.map(object, String.class))
                .switchIfEmpty(
                        this.getGuardedPathsFromUserService()
                                .filter(exchangeGuardedPath -> StringUtils.hasText(exchangeGuardedPath.getUri()))
                                .flatMap(exchangeGuardedPath -> reactiveRedisRepository.set(PATHS_KEY, exchangeGuardedPath.getId(), exchangeGuardedPath.getUri())
                                        .thenReturn(exchangeGuardedPath))
                                .doOnNext(exchangeGuardedPath ->
                                        log.info("'{}' has been added to guarded path cache", exchangeGuardedPath.getUri()))
                                .onErrorResume(throwable -> {
                                        log.error(throwable.getMessage());
                                        return this.getGuardedPathsFromUserService();
                                })
                                .map(ExchangeGuardedPath::getUri)
                )
                .onErrorResume(throwable -> {
                        log.error(throwable.getMessage());
                        return this.getGuardedPathsFromUserService()
                                .map(ExchangeGuardedPath::getUri);
                });
    }

    private Flux<ExchangeGuardedPath> getGuardedPathsFromUserService() {
        return webClientBuilder.build()
                .get()
                .uri("http://user-service/allGuardedPaths")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ExchangeGuardedPath>>() {})
                .flatMapMany(Flux::fromIterable)
                .filter(exchangeGuardedPath -> StringUtils.hasText(exchangeGuardedPath.getUri()))
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Mono<Boolean> isGuardedPath(ServerWebExchange exchange) {
//        get requested core uri
        String requestedCoreUri = exchange.getRequest().getURI().getPath();
        AntPathMatcher antPathMatcher = new AntPathMatcher();

        return this.getAllGuardedPaths()
                .filter(StringUtils::hasText)
                .map(this::formatUri)
                .any(guardedPath -> antPathMatcher.match(guardedPath, requestedCoreUri));
    }

    @Override
    public boolean isAuthorized(ServerWebExchange exchange, AuthenticatedUser authenticatedUser) {
//        get requested core uri
        String requestedCoreUri = exchange.getRequest().getURI().getPath();
        AntPathMatcher antPathMatcher = new AntPathMatcher();

        return authenticatedUser.getAuthorizedUris().stream()
                .filter(StringUtils::hasText)
                .map(this::formatUri)
                .anyMatch(authorizedUri -> antPathMatcher.match(authorizedUri, requestedCoreUri));
    }

    @RabbitListener(queues = {"${rabbitmq.queue.new-auth-cache}"})
    private Mono<Void> newAuthenticationCache(ExchangeUser exchangeUser) {
        log.info("listen to message from queue '{}'", env.getProperty("rabbitmq.queue.new-auth-cache"));
        return Mono.just(exchangeUser)
                .map(AuthenticatedUser::new)
                .map(authenticatedUser -> reactiveRedisRepository.set(AUTH_KEY, authenticatedUser.getUsername(), authenticatedUser))
                .doOnError(throwable -> log.error(throwable.getMessage()))
                .then();
    }

    @RabbitListener(queues = {"${rabbitmq.queue.auth-cache-update}"})
    private Mono<Void> updateAuthenticationCache(List<ExchangeUser> exchangeUsers, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        log.info("listen to message from queue '{}', delivery tag: {}", env.getProperty("rabbitmq.queue.auth-cache-update"), tag);
        return Mono.just(exchangeUsers)
                .flatMapMany(Flux::fromIterable)
                .flatMap(exchangeUser ->
                        reactiveRedisRepository.hasKey(AUTH_KEY, exchangeUser.getUsername())
                                .filter(bool -> bool)
                                .map(bool -> exchangeUser))
                .map(AuthenticatedUser::new)
                .flatMap(authenticatedUser ->
                        reactiveRedisRepository.set(AUTH_KEY, authenticatedUser.getUsername(), authenticatedUser)
                        .thenReturn(authenticatedUser))
                .doOnNext(authenticatedUser ->
                        log.info("Authentication info in redis cache hashmap with key '{}' updated", authenticatedUser.getUsername()))
                .doOnError(throwable -> log.error(throwable.getMessage()))
                .then();
    }

    @RabbitListener(queues = {"${rabbitmq.queue.auth-cache-delete}"})
    private Mono<Void> deleteAuthenticationCache(String username) {
        log.info("listen to message from queue '{}'", env.getProperty("rabbitmq.queue.auth-cache-delete"));
        return reactiveRedisRepository.hasKey(AUTH_KEY, username)
                .filter(bool -> bool)
                .map(bool -> reactiveRedisRepository.remove(AUTH_KEY, username))
                .doOnError(throwable -> log.error(throwable.getMessage()))
                .then();
    }

    @RabbitListener(queues = {"${rabbitmq.queue.guarded-path-update}"})
    private Mono<Void> updateGuardedPathCache(ExchangeGuardedPath exchangeGuardedPath) {
        log.info("listen to message from queue '{}'", env.getProperty("rabbitmq.queue.guarded-path-update"));
        return reactiveRedisRepository.set(PATHS_KEY, exchangeGuardedPath.getId(), exchangeGuardedPath.getUri())
                .doOnError(throwable -> log.error(throwable.getMessage()))
                .then();
    }

    @RabbitListener(queues = {"${rabbitmq.queue.guarded-path-delete}"})
    private Mono<Void> deleteGuardedPathCache(int guardedPathId) {
        log.info("listen to message from queue '{}'", env.getProperty("rabbitmq.queue.guarded-path-delete"));
        return reactiveRedisRepository.hasKey(PATHS_KEY, guardedPathId)
                .filter(bool -> bool)
                .flatMap(bool -> {

                    System.out.println(PATHS_KEY + " *** " + guardedPathId);

                    return reactiveRedisRepository.remove(PATHS_KEY, guardedPathId);
                })
                .doOnError(throwable -> log.error(throwable.getMessage()))
                .then();
    }

    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Ho_Chi_Minh")
//    @Scheduled(fixedDelay = 2000)
    private void syncAuthData() {
        reactiveRedisRepository.getAll(AUTH_KEY)
                .map(object -> modelMapper.map(object, AuthenticatedUser.class))
                .subscribe(authenticatedUser ->
                        this.getUserFromUserService(authenticatedUser.getUsername())
                                .flatMap(syncedAuthenticatedUser ->
                                        reactiveRedisRepository.set(AUTH_KEY, syncedAuthenticatedUser.getUsername(), syncedAuthenticatedUser)
                                                .thenReturn(syncedAuthenticatedUser))
                                .subscribe(syncedAuthenticatedUser ->
                                        log.info("Authentication redis cache of user with username '{}' has been synced with data from User-service", syncedAuthenticatedUser.getUsername())));
    }

    @Scheduled(cron = "0 1 2 * * ?", zone = "Asia/Ho_Chi_Minh")
//    @Scheduled(fixedDelay = 2000)
    private void syncGuardedPathData() {
        reactiveRedisRepository.getAll(PATHS_KEY)
                .map(object -> modelMapper.map(object, String.class))
                .subscribe(
                        path -> reactiveRedisRepository.remove(PATHS_KEY, path).subscribe(),
                        error -> log.error(error.getMessage()),
                        () -> this.getAllGuardedPaths()
                                .subscribe(data -> log.info("Guarded path cache synced"))
                );
    }

    private String formatUri(String uri) {
        uri = formatUriPrefix(uri);
        uri = formatUriSuffix(uri);
        return uri;
    }

    private String formatUriPrefix(String uri) {
        if (!StringUtils.hasText(uri)) {
            return uri;
        }
        if (!uri.startsWith("/")) {
            return  "/" + uri;
        } else {
            return formatUriPrefix(uri.substring(1));
        }
    }

    private String formatUriSuffix(String uri) {
        if (!StringUtils.hasText(uri)) {
            return uri;
        }
        if (!uri.endsWith("/")) {
            return uri;
        } else {
            return formatUriSuffix(uri.substring(0, uri.length() - 1));
        }
    }
}