package com.linkvault.integration.link;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkvault.constants.apiPaths.AuthEndpoints;
import com.linkvault.constants.apiPaths.LinkEndpoints;
import com.linkvault.model.Link;
import com.linkvault.model.User;
import com.linkvault.repository.LinkRepository;
import com.linkvault.repository.UserRepository;
import com.linkvault.unit.util.TestConstants;
import com.linkvault.unit.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
public class LinkControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LinkRepository linkRepository;

    @BeforeEach()
    void setUp() {
        linkRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnLinksForUser_WhenAuthenticated() throws Exception {
        // Arrange
        String json = """
            {
                "username": "validUsername",
                "password": "validPassword1@"
            }
            """;

        // Act & Assert
        mockMvc.perform(post(AuthEndpoints.BASE_AUTH + AuthEndpoints.REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(post(AuthEndpoints.BASE_AUTH + AuthEndpoints.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(responseBody);
        String token = jsonNode.get("token").asText();

        User user = userRepository.findByUsername("validUsername").orElseThrow();
        Link link1 = new Link("https://github.com", "Git Hub",
            "Repositories", user);
        Link link2 = new Link("https://spring.io", "Spring Boot",
            "Learning Spring Boot", user);

        linkRepository.saveAll(List.of(link1, link2));

        mockMvc.perform(get(LinkEndpoints.BASE_LINKS)
            .header(TestConstants.AUTHORIZATION, TestConstants.BEARER + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value(link1.getTitle()))
            .andExpect(jsonPath("$[1].title").value(link2.getTitle()));
    }

    @Test
    void shouldReturnUnauthorizedForUser_WhenFetchingLinksWithoutAToken() throws Exception {
        mockMvc.perform(get(LinkEndpoints.BASE_LINKS))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedForUser_WhenFetchingLinksWithMalformedToken() throws Exception {
        mockMvc.perform(get(LinkEndpoints.BASE_LINKS)
                .header(TestConstants.AUTHORIZATION, TestConstants.BEARER + "malformed.token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnForbidden_WhenUserTriesToDeleteAnotherUsersLink() throws Exception {
        // Arrange
        String jsonForUserA = """
            {
                "username": "validUsername1",
                "password": "validPassword1@"
            }
            """;

        String jsonForUserB = """
            {
                "username": "validUsername2",
                "password": "validPassword2@"
            }
            """;

        // Act & Assert
        mockMvc.perform(post(AuthEndpoints.BASE_AUTH + AuthEndpoints.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonForUserA))
            .andExpect(status().isOk());

        mockMvc.perform(post(AuthEndpoints.BASE_AUTH + AuthEndpoints.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonForUserB))
            .andExpect(status().isOk());

        mockMvc.perform(post(AuthEndpoints.BASE_AUTH + AuthEndpoints.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonForUserB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andReturn();

        User userB = userRepository.findByUsername("validUsername2").orElseThrow();
        Link link1 = new Link("https://github.com", "Git Hub",
            "Repositories", userB);
        linkRepository.save(link1);

        MvcResult result = mockMvc.perform(post(AuthEndpoints.BASE_AUTH + AuthEndpoints.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonForUserA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(responseBody);
        String userAToken = jsonNode.get("token").asText();

        userRepository.findByUsername("validUsername1").orElseThrow();

        String link1IdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, link1.getId());

        mockMvc.perform(delete(link1IdPath)
            .header(TestConstants.AUTHORIZATION, TestConstants.BEARER + userAToken))
            .andExpect(status().isForbidden());
    }
}
