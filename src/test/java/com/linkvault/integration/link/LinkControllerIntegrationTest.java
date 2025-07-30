package com.linkvault.integration.link;

import com.linkvault.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
public class LinkControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    UserServiceImpl userServiceImpl;

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Test
    void shouldReturnLinksForUser_WhenAuthenticated() throws Exception {
        // Register test user
        // Login with test user → get JWT
        // Save a link for the user
        // GET /api/links/{userId} with JWT → Expect 200 and user’s links
    }

}
