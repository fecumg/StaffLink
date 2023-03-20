package fpt.edu.auth_gateway.security.services;

import fpt.edu.auth_gateway.dtos.ExchangeUser;
import fpt.edu.auth_gateway.reactiveRedis.ReactiveRedisRepository;
import fpt.edu.auth_gateway.security.authObjects.AuthenticatedUser;
import fpt.edu.auth_gateway.security.jwt.JwtUtilities;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
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
                .switchIfEmpty(this.getGuardedPathsFromUserService()
                        .flatMap(path -> reactiveRedisRepository.set(PATHS_KEY, path, path)
                                .thenReturn(path))
                        .doOnNext(path ->
                                log.info("'{}' has been added to guarded path cache", path))
                        .onErrorResume(throwable -> {
                                log.error(throwable.getMessage());
                                return this.getGuardedPathsFromUserService();
                        }))
                .onErrorResume(throwable -> {
                        log.error(throwable.getMessage());
                        return this.getGuardedPathsFromUserService();
                });
    }

    private Flux<String> getGuardedPathsFromUserService() {
        return webClientBuilder.build()
                .get()
                .uri("http://user-service/allGuardedPaths")
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(throwable -> log.error(throwable.getMessage()));
    }

    @Override
    public Mono<Boolean> isGuardedPath(ServerWebExchange exchange) {
        return this.getAllGuardedPaths()
                .any(guardedPath -> guardedPath.equals(exchange.getRequest().getURI().getPath()));
    }

    @Override
    public boolean isAuthorized(ServerWebExchange exchange, AuthenticatedUser authenticatedUser) {
//        get requested core uri
        String requestedCoreUri = exchange.getRequest().getURI().getPath();
        System.out.println(requestedCoreUri);

        return authenticatedUser.getAuthorizedUris().stream()
                .anyMatch(authorizedUri -> authorizedUri.equals(requestedCoreUri));
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
    private Mono<Void> updateGuardedPathCache(String guardedPath) {
        log.info("listen to message from queue '{}'", env.getProperty("rabbitmq.queue.guarded-path-update"));
        return reactiveRedisRepository.set(PATHS_KEY, guardedPath, guardedPath)
                .doOnError(throwable -> log.error(throwable.getMessage()))
                .then();
    }

    @RabbitListener(queues = {"${rabbitmq.queue.guarded-path-delete}"})
    private Mono<Void> deleteGuardedPathCache(String guardedPath) {
        log.info("listen to message from queue '{}'", env.getProperty("rabbitmq.queue.guarded-path-delete"));
        return reactiveRedisRepository.hasKey(PATHS_KEY, guardedPath)
                .filter(bool -> bool)
                .map(bool -> reactiveRedisRepository.remove(PATHS_KEY, guardedPath))
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
}