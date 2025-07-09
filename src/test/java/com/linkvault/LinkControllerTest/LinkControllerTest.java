package com.linkvault.LinkControllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkvault.controller.LinkController;
import com.linkvault.dto.LinkDto;
import com.linkvault.model.User;
import com.linkvault.service.LinkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private final Long TEST_ID1 = 1L;
    private final Long TEST_ID2 = 2L;

    @BeforeEach
    void setUp() {

        user = new User("eddie", "password123");
        user.setId(TEST_ID1);

        linkDto1 = new LinkDto(TEST_ID1, "https://github.com",
            "Git Hub", "Repositories", user.getId());
        linkDto2 = new LinkDto(TEST_ID2, "https://spring.io",
            "Spring Boot", "Learning Spring Boot", user.getId());
    }

    @Test
    void shouldReturnAllLinksForUserWhenUserHasLinks() throws Exception {
        // Arrange
        when(linkService.getAllLinksForUser(user.getId())).thenReturn(List.of(linkDto1, linkDto2));

        // Assert
        mockMvc.perform(get("/api/links/user/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Git Hub"))
            .andExpect(jsonPath("$[1].title").value("Spring Boot"));
    }

    @Test
    void shouldReturnLinkWhenIdExists() throws Exception {
        // Arrange
        when(linkService.getLinkById(linkDto1.id())).thenReturn(Optional.of(linkDto1));

        // Assert
        mockMvc.perform(get("/api/links/" + TEST_ID1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Git Hub"));
    }

    @Test
    void shouldReturnNotFoundStatusWhenLinkDoesNotExist() throws Exception {
        // Arrange
        when(linkService.getLinkById(TEST_ID1)).thenReturn(Optional.empty());

        // Assert
        mockMvc.perform(get("/api/links/" + TEST_ID1))
            .andExpect(status().isNotFound())
            .andExpect(content().string(""));
    }

    @Test
    void shouldReturnLinkWhenNewLinkIsCreated() throws Exception {
        // Arrange
        when(linkService.createLink(TEST_ID1, linkDto2)).thenReturn(Optional.of(linkDto2));
        String json = objectMapper.writeValueAsString(linkDto2);

        // Assert
        mockMvc.perform(post("/api/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value(linkDto2.title()));
    }

    @Test
    void shouldReturnNotFoundStatusWhenLinkSaveFails() throws Exception {
        // Arrange
        when(linkService.createLink(TEST_ID1, linkDto2)).thenReturn(Optional.empty());
        String json = objectMapper.writeValueAsString(linkDto2);

        // Assert
        mockMvc.perform(post("/api/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isNotFound());
    }
}
