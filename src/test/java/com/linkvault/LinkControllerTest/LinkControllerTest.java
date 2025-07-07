package com.linkvault.LinkControllerTest;

import com.linkvault.controller.LinkController;
import com.linkvault.dto.LinkDto;
import com.linkvault.model.Link;
import com.linkvault.model.User;
import com.linkvault.repository.LinkRepository;
import com.linkvault.service.LinkServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LinkController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LinkControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private LinkServiceImpl linkServiceImpl;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("eddie", "password123");
        user.setId(1L);
    }

    @Test
    void getAllLinksForUser_ShouldReturnAllLinksForUser() throws Exception {
        // Arrange
        LinkDto link1 = new LinkDto(1L, "https://github.com", "Git Hub", "Repositories");
        LinkDto link2 = new LinkDto(2L, "https://spring.io", "Spring Boot", "Learning Spring Boot");
        when(linkServiceImpl.getAllLinksForUser(user.getId())).thenReturn(List.of(link1, link2));

        // Assert
        mockMvc.perform(get("/api/links/user/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Git Hub"))
            .andExpect(jsonPath("$[1].title").value("Spring Boot"));
    }
}
