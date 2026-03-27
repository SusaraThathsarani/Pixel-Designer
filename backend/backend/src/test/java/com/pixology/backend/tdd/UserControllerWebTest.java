package com.pixology.backend.tdd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pixology.backend.user.UserController;
import com.pixology.backend.user.UserService;
import com.pixology.backend.user.dto.LoginRequest;
import com.pixology.backend.user.dto.RegisterRequest;
import com.pixology.backend.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerWebTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockBean
    UserService service;

    @Test
    void register_returns_201_on_success() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("Alice");
        req.setEmail("alice@example.com");
        req.setPassword("secret123");

        when(service.register(any())).thenReturn(new UserResponse("u1","Alice","alice@example.com"));

        mvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("u1"))
                .andExpect(jsonPath("$.username").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void register_returns_409_on_duplicate() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("Alice");
        req.setEmail("alice@example.com");
        req.setPassword("secret123");

        when(service.register(any())).thenThrow(new DuplicateKeyException("email already exists"));

        mvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_returns_200_on_valid_credentials() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("alice@example.com");
        req.setPassword("secret123");

        when(service.login("alice@example.com","secret123"))
                .thenReturn(Optional.of(new UserResponse("u1","Alice","alice@example.com")));

        mvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("u1"));
    }

    @Test
    void login_returns_401_on_invalid_credentials() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("alice@example.com");
        req.setPassword("wrong123");

        when(service.login("alice@example.com","wrong123")).thenReturn(Optional.empty());

        mvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_returns_400_when_username_too_short() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("Al"); // only 2 chars, invalid
        req.setEmail("short@example.com");
        req.setPassword("secret123");

        mvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

}
