package com.pixology.backend.bdd.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pixology.backend.user.UserService;
import com.pixology.backend.user.dto.RegisterRequest;
import com.pixology.backend.user.dto.UserResponse;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.argThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserSteps {

    private final MockMvc mvc;
    private final ObjectMapper om;
    private final UserService service; // @MockBean from CucumberSpringConfig

    private org.springframework.test.web.servlet.ResultActions last;

    public UserSteps(MockMvc mvc, ObjectMapper om, UserService service) {
        this.mvc = mvc;
        this.om = om;
        this.service = service;
    }

    // Default stubs applied before each scenario.
    // Scenarios can override these in their @Given steps.
    @Before
    public void setupDefaults() {
        // Default: registration succeeds and echoes back normalized email (null-safe)
        given(service.register(any())).willAnswer(inv -> {
            RegisterRequest r = inv.getArgument(0, RegisterRequest.class);
            String username = (r != null && r.getUsername() != null) ? r.getUsername() : "n/a";
            String email = (r != null && r.getEmail() != null) ? r.getEmail().toLowerCase() : "n/a";
            return new UserResponse("u1", username, email);
        });

        // Default: login fails (empty). Specific scenarios override to "valid".
        given(service.login(anyString(), anyString())).willReturn(Optional.empty());
    }

    @When("I register with username {string} email {string} and password {string}")
    public void i_register(String username, String email, String password) throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setEmail(email);
        req.setPassword(password);

        last = mvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)));
    }

    @Given("an existing user with email {string}")
    public void existing_user(String email) {
        // Only throw duplicate for this specific email; other emails still use the default success stub
        given(service.register(argThat(req ->
                req != null && req.getEmail() != null && req.getEmail().equalsIgnoreCase(email)
        ))).willThrow(new DuplicateKeyException("email already exists"));
    }

    @Given("a valid account with email {string} and password {string}")
    public void valid_account(String email, String password) {
        // Override default login to succeed for this exact credential pair
        given(service.login(email, password))
                .willReturn(Optional.of(new UserResponse("u2", "B", email)));
    }

    @When("I login with email {string} and password {string}")
    public void i_login(String email, String password) throws Exception {
        last = mvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    @Then("the response status should be {int}")
    public void the_status_should_be(Integer code) throws Exception {
        last.andExpect(status().is(code));
    }
}
