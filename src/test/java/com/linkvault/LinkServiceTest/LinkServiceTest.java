package com.linkvault.LinkServiceTest;

import com.linkvault.dto.LinkDto;
import com.linkvault.exception.LinkNotFoundException;
import com.linkvault.exception.LinkSaveException;
import com.linkvault.exception.UserNotFoundException;
import com.linkvault.model.Link;
import com.linkvault.model.User;
import com.linkvault.repository.LinkRepository;
import com.linkvault.repository.UserRepository;
import com.linkvault.service.LinkService;
import com.linkvault.service.LinkServiceImpl;
import com.linkvault.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.linkvault.util.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LinkServiceTest {
    @Mock
    private LinkRepository linkRepository;
    @Mock
    private UserRepository userRepository;
    private LinkService linkService;
    private User user;
    private Link link1;
    private Link link2;
    private LinkDto linkDto;

    @BeforeEach
    void setUp() {
        linkService = new LinkServiceImpl(linkRepository, userRepository);
        user = TestDataFactory.createTestUser();
        link1 = TestDataFactory.createLink1();
        link2 = TestDataFactory.createLink2();
        linkDto = TestDataFactory.createLinkDto1();
    }

    @Test
    void shouldReturnListOfLinkDtosWhenUserHasLinks() {
        // Arrange
        when(linkRepository.findByUserId(user.getId())).thenReturn(List.of(link1, link2));

        // Act
        List<LinkDto> result = linkService.getAllLinksForUser(user.getId());

        // Assert
        assertEquals(2, result.size());
        assertEquals(link1.getTitle(), result.get(0).title());
        assertEquals(link2.getTitle(), result.get(1).title());
    }

    @Test
    void shouldReturnLinkDtoWhenIdExists() {
        // Arrange
        when(linkRepository.findById(link1.getId())).thenReturn(Optional.of(link1));

        // Act
        Optional<LinkDto> result = linkService.getLinkById(link1.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(link1.getTitle(), result.get().title());
    }

    @Test
    void shouldThrowExceptionWhenLinkDoesNotExist() {
        // Arrange
        when(linkRepository.findById(TEST_ID2)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(LinkNotFoundException.class, () -> {
            linkService.getLinkById(TEST_ID2);
        });
    }

    @Test
    void shouldCreateLinkForGivenUser() {
        // Arrange
        link1.setId(TEST_ID1);
        when(userRepository.findById(TEST_ID1)).thenReturn(Optional.of(user));
        when(linkRepository.save(any(Link.class))).thenReturn(link1);

        // Act
        Optional<LinkDto> result = linkService.createLink(user.getId(), linkDto);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(link1.getTitle(), result.get().title());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(userRepository.findById(TEST_ID3)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            linkService.createLink(TEST_ID3, linkDto);
        });
    }

    @Test
    void shouldThrowExceptionWhenLinkSaveFails(){
        // Arrange
        when(userRepository.findById(TEST_ID1)).thenReturn(Optional.of(user));
        when(linkRepository.save(any(Link.class))).thenThrow(new RuntimeException("Database failure"));

        // Act & Assert
        assertThrows(LinkSaveException.class, () -> {
            linkService.createLink(TEST_ID1, linkDto);
        });
    }
}
