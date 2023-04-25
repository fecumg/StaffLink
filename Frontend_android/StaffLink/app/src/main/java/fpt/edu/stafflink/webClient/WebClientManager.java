package fpt.edu.stafflink.webClient;

import android.content.Context;
import android.content.SharedPreferences;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.exceptions.UnauthorizedException;
import fpt.edu.stafflink.response.ErrorApiResponse;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

public class WebClientManager {
    private static WebClient webclientInstance;

    private static WebClient generateWebclientInstance(Context context, String domain) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        String bearer = sharedPreferences.getString(context.getString(R.string.authorization_sharedPreference), "");

        return WebClient.builder()
                .baseUrl(domain)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.newConnection().compress(true)))
                .defaultHeader(context.getString(R.string.authorization_sharedPreference), bearer)
                .filter(errorHandlingFilter())
                .build();
    }

    public static WebClient getWebclientInstance(Context context) {
        if (webclientInstance == null) {
            webclientInstance = generateWebclientInstance(context, context.getString(R.string.default_domain));
        }
        return webclientInstance;
    }

    public static ExchangeFilterFunction errorHandlingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(
                clientResponse -> {
                    if(HttpStatus.UNAUTHORIZED.equals(clientResponse.statusCode())) {
                        return clientResponse.bodyToMono(ErrorApiResponse.class)
                                .flatMap(errorApiResponse -> Mono.error(new UnauthorizedException(errorApiResponse.getMessage())));
                    } else if (clientResponse.statusCode().isError()) {
                        return clientResponse.bodyToMono(ErrorApiResponse.class)
                                .flatMap(errorApiResponse -> Mono.error(new RuntimeException(errorApiResponse.getMessage())));
                    } else {
                        return Mono.just(clientResponse);
                    }
                }
        );
    }
}
