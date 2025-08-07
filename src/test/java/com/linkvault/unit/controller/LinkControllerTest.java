package com.linkvault.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkvault.constants.apiPaths.LinkEndpoints;
import com.linkvault.controller.LinkController;
import com.linkvault.dto.LinkRequest;
import com.linkvault.dto.LinkResponse;
import com.linkvault.exception.*;
import com.linkvault.model.User;
import com.linkvault.service.LinkService;
import com.linkvault.service.UserServiceImpl;
import com.linkvault.unit.util.AbstractValidationTest;
import com.linkvault.unit.util.JsonBuilder;
import com.linkvault.unit.util.TestConstants;
import com.linkvault.unit.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LinkController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LinkControllerTest extends AbstractValidationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LinkService linkService;

    @MockitoBean
    private UserServiceImpl userServiceImpl;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private LinkResponse linkResponse;
    private LinkResponse linkResponseTwo;
    private LinkRequest linkRequest;


    @BeforeEach
    void setUp() {
        user = TestDataFactory.createTestUser();
        linkResponse = TestDataFactory.createLinkResponse();
        linkResponseTwo = TestDataFactory.createLinkResponseTwo();
        linkRequest = TestDataFactory.createLinkRequest();
        setMockMvc(mockMvc);
    }

    @Test
    @WithMockUser(username = "eddie")
    void shouldReturnAllLinksForUserWhenUserHasLinks() throws Exception {
        // Arrange
        when(userServiceImpl.getUserIdByUsername("eddie"))
            .thenReturn(user.getId());
        when(linkService.getAllLinksForUser(user.getId()))
            .thenReturn(List.of(linkResponse, linkResponseTwo));

        mockMvc.perform(get("/api/links"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].title").value(linkResponse.title()))
            .andExpect(jsonPath("$[1].title").value(linkResponseTwo.title()))
            .andExpect(jsonPath("$[0].url").value(linkResponse.url()))
            .andExpect(jsonPath("$[1].url").value(linkResponseTwo.url()));

        verify(linkService).getAllLinksForUser(user.getId());
    }

    @Test
    @WithMockUser(username = "eddie")
    void shouldReturnEmptyListWhenUserHasNoLinks() throws Exception {
        // Arrange
        when(userServiceImpl.getUserIdByUsername("eddie"))
            .thenReturn(user.getId());
        when(linkService.getAllLinksForUser(user.getId()))
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/links"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(0)))
            .andExpect(content().json("[]"));

        verify(linkService).getAllLinksForUser(user.getId());
    }

    @Test
    @WithMockUser(username = "eddie")
    void shouldReturnLinkWhenIdExists() throws Exception {
        // Arrange
        when(userServiceImpl.getUserIdByUsername("eddie")).thenReturn(user.getId());
        when(linkService.getLinkById(linkResponse.id(), user.getId())).thenReturn(linkResponse);

        // Assert
        mockMvc.perform(get(LinkEndpoints.BASE_LINKS + "/" + linkResponse.id()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value(linkResponse.title()))
            .andExpect(jsonPath("$.url").value(linkResponse.url()));

        verify(linkService).getLinkById(linkResponse.id(), user.getId());
    }

    // TODO: Start here
    @Test
    @WithMockUser(username = "eddie")
    void shouldReturnNotFoundStatusWhenLinkDoesNotExist() throws Exception {
        // Arrange
        when(userServiceImpl.getUserIdByUsername("eddie")).thenReturn(user.getId());
        when(linkService.getLinkById(linkResponse.id(), user.getId()))
            .thenThrow(new LinkNotFoundException(linkResponse.id(),
                new RuntimeException()));

        // Assert
        mockMvc.perform(get(LinkEndpoints.BASE_LINKS + "/" + linkResponse.id()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.message")
                .value(String.format(ExceptionMessages.LINK_NOT_FOUND, linkResponse.id())
                ));

        verify(linkService).getLinkById(linkResponse.id(), user.getId());
    }

    @Test
    @WithMockUser(username = "eddie")
    void shouldReturnLinkWhenNewLinkIsCreated() throws Exception {
        // Arrange
        when(userServiceImpl.getUserIdByUsername("eddie")).thenReturn(user.getId());
        when(linkService.createLink(user.getId(), linkRequest))
            .thenReturn(linkResponse);

        // Act & Assert
        String json = objectMapper.writeValueAsString(linkRequest);

        mockMvc.perform(post(LinkEndpoints.BASE_LINKS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value(linkResponse.title()));

        verify(linkService).createLink(user.getId(), linkRequest);
    }

    @Test
    @WithMockUser(username = "eddie")
    void shouldReturnServerErrorStatusWhenLinkSaveFails() throws Exception {
        // Arrange
        when(userServiceImpl.getUserIdByUsername("eddie")).thenReturn(user.getId());
        when(linkService.createLink(user.getId(), linkRequest)).thenThrow(
            new LinkSaveException(linkResponse.id(), user.getId(),
                new RuntimeException(ExceptionMessages.DATABASE_FAILURE)));

        // Act & Assert
        String json = objectMapper.writeValueAsString(linkRequest);

        mockMvc.perform(post(LinkEndpoints.BASE_LINKS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status")
                .value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
            .andExpect(jsonPath("$.message")
                .value(String.format(ExceptionMessages.LINK_SAVE_FAILED, linkResponse.id(), user.getId())
                ));

        verify(linkService).createLink(user.getId(), linkRequest);
    }

    @Test
    @WithMockUser(username = "eddie")
    void shouldReturnLinkWhenLinkIsUpdated() throws Exception {
        // Arrange
        when(userServiceImpl.getUserIdByUsername("eddie")).thenReturn(user.getId());
        when(linkService.updateLink(
            linkResponse.id(),
            linkRequest,
            user.getId()
        )).thenReturn(linkResponse);

        // Act & Assert
        String json = objectMapper.writeValueAsString(linkRequest);
        String linkIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkResponse.id());

        mockMvc.perform(put(linkIdPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(linkResponse.id()))
            .andExpect(jsonPath("$.title").value(linkResponse.title()))
            .andExpect(jsonPath("$.url").value(linkResponse.url()));

        verify(linkService).updateLink(linkResponse.id(), linkRequest, user.getId());
    }

    @Test
    @WithMockUser(username = "eddie")
    void shouldReturnServerErrorStatusWhenLinkSaveFailsOnUpdate() throws Exception {
        // Arrange
        when(userServiceImpl.getUserIdByUsername("eddie")).thenReturn(user.getId());
        when(linkService.updateLink(linkResponse.id(), linkRequest, user.getId())).thenThrow(
            new LinkSaveException(linkResponse.id(), user.getId(),
                new RuntimeException(ExceptionMessages.DATABASE_FAILURE)));

        String json = objectMapper.writeValueAsString(linkRequest);
        String linkIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkResponse.id());

        // Act & Assert
        mockMvc.perform(put(linkIdPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status")
                .value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
            .andExpect(jsonPath("$.message")
                .value(String.format(
                        ExceptionMessages.LINK_SAVE_FAILED, linkResponse.id(), user.getId()
                    )
                ));

        verify(linkService).updateLink(linkResponse.id(), linkRequest, user.getId());
    }

    @Test
    @WithMockUser(username = "eddie")
    void shouldReturnBadRequestWhenLinkRequestInvalid() throws Exception {
        LinkRequest invalidRequest = new LinkRequest("", "", "");
        String json = objectMapper.writeValueAsString(invalidRequest);

        String linkIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkResponse.id());

        mockMvc.perform(put(linkIdPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "eddie")
    void shouldReturnNoContentStatusWhenLinkIsDeleted() throws Exception {
        // Arrange
        when(userServiceImpl.getUserIdByUsername("eddie")).thenReturn(user.getId());
        doNothing().when(linkService).deleteLink(linkResponse.id(), user.getId());

        // Act & Assert
        String linkIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkResponse.id());

        mockMvc.perform(delete(linkIdPath))
            .andExpect(status().isNoContent());

        verify(linkService).deleteLink(linkResponse.id(), user.getId());
    }

    @Test
    @WithMockUser(username = "eddie")
    void shouldReturnServerErrorStatusWhenLinkFailsToDelete() throws Exception {
        // Arrange
        when(userServiceImpl.getUserIdByUsername("eddie"))
            .thenReturn(user.getId());
        doThrow(
            new LinkDeleteException(
                linkResponse.id(),
                user.getId(),
                new RuntimeException(ExceptionMessages.DATABASE_FAILURE)
            )
        ).when(linkService).deleteLink(linkResponse.id(), user.getId());

        // Assert
        String linkIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkResponse.id());

        mockMvc.perform(delete(linkIdPath))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
            .andExpect(jsonPath("$.message").value(
                String.format(ExceptionMessages.LINK_DELETE_FAILED, linkResponse.id(), user.getId())
            ));
    }

    @Test
    @WithMockUser(username = "eddie")
    void shouldReturnNoContentStatusWhenAllLinksAreDeleted() throws Exception {
        // Arrange
        when(userServiceImpl.getUserIdByUsername("eddie"))
            .thenReturn(user.getId());
        doNothing().when(linkService).deleteAllLinksByUser(user.getId());

        // Act & Assert
        mockMvc.perform(delete(LinkEndpoints.BASE_LINKS)).andExpect(status().isNoContent());

        verify(linkService).deleteAllLinksByUser(user.getId());
    }

    @Test
    @WithMockUser(username = "eddie")
    void shouldReturnServerErrorStatusWhenAllLinksFailToDelete() throws Exception {
        // Arrange
        when(userServiceImpl.getUserIdByUsername("eddie"))
            .thenReturn(user.getId());
        doThrow(new LinksDeleteException(user.getId(),
            new RuntimeException(ExceptionMessages.DATABASE_FAILURE)))
            .when(linkService).deleteAllLinksByUser(user.getId());

        // Act & Assert
        mockMvc.perform(delete(LinkEndpoints.BASE_LINKS))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value(
                String.format(ExceptionMessages.LINKS_DELETE_FAILED, user.getId())
            ));

        verify(linkService).deleteAllLinksByUser(user.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {TestConstants.HTTP_POST, TestConstants.HTTP_PUT})
    void shouldReturnBadRequestWhenUrlIsEmpty(String method) throws Exception {
        String jsonWithEmptyUrl = new JsonBuilder()
            .withUrl("")
            .withTitle(TestConstants.VALID_TITLE)
            .withDescription(TestConstants.VALID_DESCRIPTION)
            .build();

        String linkIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkResponse.id());

        MockHttpServletRequestBuilder requestBuilder =
            buildSimpleRequest(method, linkIdPath);

        ResultActions result = performJsonRequest(requestBuilder, jsonWithEmptyUrl);
        assertValidationFailure(result, TestConstants.URL);
    }

    @ParameterizedTest
    @ValueSource(strings = {TestConstants.HTTP_POST, TestConstants.HTTP_PUT})
    void shouldReturnBadRequestWhenUrlIsTooLong(String method) throws Exception {
        String tooLongUrl = "https://" + "a".repeat(265) +".com";
        String jsonWithTooLongUrl = new JsonBuilder()
            .withUrl(tooLongUrl)
            .withTitle(TestConstants.VALID_TITLE)
            .withDescription(TestConstants.VALID_DESCRIPTION)
            .build();

        String linkIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkResponse.id());

        MockHttpServletRequestBuilder requestBuilder =
            buildSimpleRequest(method, linkIdPath);

        ResultActions result = performJsonRequest(requestBuilder, jsonWithTooLongUrl);
        assertValidationFailure(result, TestConstants.URL);
    }

    @ParameterizedTest
    @ValueSource(strings = {TestConstants.HTTP_POST, TestConstants.HTTP_PUT})
    void shouldReturnBadRequestWhenTitleIsTooLong(String method) throws Exception {
        String tooLongTitle = "a".repeat(150);
        String jsonWithTooLongTitle = new JsonBuilder()
            .withUrl(TestConstants.VALID_URL)
            .withTitle(tooLongTitle)
            .withDescription(TestConstants.VALID_DESCRIPTION)
            .build();

        String linkIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkResponse.id());

        MockHttpServletRequestBuilder requestBuilder =
            buildSimpleRequest(method, linkIdPath);

        ResultActions result = performJsonRequest(requestBuilder, jsonWithTooLongTitle);
        assertValidationFailure(result, TestConstants.TITLE);
    }

    @ParameterizedTest
    @ValueSource(strings = {TestConstants.HTTP_POST, TestConstants.HTTP_PUT})
    void shouldReturnBadRequestWhenUrlIsInvalidFormat(String method) throws Exception {
        String jsonWithTooLongUrl = new JsonBuilder()
            .withUrl("not-a-url-at-all")
            .withTitle(TestConstants.VALID_TITLE)
            .withDescription(TestConstants.VALID_DESCRIPTION)
            .build();

        String linkIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkResponse.id());

        MockHttpServletRequestBuilder requestBuilder =
            buildSimpleRequest(method, linkIdPath);

        ResultActions result = performJsonRequest(requestBuilder, jsonWithTooLongUrl);
        assertValidationFailure(result, TestConstants.URL);
    }

    @ParameterizedTest
    @ValueSource(strings = {TestConstants.HTTP_POST, TestConstants.HTTP_PUT})
    void shouldReturnBadRequestWhenDescriptionIsTooLong(String method) throws Exception {
        String tooLongDescription = "a".repeat(265);
        String jsonWithTooLongDescription = new JsonBuilder()
            .withUrl(TestConstants.VALID_URL)
            .withTitle(TestConstants.VALID_TITLE)
            .withDescription(tooLongDescription)
            .build();

        String linkIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkResponse.id());

        MockHttpServletRequestBuilder requestBuilder =
            buildSimpleRequest(method, linkIdPath);

        ResultActions result = performJsonRequest(requestBuilder, jsonWithTooLongDescription);
        assertValidationFailure(result, TestConstants.DESCRIPTION);
    }

    @ParameterizedTest
    @ValueSource(strings = {TestConstants.HTTP_POST, TestConstants.HTTP_PUT})
    void shouldReturnBadRequestWhenFieldsAreMissing(String method) throws Exception {
        String invalidJson = "{}";

        String linkIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkResponse.id());

        MockHttpServletRequestBuilder requestBuilder =
            buildSimpleRequest(method, linkIdPath);

        ResultActions result = performJsonRequest(requestBuilder, invalidJson);

        result
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists())
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.message").value(
                ExceptionMessages.INVALID_FIELDS))
            .andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            TestConstants.HTTP_GET, TestConstants.HTTP_DELETE, TestConstants.HTTP_PUT
        }
    )
    void shouldReturnBadRequestWhenLinkIdIsZero(String method) throws Exception {
        String jsonValid = new JsonBuilder()
            .withUrl(TestConstants.VALID_URL)
            .withTitle(TestConstants.VALID_TITLE)
            .withDescription(TestConstants.VALID_DESCRIPTION)
            .build();

        String linkIdPath = TestDataFactory
            .buildLinkEndpointWithId(
                TestConstants.LINK_ID_PATH_VAR, TestConstants.INVALID_LINK_ID
            );

        MockHttpServletRequestBuilder requestBuilder = switch(method) {
            case TestConstants.HTTP_GET -> get(linkIdPath);
            case TestConstants.HTTP_DELETE -> delete(linkIdPath);
            case TestConstants.HTTP_PUT -> put(linkIdPath).content(jsonValid);
            default -> throw new IllegalArgumentException(TestConstants.INVALID_METHOD);
        };

        ResultActions result = performJsonRequest(requestBuilder, jsonValid);
        assertValidationFailure(result, TestConstants.LINK_ID);
    }
}
