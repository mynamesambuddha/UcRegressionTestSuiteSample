package uc.mainframe.migration.regression.cucumber.steps;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import uc.mainframe.migration.regression.authentication.JwtTokenClient;

public class AuthenticationStepDefinitions {

    private final JwtTokenClient jwtTokenClient;

    private String accessToken;

    public AuthenticationStepDefinitions(
            JwtTokenClient jwtTokenClient) {

        this.jwtTokenClient = jwtTokenClient;
    }

    @When("the regression test suite requests an access token")
    public void requestAccessToken() {
        accessToken = jwtTokenClient.getAccessToken();
    }

    @Then("a valid access token should be returned")
    public void verifyAccessToken() {
        assertNotNull(
                accessToken,
                "The authentication service returned a null access token");

        assertFalse(
                accessToken.isBlank(),
                "The authentication service returned a blank access token");
    }
}