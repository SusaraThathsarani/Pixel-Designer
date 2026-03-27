package com.pixology.backend.api;

import com.pixology.backend.user.UserService;
import com.pixology.backend.user.dto.UserResponse;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@AutoConfigureMockMvc
class UserApiRestAssuredTest {

    @MockBean
    private UserService userService;

    @BeforeEach
    void setup(WebApplicationContext ctx) {
        RestAssuredMockMvc.mockMvc(
                MockMvcBuilders.webAppContextSetup(ctx).build()
        );
        Mockito.reset(userService);
    }

    @Test
    void registration_returns_201_and_payload() throws Exception {
        given(userService.register(any())).willAnswer(inv -> {
            var req = inv.getArgument(0);
            String username = (String) req.getClass().getMethod("getUsername").invoke(req);
            String email = ((String) req.getClass().getMethod("getEmail").invoke(req)).toLowerCase();
            return new UserResponse("u1", username, email);
        });

        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("username","Alice","email","Alice@EXAMPLE.COM","password","secret123"))
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(201)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("id", equalTo("u1"))
                .body("username", equalTo("Alice"))
                .body("email", equalTo("alice@example.com"));
    }

    @Test
    void duplicate_registration_returns_409() {
        given(userService.register(any())).willThrow(new DuplicateKeyException("email exists"));

        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("username","Alice","email","alice@example.com","password","secret123"))
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(409);
    }

    @Test
    void login_success_returns_200_and_payload() {
        given(userService.login(eq("alice@example.com"), eq("secret123")))
                .willReturn(Optional.of(new UserResponse("u2","Alice","alice@example.com")));

        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("email","alice@example.com","password","secret123"))
                .when()
                .post("/api/users/login")
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("id", equalTo("u2"))
                .body("username", equalTo("Alice"))
                .body("email", equalTo("alice@example.com"));
    }

    @Test
    void login_invalid_returns_401() {
        given(userService.login(anyString(), anyString()))
                .willReturn(Optional.empty());

        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("email","no@example.com","password","badbad"))
                .when()
                .post("/api/users/login")
                .then()
                .statusCode(401);
    }

    @Test
    void registration_with_short_username_returns_400() {
        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("username","Al","email","short@example.com","password","secret123"))
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(400);
    }

}
