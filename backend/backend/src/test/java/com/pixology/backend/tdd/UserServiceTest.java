package com.pixology.backend.tdd;

import com.pixology.backend.user.User;
import com.pixology.backend.user.UserRepository;
import com.pixology.backend.user.UserService;
import com.pixology.backend.user.dto.RegisterRequest;
import com.pixology.backend.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    UserRepository repo;
    UserService service;

    @BeforeEach
    void setUp() {
        repo = mock(UserRepository.class);
        service = new UserService(repo);
    }

    @Test
    void register_creates_user_when_unique_and_valid() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("Sath");
        req.setEmail("SATH@example.com");
        req.setPassword("secret123");

        when(repo.existsByUsername("Sath")).thenReturn(false);
        when(repo.existsByEmail("sath@example.com")).thenReturn(false);
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        when(repo.save(saved.capture())).thenAnswer(inv -> {
            User u = saved.getValue();
            u.setId("u1");
            return u;
        });

        UserResponse res = service.register(req);

        assertThat(res.getId()).isEqualTo("u1");
        assertThat(res.getUsername()).isEqualTo("Sath");
        assertThat(res.getEmail()).isEqualTo("sath@example.com");
        assertThat(saved.getValue().getPasswordHash()).isNotBlank();
    }

    @Test
    void register_rejects_blank_fields() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(" ");
        req.setEmail(" ");
        req.setPassword(" ");

        assertThatThrownBy(() -> service.register(req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void register_rejects_duplicate_username() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("A");
        req.setEmail("a@example.com");
        req.setPassword("secret123");

        when(repo.existsByUsername("A")).thenReturn(true);

        assertThatThrownBy(() -> service.register(req))
                .isInstanceOf(DuplicateKeyException.class)
                .hasMessageContaining("username");
    }

    @Test
    void register_rejects_duplicate_email() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("A");
        req.setEmail("a@example.com");
        req.setPassword("secret123");

        when(repo.existsByUsername("A")).thenReturn(false);
        when(repo.existsByEmail("a@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(req))
                .isInstanceOf(DuplicateKeyException.class)
                .hasMessageContaining("email");
    }

    @Test
    void authenticate_matches_hashed_password() {
        User u = new User("A", "a@example.com", org.springframework.security.crypto.bcrypt.BCrypt.hashpw("secret123", org.springframework.security.crypto.bcrypt.BCrypt.gensalt(4)));
        when(repo.findByEmail("a@example.com")).thenReturn(Optional.of(u));

        Optional<User> out = service.authenticate("a@example.com", "secret123");

        assertThat(out).isPresent();
    }

    @Test
    void authenticate_rejects_wrong_password() {
        User u = new User("A", "a@example.com", org.springframework.security.crypto.bcrypt.BCrypt.hashpw("secret123", org.springframework.security.crypto.bcrypt.BCrypt.gensalt(4)));
        when(repo.findByEmail("a@example.com")).thenReturn(Optional.of(u));

        Optional<User> out = service.authenticate("a@example.com", "nope");

        assertThat(out).isEmpty();
    }
}
