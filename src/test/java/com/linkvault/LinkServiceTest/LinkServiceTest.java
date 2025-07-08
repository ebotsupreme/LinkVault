package com.linkvault.LinkServiceTest;

import com.linkvault.dto.LinkDto;
import com.linkvault.model.Link;
import com.linkvault.model.User;
import com.linkvault.repository.LinkRepository;
import com.linkvault.service.LinkService;
import com.linkvault.service.LinkServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LinkServiceTest {
    @Mock
    private LinkRepository linkRepository;
    private LinkService linkService;
    private User user;

    @BeforeEach
    void setUp() {
        linkService = new LinkServiceImpl(linkRepository);
        user = new User("eddie", "password123");
        user.setId(1L);
    }

    @Test
    void getAllLinks_ShouldReturnListOfLinkDtos_WhenUserHasLinks() {
        // Arrange
        Link link1 = new Link("https://github.com", "Git Hub", "Repositories", user);
        Link link2 = new Link("https://spring.io", "Spring Boot", "Learning Spring Boot", user);
        when(linkRepository.findByUserId(user.getId())).thenReturn(List.of(link1, link2));

        // Act
        List<LinkDto> result = linkService.getAllLinksForUser(user.getId());

        // Assert
        assertEquals(2, result.size());
        assertEquals("Git Hub", result.get(0).title());
        assertEquals("Spring Boot", result.get(1).title());
    }

    @Test
    void getLinkById_ShouldReturnLinkDto_WhenIdExists() {
        // Arrange
        Link link = new Link("https://github.com", "Git Hub", "Repositories", user);
        link.setId(1L);
        when(linkRepository.findById(link.getId())).thenReturn(Optional.of(link));

        // Act
        Optional<LinkDto> result = linkService.getLinkById(link.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Git Hub", result.get().title());
    }

    @Test
    void getLinkById_ShouldFail_WhenNoIdExists() {
        // Arrange
        when(linkRepository.findById(2L)).thenReturn(Optional.empty());

        // Act
        Optional<LinkDto> result = linkService.getLinkById(2L);

        // Assert
        assertTrue(result.isEmpty());
    }


}
