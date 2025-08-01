package com.linkvault.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkvault.constants.apiPaths.LinkEndpoints;
import com.linkvault.controller.LinkController;
import com.linkvault.dto.LinkDto;
import com.linkvault.exception.*;
import com.linkvault.model.Role;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
    private LinkDto linkDto1;
    private LinkDto linkDto2;

    @BeforeEach
    void setUp() {
        user = TestDataFactory.createTestUser();
        linkDto1 = TestDataFactory.createLinkDto1();
        linkDto2 = TestDataFactory.createLinkDto2();
        setMockMvc(mockMvc);
    }

    @Test
    void shouldReturnAllLinksForUserWhenUserHasLinks() throws Exception {
        // Arrange
        when(linkService.getAllLinksForUser(user.getId()))
            .thenReturn(List.of(linkDto1, linkDto2));
        when(userServiceImpl.getUserIdByUsername("eddie"))
            .thenReturn(user.getId());

        // Setup fake auth
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            "eddie",
            "password123",
            List.of(new SimpleGrantedAuthority(Role.USER.toString()))
        );
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/api/links")
            .with(user(userDetails)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value(linkDto1.title()))
            .andExpect(jsonPath("$[1].title").value(linkDto2.title()));

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnLinkWhenIdExists() throws Exception {
        // Arrange
        when(linkService.getLinkById(linkDto1.id())).thenReturn(Optional.of(linkDto1));

        // Assert
        mockMvc.perform(get(LinkEndpoints.BASE_LINKS + "/" + linkDto1.id()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value(linkDto1.title()));
    }

    @Test
    void shouldReturnNotFoundStatusWhenLinkDoesNotExist() throws Exception {
        // Arrange
        when(linkService.getLinkById(linkDto1.id()))
            .thenThrow(new LinkNotFoundException(linkDto1.id(),
                new RuntimeException()));

        // Assert
        mockMvc.perform(get(LinkEndpoints.BASE_LINKS + "/" + linkDto1.id()))
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.message")
                .value(String.format(ExceptionMessages.LINK_NOT_FOUND, linkDto1.id())
                ));
    }

    @Test
    void shouldReturnLinkWhenNewLinkIsCreated() throws Exception {
        // Arrange
        when(linkService.createLink(user.getId(), linkDto2))
            .thenReturn(Optional.of(linkDto2));
        String json = objectMapper.writeValueAsString(linkDto2);

        // Assert
        mockMvc.perform(post(LinkEndpoints.BASE_LINKS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value(linkDto2.title()));
    }

    @Test
    void shouldReturnServerErrorStatusWhenLinkSaveFails() throws Exception {
        // Arrange
        when(linkService.createLink(user.getId(), linkDto2)).thenThrow(
            new LinkSaveException(linkDto2,
                new RuntimeException(ExceptionMessages.DATABASE_FAILURE)));
        String json = objectMapper.writeValueAsString(linkDto2);

        // Assert
        mockMvc.perform(post(LinkEndpoints.BASE_LINKS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(jsonPath("$.status")
                .value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
            .andExpect(jsonPath("$.message")
                .value(String.format(ExceptionMessages.LINK_SAVE_FAILED, linkDto2.url())
                ));
    }

    @Test
    void shouldReturnLinkWhenLinkIsUpdated() throws Exception {
        // Arrange
        when(linkService.updateLink(linkDto1.id(),
            linkDto1)).thenReturn(Optional.of(linkDto1));
        String json = objectMapper.writeValueAsString(linkDto1);

        // Assert
        String linkDtoIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkDto1.id());

        mockMvc.perform(put(linkDtoIdPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value(linkDto1.title()));
    }

    @Test
    void shouldReturnServerErrorStatusWhenLinkSaveFailsOnUpdate() throws Exception {
        // Arrange
        when(linkService.updateLink(linkDto1.id(), linkDto1)).thenThrow(
            new LinkSaveException(linkDto1,
                new RuntimeException(ExceptionMessages.DATABASE_FAILURE)));
        String json = objectMapper.writeValueAsString(linkDto1);

        // Assert
        String linkDtoIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkDto1.id());

        mockMvc.perform(put(linkDtoIdPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(jsonPath("$.status")
                .value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
            .andExpect(jsonPath("$.message")
                .value(String.format(ExceptionMessages.LINK_SAVE_FAILED, linkDto1.url())
                ));
    }

    @Test
    void shouldReturnNoContentStatusWhenLinkIsDeleted() throws Exception {
        // Arrange
        doNothing().when(linkService).deleteLink(linkDto1.id(), user.getId());
        when(userServiceImpl.getUserIdByUsername("eddie"))
            .thenReturn(user.getId());

        // Setup fake auth
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            "eddie",
            "password123",
            List.of(new SimpleGrantedAuthority(Role.USER.toString()))
        );
        UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Assert
        String linkDtoIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkDto1.id());

        mockMvc.perform(delete(linkDtoIdPath)
                .with(user(userDetails)))
            .andExpect(status().isNoContent());

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnServerErrorStatusWhenLinkFailsToDelete() throws Exception {
        // Arrange
        when(userServiceImpl.getUserIdByUsername("eddie"))
            .thenReturn(user.getId());
        doThrow(
            new LinkDeleteException(linkDto1,
                new RuntimeException(ExceptionMessages.DATABASE_FAILURE))
        ).when(linkService).deleteLink(linkDto1.id(), user.getId());

        // Setup fake auth
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            "eddie",
            "password123",
            List.of(new SimpleGrantedAuthority(Role.USER.toString()))
        );
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Assert
        String linkDtoIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkDto1.id());

        mockMvc.perform(delete(linkDtoIdPath)
                .with(user(userDetails)))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value(
                String.format(ExceptionMessages.LINK_DELETE_FAILED, linkDto1.url())
            ));

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnNoContentStatusWhenAllLinksAreDeleted() throws Exception {
        // Arrange
        when(userServiceImpl.getUserIdByUsername("eddie"))
            .thenReturn(user.getId());
        doNothing().when(linkService).deleteAllLinksByUser(user.getId());

        // Setup fake auth
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            "eddie",
            "password123",
            List.of(new SimpleGrantedAuthority(Role.USER.toString()))
        );
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Assert
        mockMvc.perform(delete(LinkEndpoints.BASE_LINKS)).andExpect(status().isNoContent());

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnServerErrorStatusWhenAllLinksFailToDelete() throws Exception {
        // Arrange
        when(userServiceImpl.getUserIdByUsername("eddie"))
            .thenReturn(user.getId());
        doThrow(new LinksDeleteException(user.getId(),
            new RuntimeException(ExceptionMessages.DATABASE_FAILURE)))
            .when(linkService).deleteAllLinksByUser(user.getId());

        // Setup fake auth
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            "eddie",
            "password123",
            List.of(new SimpleGrantedAuthority(Role.USER.toString()))
        );
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Assert
        mockMvc.perform(delete(LinkEndpoints.BASE_LINKS))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value(
                String.format(ExceptionMessages.LINKS_DELETE_FAILED, user.getId())
            ));

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @ValueSource(strings = {TestConstants.HTTP_POST, TestConstants.HTTP_PUT})
    void shouldReturnBadRequestWhenUserIdIsNull(String method) throws Exception {
        String jsonWithNullUserId = new JsonBuilder()
            .withUserId(null)
            .withUrl(TestConstants.VALID_URL)
            .withTitle(TestConstants.VALID_TITLE)
            .withDescription(TestConstants.VALID_DESCRIPTION)
            .build();

        String linkDtoIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkDto1.id());

        MockHttpServletRequestBuilder requestBuilder =
            buildSimpleRequest(method, linkDtoIdPath);

        ResultActions result = performJsonRequest(requestBuilder, jsonWithNullUserId);
        assertValidationFailure(result, TestConstants.USER_ID);
    }

    @ParameterizedTest
    @ValueSource(strings = {TestConstants.HTTP_POST, TestConstants.HTTP_PUT})
    void shouldReturnBadRequestWhenUrlIsEmpty(String method) throws Exception {
        String jsonWithEmptyUrl = new JsonBuilder()
            .withUserId(TestConstants.VALID_USER_ID)
            .withUrl("")
            .withTitle(TestConstants.VALID_TITLE)
            .withDescription(TestConstants.VALID_DESCRIPTION)
            .build();

        String linkDtoIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkDto1.id());

        MockHttpServletRequestBuilder requestBuilder =
            buildSimpleRequest(method, linkDtoIdPath);

        ResultActions result = performJsonRequest(requestBuilder, jsonWithEmptyUrl);
        assertValidationFailure(result, TestConstants.URL);
    }

    @ParameterizedTest
    @ValueSource(strings = {TestConstants.HTTP_POST, TestConstants.HTTP_PUT})
    void shouldReturnBadRequestWhenUrlIsTooLong(String method) throws Exception {
        String tooLongUrl = "a".repeat(265);
        String jsonWithTooLongUrl = String.format(new JsonBuilder()
            .withUserId(TestConstants.VALID_USER_ID)
            .withUrl("https://%s.com")
            .withTitle(TestConstants.VALID_TITLE)
            .withDescription(TestConstants.VALID_DESCRIPTION)
            .build(), tooLongUrl);

        String linkDtoIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkDto1.id());

        MockHttpServletRequestBuilder requestBuilder =
            buildSimpleRequest(method, linkDtoIdPath);

        ResultActions result = performJsonRequest(requestBuilder, jsonWithTooLongUrl);
        assertValidationFailure(result, TestConstants.URL);
    }

    @ParameterizedTest
    @ValueSource(strings = {TestConstants.HTTP_POST, TestConstants.HTTP_PUT})
    void shouldReturnBadRequestWhenTitleIsTooLong(String method) throws Exception {
        String tooLongTitle = "a".repeat(150);
        String jsonWithTooLongTitle = String.format(new JsonBuilder()
            .withUserId(TestConstants.VALID_USER_ID)
            .withUrl(TestConstants.VALID_URL)
            .withTitle("%s")
            .withDescription(TestConstants.VALID_DESCRIPTION)
            .build(), tooLongTitle);

        String linkDtoIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkDto1.id());

        MockHttpServletRequestBuilder requestBuilder =
            buildSimpleRequest(method, linkDtoIdPath);

        ResultActions result = performJsonRequest(requestBuilder, jsonWithTooLongTitle);
        assertValidationFailure(result, TestConstants.TITLE);
    }

    @ParameterizedTest
    @ValueSource(strings = {TestConstants.HTTP_POST, TestConstants.HTTP_PUT})
    void shouldReturnBadRequestWhenUrlIsInvalidFormat(String method) throws Exception {
        String jsonWithTooLongUrl = new JsonBuilder()
            .withUserId(TestConstants.VALID_USER_ID)
            .withUrl("not-a-url-at-all")
            .withTitle(TestConstants.VALID_TITLE)
            .withDescription(TestConstants.VALID_DESCRIPTION)
            .build();

        String linkDtoIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkDto1.id());

        MockHttpServletRequestBuilder requestBuilder =
            buildSimpleRequest(method, linkDtoIdPath);

        ResultActions result = performJsonRequest(requestBuilder, jsonWithTooLongUrl);
        assertValidationFailure(result, TestConstants.URL);
    }

    @ParameterizedTest
    @ValueSource(strings = {TestConstants.HTTP_POST, TestConstants.HTTP_PUT})
    void shouldReturnBadRequestWhenDescriptionIsTooLong(String method) throws Exception {
        String tooLongDescription = "a".repeat(265);
        String jsonWithTooLongDescription = String.format(new JsonBuilder()
            .withUserId(TestConstants.VALID_USER_ID)
            .withUrl(TestConstants.VALID_URL)
            .withTitle(TestConstants.VALID_TITLE)
            .withDescription("%s")
            .build(), tooLongDescription);

        String linkDtoIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkDto1.id());

        MockHttpServletRequestBuilder requestBuilder =
            buildSimpleRequest(method, linkDtoIdPath);

        ResultActions result = performJsonRequest(requestBuilder, jsonWithTooLongDescription);
        assertValidationFailure(result, TestConstants.DESCRIPTION);
    }

    @ParameterizedTest
    @ValueSource(strings = {TestConstants.HTTP_POST, TestConstants.HTTP_PUT})
    void shouldReturnBadRequestWhenFieldsAreMissing(String method) throws Exception {
        String invalidJson = "{}";

        String linkDtoIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkDto1.id());

        MockHttpServletRequestBuilder requestBuilder =
            buildSimpleRequest(method, linkDtoIdPath);

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
    @ValueSource(strings = {TestConstants.HTTP_POST, TestConstants.HTTP_PUT})
    void shouldReturnBadRequestWhenUserIdIsMissing(String method) throws Exception {
        String jsonWithOutUserField = new JsonBuilder()
            .withoutField(TestConstants.USER_ID)
            .withUrl(TestConstants.VALID_URL)
            .withTitle(TestConstants.VALID_TITLE)
            .withDescription(TestConstants.VALID_DESCRIPTION)
            .build();

        String linkDtoIdPath = TestDataFactory
            .buildLinkEndpointWithId(TestConstants.LINK_ID_PATH_VAR, linkDto1.id());

        MockHttpServletRequestBuilder requestBuilder =
            buildSimpleRequest(method, linkDtoIdPath);

        ResultActions result = performJsonRequest(requestBuilder, jsonWithOutUserField);
        assertValidationFailure(result, TestConstants.USER_ID);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            TestConstants.HTTP_GET, TestConstants.HTTP_DELETE, TestConstants.HTTP_PUT
        }
    )
    void shouldReturnBadRequestWhenLinkIdIsZero(String method) throws Exception {
        String jsonValid = new JsonBuilder()
            .withUserId(TestConstants.VALID_USER_ID)
            .withUrl(TestConstants.VALID_URL)
            .withTitle(TestConstants.VALID_TITLE)
            .withDescription(TestConstants.VALID_DESCRIPTION)
            .build();

        String linkDtoIdPath = TestDataFactory
            .buildLinkEndpointWithId(
                TestConstants.LINK_ID_PATH_VAR, TestConstants.INVALID_LINK_DTO_ID
            );

        MockHttpServletRequestBuilder requestBuilder = switch(method) {
            case TestConstants.HTTP_GET -> get(linkDtoIdPath);
            case TestConstants.HTTP_DELETE -> delete(linkDtoIdPath);
            case TestConstants.HTTP_PUT -> put(linkDtoIdPath).content(jsonValid);
            default -> throw new IllegalArgumentException(TestConstants.INVALID_METHOD);
        };

        ResultActions result = performJsonRequest(requestBuilder, jsonValid);
        assertValidationFailure(result, TestConstants.LINK_ID);
    }

    @ParameterizedTest
    @ValueSource(strings = {TestConstants.HTTP_GET, TestConstants.HTTP_DELETE})
    void shouldReturnBadRequestWhenUserIdIsNegative(String method) throws Exception {
        String linkDtoIdPath = TestDataFactory
            .buildUserEndpointWithIncorrectId(
                TestConstants.USER_ID_PATH_VAR, TestConstants.INVALID_USER_ID
            );

        MockHttpServletRequestBuilder requestBuilder = switch(method) {
            case TestConstants.HTTP_GET -> get(linkDtoIdPath);
            case TestConstants.HTTP_DELETE -> delete(linkDtoIdPath);
            default -> throw new IllegalArgumentException(TestConstants.INVALID_METHOD);
        };

        ResultActions result = mockMvc.perform(requestBuilder
            .contentType(MediaType.APPLICATION_JSON));
        assertValidationFailure(result, TestConstants.USER_ID);
    }
}
