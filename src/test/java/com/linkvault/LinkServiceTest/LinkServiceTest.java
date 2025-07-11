package com.linkvault.LinkServiceTest;

import com.linkvault.dto.LinkDto;
import com.linkvault.exception.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.linkvault.util.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    private LinkDto linkDto1;
    private LinkDto linkDto2;

    @BeforeEach
    void setUp() {
        linkService = new LinkServiceImpl(linkRepository, userRepository);
        user = TestDataFactory.createTestUser();
        link1 = TestDataFactory.createLink1();
        link2 = TestDataFactory.createLink2();
        linkDto1 = TestDataFactory.createLinkDto1();
        linkDto2 = TestDataFactory.createLinkDto2();
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

        verify(linkRepository).findByUserId(user.getId());
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

        verify(linkRepository).findById(link1.getId());
    }

    @Test
    void shouldThrowExceptionWhenLinkDoesNotExist() {
        // Arrange
        when(linkRepository.findById(link2.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(LinkNotFoundException.class, () -> {
            linkService.getLinkById(link2.getId());
        });
    }

    @Test
    void shouldCreateLinkForGivenUser() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(linkRepository.save(any(Link.class))).thenReturn(link1);

        // Act
        Optional<LinkDto> result = linkService.createLink(user.getId(), linkDto1);

        // Assert
        assertTrue(result.isPresent());
        LinkDto created = result.get();
        assertEquals(link1.getTitle(), created.title());

        verify(userRepository).findById(user.getId());
        verify(linkRepository).save(any(Link.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(userRepository.findById(TEST_ID3)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            linkService.createLink(TEST_ID3, linkDto1);
        });
    }

    @Test
    void shouldThrowExceptionWhenLinkSaveFails(){
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(linkRepository.save(any(Link.class))).thenThrow(new RuntimeException(ExceptionMessages.DATABASE_FAILURE));

        // Act & Assert
        assertThrows(LinkSaveException.class, () -> {
            linkService.createLink(user.getId(), linkDto1);
        });
    }

    @Test
    void shouldUpdateLinkForGivenUser() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(linkRepository.findById(link1.getId())).thenReturn(Optional.of(link1));

        // Simulate update
        link1.setUrl(linkDto2.url());
        link1.setTitle(linkDto2.title());
        link1.setDescription(linkDto2.description());

        when(linkRepository.save(any(Link.class))).thenReturn(link1);

        // Act
        Optional<LinkDto> result = linkService.updateLink(link1.getId(), linkDto2);

        // Assert
        assertTrue(result.isPresent());
        LinkDto updated = result.get();
        assertEquals(link1.getUrl(), updated.url());
        assertEquals(link1.getTitle(), updated.title());
        assertEquals(link1.getDescription(), updated.description());

        verify(userRepository).findById(user.getId());
        verify(linkRepository).findById(link1.getId());
        verify(linkRepository).save(link1);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringUpdate() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> linkService.updateLink(link1.getId(), linkDto1));
    }

    @Test
    void shouldThrowExceptionWhenLinkNotFoundDuringUpdate() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(linkRepository.findById(linkDto2.id())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(LinkNotFoundException.class, () -> linkService.updateLink(linkDto2.id(), linkDto2));
    }

    @Test
    void shouldThrowExceptionWhenLinkSaveFailsDuringUpdate() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(linkRepository.findById(linkDto2.id())).thenReturn(Optional.of(link2));
        when(linkRepository.save(any(Link.class))).thenThrow(new RuntimeException(ExceptionMessages.DATABASE_FAILURE));

        // Act & Assert
        assertThrows(LinkSaveException.class, () -> linkService.updateLink(link2.getId(), linkDto2));
    }

    @Test
    void shouldDeleteLinkForGivenUser() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(linkRepository.findById(linkDto1.id())).thenReturn(Optional.of(link1));
        doNothing().when(linkRepository).deleteById(link1.getId());

        // Act
        linkService.deleteLink(link1.getId());

        // Assert
        verify(userRepository).findById(user.getId());
        verify(linkRepository).findById(linkDto1.id());
        verify(linkRepository).deleteById(link1.getId());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringDelete() {
        // Arrange
        when(linkRepository.findById(linkDto1.id())).thenReturn(Optional.of(link1));
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> linkService.deleteLink(link1.getId()));
    }

    @Test
    void shouldThrowExceptionWhenLinkNotFoundDuringDelete() {
        // Arrange
        when(linkRepository.findById(linkDto2.id())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(LinkNotFoundException.class, () -> linkService.deleteLink(linkDto2.id()));
    }

    @Test
    void shouldThrowExceptionWhenLinkDeleteFails() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(linkRepository.findById(linkDto2.id())).thenReturn(Optional.of(link2));
        doThrow(new RuntimeException(ExceptionMessages.DATABASE_FAILURE))
            .when(linkRepository).deleteById(link2.getId());

        // Act & Assert
        assertThrows(LinkDeleteException.class, () -> linkService.deleteLink(link2.getId()));
    }

    @Test
    void shouldDeleteAllLinksForGivenUser() {
        // Arrange
        List<Link> linkList = new ArrayList<>(List.of(link1, link2));
        when(linkRepository.findByUserId(user.getId())).thenReturn(linkList);
        doNothing().when(linkRepository).deleteAll(linkList);

        // Act
        linkService.deleteAllLinksByUser(user.getId());

        // Assert
        verify(linkRepository).findByUserId(user.getId());
        verify(linkRepository).deleteAll(linkList);
    }
}
