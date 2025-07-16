package com.linkvault.LinkControllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkvault.constants.apiPaths.LinkEndpoints;
import com.linkvault.controller.LinkController;
import com.linkvault.dto.LinkDto;
import com.linkvault.exception.*;
import com.linkvault.model.User;
import com.linkvault.service.LinkService;
import com.linkvault.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LinkController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LinkControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private LinkService linkService;
    private User user;
    @Autowired
    private ObjectMapper objectMapper;
    private LinkDto linkDto1;
    private LinkDto linkDto2;

    @BeforeEach
    void setUp() {
        user = TestDataFactory.createTestUser();
        linkDto1 = TestDataFactory.createLinkDto1();
        linkDto2 = TestDataFactory.createLinkDto2();
    }

    @Test
    void shouldReturnAllLinksForUserWhenUserHasLinks() throws Exception {
        // Arrange
        when(linkService.getAllLinksForUser(user.getId()))
            .thenReturn(List.of(linkDto1, linkDto2));

        // Assert
        String url = LinkEndpoints.BASE_LINKS + LinkEndpoints.BY_USER
            .replace("{userId}", user.getId().toString());
        mockMvc.perform(get(url))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value(linkDto1.title()))
            .andExpect(jsonPath("$[1].title").value(linkDto2.title()));
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
        String linkDtoIdPath = LinkEndpoints.BASE_LINKS + LinkEndpoints.BY_LINK_ID
                .replace("{linkId}", linkDto1.id().toString());
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
        String linkDtoIdPath = LinkEndpoints.BASE_LINKS + LinkEndpoints.BY_LINK_ID
            .replace("{linkId}", linkDto1.id().toString());
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
        doNothing().when(linkService).deleteLink(linkDto1.id());

        // Assert
        String linkDtoIdPath = LinkEndpoints.BASE_LINKS + LinkEndpoints.BY_LINK_ID
            .replace("{linkId}", linkDto1.id().toString());
        mockMvc.perform(delete(linkDtoIdPath)).andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnServerErrorStatusWhenLinkFailsToDelete() throws Exception {
        // Arrange
        doThrow(
            new LinkDeleteException(linkDto1,
                new RuntimeException(ExceptionMessages.DATABASE_FAILURE))
        ).when(linkService).deleteLink(linkDto1.id());

        // Assert
        String linkDtoIdPath = LinkEndpoints.BASE_LINKS + LinkEndpoints.BY_LINK_ID
            .replace("{linkId}", linkDto1.id().toString());
        mockMvc.perform(delete(linkDtoIdPath))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value(
                String.format(ExceptionMessages.LINK_DELETE_FAILED, linkDto1.url())
            ));
    }

    @Test
    void shouldReturnNoContentStatusWhenAllLinksAreDeleted() throws Exception {
        // Arrange
        doNothing().when(linkService).deleteAllLinksByUser(user.getId());

        // Assert
        String userIdPath = LinkEndpoints.BASE_LINKS + LinkEndpoints.BY_USER
            .replace("{userId}", user.getId().toString());
        mockMvc.perform(delete(userIdPath)).andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnServerErrorStatusWhenAllLinksFailToDelete() throws Exception {
        // Arrange
        doThrow(new LinksDeleteException(user.getId(),
            new RuntimeException(ExceptionMessages.DATABASE_FAILURE)))
            .when(linkService).deleteAllLinksByUser(user.getId());

        // Assert
        String userIdPath = LinkEndpoints.BASE_LINKS + LinkEndpoints.BY_USER
            .replace("{userId}", user.getId().toString());
        mockMvc.perform(delete(userIdPath))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value(
                String.format(ExceptionMessages.LINKS_DELETE_FAILED, user.getId())
            ));
    }

    // Input validation tests TODO: Abstract class
    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void shouldReturnBadRequestWhenUserIdIsNull(String method) throws Exception {
        String invalidJson = """
            {
                "userId": null,
                "url": "https://valid.com",
                "title": "Valid title",
                "description": "Valid description."
            }
        """;

        String linkDtoIdPath = LinkEndpoints.BASE_LINKS + LinkEndpoints.BY_LINK_ID
            .replace("{linkId}", linkDto1.id().toString());

        MockHttpServletRequestBuilder requestBuilder = switch(method) {
            case "POST" -> post(LinkEndpoints.BASE_LINKS);
            case "PUT" -> put(linkDtoIdPath);
            default -> throw new IllegalArgumentException("Invalid method");
        };

        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists())
            .andExpect(jsonPath("$.errors[*]", hasItem(containsString("userId"))))
            .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
            .andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void shouldReturnBadRequestWhenUrlIsEmpty(String method) throws Exception {
        String invalidJson = """
            {
                "userId": 1,
                "url": "",
                "title": "Valid title",
                "description": "Valid description"
            }
            """;

        String linkDtoIdPath = LinkEndpoints.BASE_LINKS + LinkEndpoints.BY_LINK_ID
            .replace("{linkId}", linkDto1.id().toString());

        MockHttpServletRequestBuilder requestBuilder = switch(method) {
            case "POST" -> post(LinkEndpoints.BASE_LINKS);
            case "PUT" -> put(linkDtoIdPath);
            default -> throw new IllegalArgumentException("Invalid method");
        };

        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists())
            .andExpect(jsonPath("$.errors[*]", hasItem(containsString("url"))))
            .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
            .andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void shouldReturnBadRequestWhenUrlIsTooLong(String method) throws Exception {
        String tooLongUrl = "a".repeat(265);
        String invalidJson = String.format("""
            {
                "userId": 1,
                "url": "https://%s.com",
                "title": "Valid title",
                "description": "Valid description"
            }
            """, tooLongUrl);

        String linkDtoIdPath = LinkEndpoints.BASE_LINKS + LinkEndpoints.BY_LINK_ID
            .replace("{linkId}", linkDto1.id().toString());

        MockHttpServletRequestBuilder requestBuilder = switch(method) {
            case "POST" -> post(LinkEndpoints.BASE_LINKS);
            case "PUT" -> put(linkDtoIdPath);
            default -> throw new IllegalArgumentException("Invalid method");
        };

        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists())
            .andExpect(jsonPath("$.errors[*]", hasItem(containsString("url"))))
            .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
            .andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void shouldReturnBadRequestWhenTitleIsTooLong(String method) throws Exception {
        String tooLongTitle = "a".repeat(150);
        String invalidJson = String.format("""
            {
                "userId": 1,
                "url": "https://valid.com",
                "title": "%s",
                "description": "Valid description"
            }
            """, tooLongTitle);

        String linkDtoIdPath = LinkEndpoints.BASE_LINKS + LinkEndpoints.BY_LINK_ID
            .replace("{linkId}", linkDto1.id().toString());

        MockHttpServletRequestBuilder requestBuilder = switch(method) {
            case "POST" -> post(LinkEndpoints.BASE_LINKS);
            case "PUT" -> put(linkDtoIdPath);
            default -> throw new IllegalArgumentException("Invalid method");
        };

        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists())
            .andExpect(jsonPath("$.errors[*]", hasItem(containsString("title"))))
            .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
            .andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void shouldReturnBadRequestWhenUrlIsInvalidFormat(String method) throws Exception {
        String invalidJson = """
            {
                "userId": 1,
                "url": "not-a-url-at-all",
                "title": "Valid title",
                "description": "Valid description"
            }
            """;

        String linkDtoIdPath = LinkEndpoints.BASE_LINKS + LinkEndpoints.BY_LINK_ID
            .replace("{linkId}", linkDto1.id().toString());

        MockHttpServletRequestBuilder requestBuilder = switch(method) {
            case "POST" -> post(LinkEndpoints.BASE_LINKS);
            case "PUT" -> put(linkDtoIdPath);
            default -> throw new IllegalArgumentException("Invalid method");
        };

        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists())
            .andExpect(jsonPath("$.errors[*]", hasItem(containsString("url"))))
            .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
            .andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void shouldReturnBadRequestWhenDescriptionIsTooLong(String method) throws Exception {
        String tooLongDescription = "a".repeat(265);
        String invalidJson = String.format("""
            {
                "userId": 1,
                "url": "https://valid.com",
                "title": "Valid title",
                "description": "%s"
            }
            """, tooLongDescription);

        String linkDtoIdPath = LinkEndpoints.BASE_LINKS + LinkEndpoints.BY_LINK_ID
            .replace("{linkId}", linkDto1.id().toString());

        MockHttpServletRequestBuilder requestBuilder = switch(method) {
            case "POST" -> post(LinkEndpoints.BASE_LINKS);
            case "PUT" -> put(linkDtoIdPath);
            default -> throw new IllegalArgumentException("Invalid method");
        };

        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists())
            .andExpect(jsonPath("$.errors[*]", hasItem(containsString("description"))))
            .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
            .andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void shouldReturnBadRequestWhenFieldsAreMissing(String method) throws Exception {
        String invalidJson = "{}";

        String linkDtoIdPath = LinkEndpoints.BASE_LINKS + LinkEndpoints.BY_LINK_ID
            .replace("{linkId}", linkDto1.id().toString());

        MockHttpServletRequestBuilder requestBuilder = switch(method) {
            case "POST" -> post(LinkEndpoints.BASE_LINKS);
            case "PUT" -> put(linkDtoIdPath);
            default -> throw new IllegalArgumentException("Invalid method");
        };

        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists())
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
            .andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void shouldReturnBadRequestWhenUserIdIsMissing(String method) throws Exception {
        String invalidJson = """
            {
                "url": "https://valid.com",
                "title": "Valid title",
                "description": "Valid description"
            }
            """;

        String linkDtoIdPath = LinkEndpoints.BASE_LINKS + LinkEndpoints.BY_LINK_ID
            .replace("{linkId}", linkDto1.id().toString());

        MockHttpServletRequestBuilder requestBuilder = switch(method) {
            case "POST" -> post(LinkEndpoints.BASE_LINKS);
            case "PUT" -> put(linkDtoIdPath);
            default -> throw new IllegalArgumentException("Invalid method");
        };

        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists())
            .andExpect(jsonPath("$.errors[*]", hasItem(containsString("userId"))))
            .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
            .andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "DELETE", "PUT"})
    void shouldReturnBadRequestWhenLinkIdIsZero(String method) throws Exception {
        String validJson = """
            {
                "userId": 1,
                "url": "https://valid.com",
                "title": "Valid title",
                "description": "Valid description."
            }
        """;

        String linkDtoIdPath = LinkEndpoints.BASE_LINKS + LinkEndpoints.BY_LINK_ID
            .replace("{linkId}", "0");

        MockHttpServletRequestBuilder requestBuilder = switch(method) {
            case "GET" -> get(linkDtoIdPath);
            case "DELETE" -> delete(linkDtoIdPath);
            case "PUT" -> put(linkDtoIdPath).content(validJson);
            default -> throw new IllegalArgumentException("Invalid method");
        };

        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists())
            .andExpect(jsonPath("$.errors[*]", hasItem(containsString("linkId"))))
            .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
            .andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "DELETE"})
    void shouldReturnBadRequestWhenUserIdIsNegative(String method) throws Exception {

        String linkDtoIdPath = LinkEndpoints.BASE_LINKS + LinkEndpoints.BY_USER
            .replace("{userId}", "-1");

        MockHttpServletRequestBuilder requestBuilder = switch(method) {
            case "GET" -> get(linkDtoIdPath);
            case "DELETE" -> delete(linkDtoIdPath);
            default -> throw new IllegalArgumentException("Invalid method");
        };

        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").exists())
            .andExpect(jsonPath("$.errors[*]", hasItem(containsString("userId"))))
            .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
            .andExpect(jsonPath("$.status").value(400));
    }
}
