package com.linkvault.integration.link;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkvault.constants.apiPaths.LinkEndpoints;
import com.linkvault.integration.util.IntegrationTestFactory;
import com.linkvault.integration.util.JwtTestTokenFactory;
import com.linkvault.model.User;
import com.linkvault.repository.LinkRepository;
import com.linkvault.repository.UserRepository;
import com.linkvault.unit.util.TestConstants;
import com.linkvault.unit.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

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

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach()
    void setUp() {
        linkRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnLinksForUser_WhenAuthenticated() throws Exception {
        // Arrange
        String json = IntegrationTestFactory.createJsonForUser(
            "validUsername", "validPassword1@"
        );

        // Act & Assert
        IntegrationTestFactory.performJsonRegisterUserRequest(mockMvc, json);

        MvcResult result = IntegrationTestFactory.performJsonUserLoginRequest(mockMvc, json);
        String token = IntegrationTestFactory.getUserTokenFromJsonResponse(result, mapper);

        String createJsonOne = IntegrationTestFactory.createJsonForLink(
        "https://docs.oracle.com",
        "Java docs",
        "Java documentation"
        );

        String createJsonTwo = IntegrationTestFactory.createJsonForLink(
            "https://github.com",
            "Git Hub",
            "Repositories"
        );

        ResultActions resultOne = IntegrationTestFactory.performJsonCreateLinkRequest(
            mockMvc, token,
            createJsonOne
        );
        MvcResult linkResponseOne = IntegrationTestFactory.assertLinkCreateValidationSuccess(
            resultOne,
            "https://docs.oracle.com",
            "Java docs"
        );

        ResultActions resultTwo = IntegrationTestFactory.performJsonCreateLinkRequest(
            mockMvc, token,
            createJsonTwo
        );

        MvcResult linkResponseTwo = IntegrationTestFactory.assertLinkCreateValidationSuccess(
            resultTwo,
            "https://github.com",
            "Git Hub"
        );

        String linkResponseBodyOne = linkResponseOne.getResponse().getContentAsString();
        JsonNode linkResponseJsonNodeOne = mapper.readTree(linkResponseBodyOne);
        String linkUrlOne = linkResponseJsonNodeOne.get("url").asText();
        String linkTitleOne = linkResponseJsonNodeOne.get("title").asText();

        String linkResponseBodyTwo = linkResponseTwo.getResponse().getContentAsString();
        JsonNode linkResponseJsonNodeTwo = mapper.readTree(linkResponseBodyTwo);
        String linkUrlTwo = linkResponseJsonNodeTwo.get("url").asText();
        String linkTitleTwo = linkResponseJsonNodeTwo.get("title").asText();

        mockMvc.perform(get(LinkEndpoints.BASE_LINKS)
            .header(TestConstants.AUTHORIZATION, TestConstants.BEARER + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].url").value(linkUrlOne))
            .andExpect(jsonPath("$[0].title").value(linkTitleOne))
            .andExpect(jsonPath("$[1].url").value(linkUrlTwo))
            .andExpect(jsonPath("$[1].title").value(linkTitleTwo));
    }

    @Test
    void shouldReturnOkWithEmptyList_WhenUserHasNoOwnedLinks() throws Exception {
        // Arrange
        String jsonForUserA = IntegrationTestFactory.createJsonForUser(
            "validUsername1", "validPassword1@"
        );

        // Act & Assert
        IntegrationTestFactory.performJsonRegisterUserRequest(mockMvc, jsonForUserA);

        MvcResult result = IntegrationTestFactory.performJsonUserLoginRequest(mockMvc, jsonForUserA);
        String userAToken = IntegrationTestFactory.getUserTokenFromJsonResponse(result, mapper);

        mockMvc.perform(get(LinkEndpoints.BASE_LINKS)
                .header(TestConstants.AUTHORIZATION, TestConstants.BEARER + userAToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnUnauthorizedForUser_WhenFetchingLinksWithoutAToken() throws Exception {
        IntegrationTestFactory.performUnauthorizedJsonRequestWithoutHeader(
            mockMvc,
            HttpMethod.GET,
            LinkEndpoints.BASE_LINKS
        );
    }

    @Test
    void shouldReturnUnauthorizedForUser_WhenFetchingLinksWithMalformedToken() throws Exception {
        IntegrationTestFactory.performUnauthorizedJsonRequest(
            mockMvc,
            HttpMethod.GET,
            LinkEndpoints.BASE_LINKS,
            "malformed.token"
        );
    }

    @Test
    void shouldReturnLinkForUser_WhenAuthenticated() throws Exception {
        // Arrange
        String json = IntegrationTestFactory.createJsonForUser(
            "validUsername",
            "validPassword1@"
        );

        // Act & Assert
        IntegrationTestFactory.performJsonRegisterUserRequest(mockMvc, json);

        MvcResult result = IntegrationTestFactory.performJsonUserLoginRequest(mockMvc, json);
        String token = IntegrationTestFactory.getUserTokenFromJsonResponse(result, mapper);

        String createJson = IntegrationTestFactory.createJsonForLink(
            "https://docs.oracle.com",
            "Java docs",
            "Java documentation"
        );

        ResultActions linkCreateResult = IntegrationTestFactory.performJsonCreateLinkRequest(
            mockMvc, token,
            createJson
        );

        MvcResult linkResponse = IntegrationTestFactory.assertLinkCreateValidationSuccess(
            linkCreateResult,
            "https://docs.oracle.com",
            "Java docs"
        );

        JsonNode linkResponseJsonNode = IntegrationTestFactory.getLinkResponseJsonNodeFromBody(
            linkResponse,
            mapper
        );

        long linkId = linkResponseJsonNode.get("id").asLong();
        String linkUrl = linkResponseJsonNode.get("url").asText();
        String linkTitle = linkResponseJsonNode.get("title").asText();

        String link1IdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkId);

        mockMvc.perform(get(link1IdPath)
                .header(TestConstants.AUTHORIZATION, TestConstants.BEARER + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.url").value(linkUrl))
            .andExpect(jsonPath("$.title").value(linkTitle));
    }

    @Test
    void shouldReturnForbidden_WhenUserTriesToFetchAnotherUsersLink() throws Exception {
        // Arrange
        String jsonForUserA = IntegrationTestFactory.createJsonForUser(
            "validUsername1",
            "validPassword1@"
        );
        String jsonForUserB = IntegrationTestFactory.createJsonForUser(
            "validUsername2",
            "validPassword2@"
        );

        // Act & Assert
        IntegrationTestFactory.performJsonRegisterUserRequest(mockMvc, jsonForUserA);
        IntegrationTestFactory.performJsonRegisterUserRequest(mockMvc, jsonForUserB);

        MvcResult resultForUserB = IntegrationTestFactory.performJsonUserLoginRequest(mockMvc, jsonForUserB);
        String userBToken = IntegrationTestFactory.getUserTokenFromJsonResponse(resultForUserB, mapper);

        String createJson = IntegrationTestFactory.createJsonForLink(
            "https://docs.oracle.com",
            "Java docs",
            "Java documentation"
        );

        ResultActions linkCreateResult = IntegrationTestFactory.performJsonCreateLinkRequest(
            mockMvc, userBToken,
            createJson
        );

        MvcResult linkResponse = IntegrationTestFactory.assertLinkCreateValidationSuccess(
            linkCreateResult,
            "https://docs.oracle.com",
            "Java docs"
        );

        JsonNode linkResponseJsonNode = IntegrationTestFactory.getLinkResponseJsonNodeFromBody(
            linkResponse,
            mapper
        );

        long linkId = linkResponseJsonNode.get("id").asLong();

        MvcResult resultForUserA = IntegrationTestFactory.performJsonUserLoginRequest(mockMvc, jsonForUserA);
        String userAToken = IntegrationTestFactory.getUserTokenFromJsonResponse(resultForUserA, mapper);

        String link1IdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkId);

        mockMvc.perform(get(link1IdPath)
                .header(TestConstants.AUTHORIZATION, TestConstants.BEARER + userAToken))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnNotFound_WhenUserFetchesALinkThatDoesntExist() throws Exception {
        // Arrange
        String jsonForUserA = IntegrationTestFactory.createJsonForUser(
            "validUsername1",
            "validPassword1@"
        );

        // Act & Assert
        IntegrationTestFactory.performJsonRegisterUserRequest(mockMvc, jsonForUserA);

        MvcResult result = IntegrationTestFactory.performJsonUserLoginRequest(mockMvc, jsonForUserA);
        String userAToken = IntegrationTestFactory.getUserTokenFromJsonResponse(result, mapper);

        String link1IdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, 999999999L);

        mockMvc.perform(get(link1IdPath)
            .header(TestConstants.AUTHORIZATION, TestConstants.BEARER + userAToken))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnUnauthorizedForUser_WhenFetchingLinkWithoutAToken() throws Exception {
        String link1IdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, 1L);

        IntegrationTestFactory.performUnauthorizedJsonRequestWithoutHeader(
            mockMvc,
            HttpMethod.GET,
            link1IdPath
        );
    }

    @Test
    void shouldReturnUnauthorizedForUser_WhenFetchingLinkWithMalformedToken() throws Exception {
        String link1IdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, 1L);

        IntegrationTestFactory.performUnauthorizedJsonRequest(
            mockMvc,
            HttpMethod.GET,
            link1IdPath,
            "malformed.token"
        );
    }

    @Test
    void shouldReturnForbidden_WhenUserTriesToDeleteAnotherUsersLink() throws Exception {
        // Arrange
        String jsonForUserA = IntegrationTestFactory.createJsonForUser(
            "validUsername1",
            "validPassword1@"
        );
        String jsonForUserB = IntegrationTestFactory.createJsonForUser(
            "validUsername2",
            "validPassword2@"
        );

        // Act & Assert
        IntegrationTestFactory.performJsonRegisterUserRequest(mockMvc, jsonForUserA);
        IntegrationTestFactory.performJsonRegisterUserRequest(mockMvc, jsonForUserB);

        MvcResult resultForUserB = IntegrationTestFactory.performJsonUserLoginRequest(mockMvc, jsonForUserB);
        String userBToken = IntegrationTestFactory.getUserTokenFromJsonResponse(resultForUserB, mapper);

        String createJson = IntegrationTestFactory.createJsonForLink(
            "https://docs.oracle.com",
            "Java docs",
            "Java documentation"
        );

        ResultActions linkCreateResult = IntegrationTestFactory.performJsonCreateLinkRequest(
            mockMvc, userBToken,
            createJson
        );

        MvcResult linkResponse = IntegrationTestFactory.assertLinkCreateValidationSuccess(
            linkCreateResult,
            "https://docs.oracle.com",
            "Java docs"
        );

        JsonNode linkResponseJsonNode = IntegrationTestFactory.getLinkResponseJsonNodeFromBody(
            linkResponse,
            mapper
        );

        long linkId = linkResponseJsonNode.get("id").asLong();

        MvcResult resultForUserA = IntegrationTestFactory.performJsonUserLoginRequest(mockMvc, jsonForUserA);
        String userAToken = IntegrationTestFactory.getUserTokenFromJsonResponse(resultForUserA, mapper);

        String link1IdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkId);

        mockMvc.perform(delete(link1IdPath)
            .header(TestConstants.AUTHORIZATION, TestConstants.BEARER + userAToken))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForUser_WhenDeletingLinkWithoutAToken() throws Exception {
        String link1IdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, 1L);

        IntegrationTestFactory.performUnauthorizedJsonRequestWithoutHeader(
            mockMvc,
            HttpMethod.DELETE,
            link1IdPath
        );
    }

    @Test
    void shouldReturnUnauthorizedForUser_WhenDeletingLinkWithMalformedToken() throws Exception {
        String link1IdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, 1L);

        IntegrationTestFactory.performUnauthorizedJsonRequest(
            mockMvc,
            HttpMethod.DELETE,
            link1IdPath,
            "malformed.token"
        );
    }

    @Test
    void shouldReturnNoContent_WhenUserTriesToDeleteEmptyLinkList() throws Exception {
        // Arrange
        String jsonForUserA = IntegrationTestFactory.createJsonForUser(
            "validUsername1",
            "validPassword1@"
        );

        // Act & Assert
        IntegrationTestFactory.performJsonRegisterUserRequest(mockMvc, jsonForUserA);

        MvcResult result = IntegrationTestFactory.performJsonUserLoginRequest(mockMvc, jsonForUserA);
        String userAToken = IntegrationTestFactory.getUserTokenFromJsonResponse(result, mapper);

        mockMvc.perform(delete(LinkEndpoints.BASE_LINKS)
                .header(TestConstants.AUTHORIZATION, TestConstants.BEARER + userAToken))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnUnauthorizedForUser_WhenDeletingLinksWithoutAToken() throws Exception {
        IntegrationTestFactory.performUnauthorizedJsonRequestWithoutHeader(
            mockMvc,
            HttpMethod.DELETE,
            LinkEndpoints.BASE_LINKS
        );
    }

    @Test
    void shouldReturnUnauthorizedForUser_WhenDeletingLinksWithMalformedToken() throws Exception {
        IntegrationTestFactory.performUnauthorizedJsonRequest(
            mockMvc,
            HttpMethod.DELETE,
            LinkEndpoints.BASE_LINKS,
            "malformed.token"
        );
    }

    @Test
    void shouldReturnUnauthorized_WhenTokenIsExpired() throws Exception {
        String expiredToken = JwtTestTokenFactory.buildExpiredToken(jwtSecret);

        IntegrationTestFactory.performUnauthorizedJsonRequest(
            mockMvc,
            HttpMethod.DELETE,
            LinkEndpoints.BASE_LINKS,
            expiredToken
        );
    }

    @Test
    void shouldReturnForbidden_WhenUserTriesToUpdateAnotherUsersLink() throws Exception {
        // Arrange
        String jsonForUserA = IntegrationTestFactory.createJsonForUser(
            "validUsername1",
            "validPassword1@"
        );
        String jsonForUserB = IntegrationTestFactory.createJsonForUser(
            "validUsername2",
            "validPassword2@"
        );

        // Act & Assert
        IntegrationTestFactory.performJsonRegisterUserRequest(mockMvc, jsonForUserA);
        IntegrationTestFactory.performJsonRegisterUserRequest(mockMvc, jsonForUserB);

        MvcResult resultForUserB = IntegrationTestFactory.performJsonUserLoginRequest(mockMvc, jsonForUserB);
        String userBToken = IntegrationTestFactory.getUserTokenFromJsonResponse(resultForUserB, mapper);

        String createJson = IntegrationTestFactory.createJsonForLink(
            "https://docs.oracle.com",
            "Java docs",
            "Java documentation"
        );

        ResultActions linkCreateResult = IntegrationTestFactory.performJsonCreateLinkRequest(
            mockMvc, userBToken,
            createJson
        );

        MvcResult linkResponse = IntegrationTestFactory.assertLinkCreateValidationSuccess(
            linkCreateResult,
            "https://docs.oracle.com",
            "Java docs"
        );

        JsonNode linkResponseJsonNode = IntegrationTestFactory.getLinkResponseJsonNodeFromBody(
            linkResponse,
            mapper
        );

        long linkId = linkResponseJsonNode.get("id").asLong();

        String updateLinkJson = IntegrationTestFactory.createJsonForLink(
            "https://updated.com",
            "Updated Title",
            "Updated Description"
        );

        MvcResult resultForUserA = IntegrationTestFactory.performJsonUserLoginRequest(mockMvc, jsonForUserA);
        String userAToken = IntegrationTestFactory.getUserTokenFromJsonResponse(resultForUserA, mapper);

        String link1IdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkId);

        mockMvc.perform(put(link1IdPath)
                .header(TestConstants.AUTHORIZATION, TestConstants.BEARER + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateLinkJson))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedForUser_WhenUpdatingLinkWithoutAToken() throws Exception {
        String link1IdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, 1L);

        IntegrationTestFactory.performUnauthorizedJsonRequestWithoutHeader(
            mockMvc,
            HttpMethod.PUT,
            link1IdPath
        );
    }

    @Test
    void shouldReturnUnauthorizedForUser_WhenUpdatingLinkWithMalformedToken() throws Exception {
        String link1IdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, 1L);

        IntegrationTestFactory.performUnauthorizedJsonRequest(
            mockMvc,
            HttpMethod.PUT,
            link1IdPath,
            "malformed.token"
        );
    }

    @Test
    void shouldIgnoreUserIdInRequest_AndCreateLinkForAuthenticatedUser() throws Exception {
        // Arrange
        String jsonForUserA = IntegrationTestFactory.createJsonForUser(
            "validUsername1",
            "validPassword1@"
        );
        String jsonForUserB = IntegrationTestFactory.createJsonForUser(
            "validUsername2",
            "validPassword2@"
        );

        // Act & Assert
        IntegrationTestFactory.performJsonRegisterUserRequest(mockMvc, jsonForUserA);
        IntegrationTestFactory.performJsonRegisterUserRequest(mockMvc, jsonForUserB);

        MvcResult result = IntegrationTestFactory.performJsonUserLoginRequest(mockMvc, jsonForUserA);
        String userAToken = IntegrationTestFactory.getUserTokenFromJsonResponse(result, mapper);

        User userB = userRepository.findByUsername("validUsername2").orElseThrow();

        String maliciousJson = String.format("""
            {
                "url": "https://githack.com",
                "title": "Git Hack",
                "description": "Trying to assign to userB",
                "userId": %d
            }
            """, userB.getId());

        ResultActions linkCreateResult = IntegrationTestFactory.performJsonCreateLinkRequest(
            mockMvc, userAToken,
            maliciousJson
        );

        IntegrationTestFactory.assertLinkCreateValidationSuccess(
            linkCreateResult,
            "https://githack.com",
            "Git Hack"
        );
    }

    @Test
    void shouldReturnUnauthorizedForUser_WhenCreatingLinkWithoutAToken() throws Exception {
        String link1IdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, 1L);

        IntegrationTestFactory.performUnauthorizedJsonRequestWithoutHeader(
            mockMvc,
            HttpMethod.POST,
            link1IdPath
        );
    }

    @Test
    void shouldReturnUnauthorizedForUser_WhenCreatingLinkWithMalformedToken() throws Exception {
        String link1IdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, 1L);

        IntegrationTestFactory.performUnauthorizedJsonRequest(
            mockMvc,
            HttpMethod.POST,
            link1IdPath,
            "malformed.token"
        );
    }

    @Test
    void shouldReturnCreatedStatusCode_AndCreateLinkForAuthenticatedUser() throws Exception {
        // Arrange
        String jsonForUserA = IntegrationTestFactory.createJsonForUser(
            "validUsername1",
            "validPassword1@"
        );

        // Act & Assert
        IntegrationTestFactory.performJsonRegisterUserRequest(mockMvc, jsonForUserA);

        MvcResult result = IntegrationTestFactory.performJsonUserLoginRequest(mockMvc, jsonForUserA);
        String userAToken = IntegrationTestFactory.getUserTokenFromJsonResponse(result, mapper);

        String createJson = IntegrationTestFactory.createJsonForLink(
            "https://docs.oracle.com",
            "Java docs",
            "Java documentation"
        );

        ResultActions linkCreateResult = IntegrationTestFactory.performJsonCreateLinkRequest(
            mockMvc, userAToken,
            createJson
        );

        IntegrationTestFactory.assertLinkCreateValidationSuccess(
            linkCreateResult,
            "https://docs.oracle.com",
            "Java docs"
        );
    }

    @Test
    void shouldReturnSuccessStatusCode_AndUpdateLinkForAuthenticatedUser() throws Exception {
        // Arrange
        String jsonForUserA = IntegrationTestFactory.createJsonForUser(
            "validUsername1",
            "validPassword1@"
        );

        // Act & Assert
        IntegrationTestFactory.performJsonRegisterUserRequest(mockMvc, jsonForUserA);

        MvcResult result = IntegrationTestFactory.performJsonUserLoginRequest(mockMvc, jsonForUserA);
        String userAToken = IntegrationTestFactory.getUserTokenFromJsonResponse(result, mapper);

        String createJson = IntegrationTestFactory.createJsonForLink(
            "https://docs.oracle.com",
            "Java docs",
            "Java documentation"
        );

        ResultActions linkCreateResult = IntegrationTestFactory.performJsonCreateLinkRequest(
            mockMvc, userAToken,
            createJson
        );
        MvcResult linkResponse = IntegrationTestFactory.assertLinkCreateValidationSuccess(
            linkCreateResult,
            "https://docs.oracle.com",
            "Java docs"
        );

        JsonNode linkResponseJsonNode = IntegrationTestFactory.getLinkResponseJsonNodeFromBody(
            linkResponse,
            mapper
        );

        long linkId = linkResponseJsonNode.get("id").asLong();

        String updateLinkJson = IntegrationTestFactory.createJsonForLink(
            "https://updated.com",
            "Updated Title",
            "Updated Description"
        );

        String link1IdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkId);

        ResultActions linkUpdateResult = mockMvc.perform(put(link1IdPath)
                .header(TestConstants.AUTHORIZATION, TestConstants.BEARER + userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateLinkJson))
            .andExpect(status().isOk());

        IntegrationTestFactory.assertLinkCreateValidationSuccess(
            linkUpdateResult,
            "https://updated.com",
            "Updated Title"
        );
    }
}
