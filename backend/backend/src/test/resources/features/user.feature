Feature: User registration and login

  Scenario: Successful registration
    When I register with username "Alice" email "alice@example.com" and password "secret123"
    Then the response status should be 201

  Scenario: Duplicate registration
    Given an existing user with email "alice@example.com"
    When I register with username "Alice" email "alice@example.com" and password "secret123"
    Then the response status should be 409

  Scenario: Valid login
    Given a valid account with email "alice@example.com" and password "secret123"
    When I login with email "alice@example.com" and password "secret123"
    Then the response status should be 200

  Scenario: Invalid login
    When I login with email "no@example.com" and password "badbad"
    Then the response status should be 401

  Scenario: Registration fails when username is too short
    When I register with username "Al" email "short@example.com" and password "secret123"
    Then the response status should be 400
