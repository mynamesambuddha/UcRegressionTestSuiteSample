package uc.mainframe.migration.regression.authentication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class JwtTokenClient {

    private final RestClient restClient;
    private final String authenticationUrl;
    private final String username;
    private final String password;

    public JwtTokenClient(
            RestClient.Builder restClientBuilder,
            @Value("${regression.authentication.url}")
            String authenticationUrl,
            @Value("${regression.authentication.username}")
            String username,
            @Value("${regression.authentication.password}")
            String password) {

        this.restClient = restClientBuilder.build();
        this.authenticationUrl = authenticationUrl;
        this.username = username;
        this.password = password;
    }

    
    public String getAccessToken() {

        TokenRequest tokenRequest =
                new TokenRequest(username, password);

        TokenResponse tokenResponse = restClient
                .post()
                .uri(authenticationUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(tokenRequest)
                .retrieve()
                .body(TokenResponse.class);

        if (tokenResponse == null
                || tokenResponse.accessToken() == null
                || tokenResponse.accessToken().isBlank()) {

            throw new IllegalStateException(
                    "Authentication service did not return an access token");
        }

        return tokenResponse.accessToken();
    }

    private record TokenRequest(
            String username,
            String password) {
    }

    private record TokenResponse(
            String accessToken,
            String tokenType,
            long expiresInSeconds) {
    }
}