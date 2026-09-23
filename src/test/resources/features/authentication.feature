@authentication
Feature: Service authentication

  Scenario: Obtain a valid access token
  When the regression test suite requests an access token
  Then a valid access token should be returned