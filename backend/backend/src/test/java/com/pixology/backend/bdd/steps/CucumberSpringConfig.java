package com.pixology.backend.bdd.steps;

import com.pixology.backend.PixologyBackendApplication;
import com.pixology.backend.user.UserService;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@CucumberContextConfiguration
@SpringBootTest(classes = PixologyBackendApplication.class)
@AutoConfigureMockMvc
public class CucumberSpringConfig {

    @MockBean
    UserService userService;
}
