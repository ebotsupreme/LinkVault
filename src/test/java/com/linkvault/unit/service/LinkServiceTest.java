package com.linkvault.unit.service;

import com.linkvault.dto.LinkRequest;
import com.linkvault.dto.LinkResponse;
import com.linkvault.exception.*;
import com.linkvault.model.Link;
import com.linkvault.model.User;
import com.linkvault.repository.LinkRepository;
import com.linkvault.repository.UserRepository;
import com.linkvault.service.LinkService;
import com.linkvault.service.LinkServiceImpl;
import com.linkvault.unit.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.linkvault.unit.util.TestDataFactory.*;
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

    private LinkRequest linkRequest;
    private LinkRequest linkRequestTwo;

    @BeforeEach
    void setUp() {
        linkService = new LinkServiceImpl(linkRepository, userRepository);
        user = TestDataFactory.createTestUser();
        link1 = TestDataFactory.createLink1();
        link2 = TestDataFactory.createLink2();
        linkRequest = TestDataFactory.createLinkRequest();
        linkRequestTwo = TestDataFactory.createLinkRequestTwo();
    }

    @Test
    void shouldReturnListOfLinkResponsesWhenUserHasLinks() {
        // Arrange
        when(linkRepository.findByUserId(user.getId())).thenReturn(List.of(link1, link2));

        // Act
        List<LinkResponse> result = linkService.getAllLinksForUser(user.getId());

        // Assert
        assertEquals(2, result.size());
        assertEquals(link1.getTitle(), result.getFirst().title());
        assertEquals(link1.getUrl(), result.getFirst().url());
        assertEquals(link1.getUser().getId(), result.getFirst().userId());

        assertEquals(link2.getTitle(), result.get(1).title());
        assertEquals(link2.getUrl(), result.get(1).url());

        verify(linkRepository).findByUserId(user.getId());
    }

    @Test
    void shouldReturnLinkWhenIdExists() {
        // Arrange
        when(linkRepository.findById(link1.getId())).thenReturn(Optional.of(link1));

        // Act
        LinkResponse result = linkService.getLinkById(link1.getId(), user.getId());

        // Assert
        assertNotNull(result);
        assertEquals(link1.getTitle(), result.title());

        verify(linkRepository).findById(link1.getId());
    }

    @Test
    void shouldThrowExceptionWhenLinkDoesNotExist() {
        // Arrange
        when(linkRepository.findById(link2.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(LinkNotFoundException.class, () ->
            linkService.getLinkById(link2.getId(), user.getId()));

        verify(linkRepository).findById(link2.getId());
    }

    @Test
    void shouldCreateLinkForGivenUser() {
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(linkRepository.save(any(Link.class))).thenReturn(link1);

        // Act
        LinkResponse result = linkService.createLink(user.getId(), linkRequest);

        // Assert
        assertNotNull(result);
        assertEquals(link1.getTitle(), result.title());

        verify(userRepository).findById(user.getId());
        verify(linkRepository).save(any(Link.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(userRepository.findById(TEST_ID3)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> linkService.createLink(TEST_ID3, linkRequest));

        verify(userRepository).findById(TEST_ID3);
    }

    @Test
    void shouldThrowExceptionWhenLinkSaveFails(){
        // Arrange
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(linkRepository.save(any(Link.class)))
            .thenThrow(new RuntimeException(ExceptionMessages.DATABASE_FAILURE));

        // Act & Assert
        assertThrows(LinkSaveException.class, () -> linkService.createLink(user.getId(), linkRequest));

        verify(userRepository).findById(user.getId());
        verify(linkRepository).save(any(Link.class));
    }

    @Test
    void shouldUpdateLinkForGivenUser() {
        // Arrange
        when(linkRepository.findById(link1.getId())).thenReturn(Optional.of(link1));
        when(linkRepository.save(any(Link.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LinkResponse result = linkService.updateLink(link1.getId(), linkRequestTwo, user.getId());

        // Assert
        assertNotNull(result);
        assertEquals(linkRequestTwo.url(), result.url());
        assertEquals(linkRequestTwo.title(), result.title());
        assertEquals(linkRequestTwo.description(), result.description());

        verify(linkRepository).findById(link1.getId());

        ArgumentCaptor<Link> captor = ArgumentCaptor.forClass(Link.class);
        verify(linkRepository).save(captor.capture());

        Link savedLink = captor.getValue();
        assertEquals(linkRequestTwo.url(), savedLink.getUrl());
        assertEquals(linkRequestTwo.title(), savedLink.getTitle());
        assertEquals(linkRequestTwo.description(), savedLink.getDescription());
    }

    @Test
    void shouldThrowExceptionWhenLinkNotFoundDuringUpdate() {
        // Arrange
        when(linkRepository.findById(link2.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(LinkNotFoundException.class, () ->
            linkService.updateLink(link2.getId(), linkRequestTwo, user.getId()));

        verify(linkRepository).findById(link2.getId());
    }

    @Test
    void shouldThrowExceptionWhenLinkSaveFailsDuringUpdate() {
        // Arrange
        when(linkRepository.findById(link2.getId())).thenReturn(Optional.of(link2));
        when(linkRepository.save(any(Link.class)))
            .thenThrow(new RuntimeException(ExceptionMessages.DATABASE_FAILURE));

        // Act & Assert
        assertThrows(LinkSaveException.class, () ->
            linkService.updateLink(link2.getId(), linkRequestTwo, user.getId()));

        verify(linkRepository).findById(link2.getId());
        verify(linkRepository).save(any(Link.class));
    }

    @Test
    void shouldDeleteLinkForGivenUser() {
        // Arrange
        when(linkRepository.findById(link1.getId())).thenReturn(Optional.of(link1));
        doNothing().when(linkRepository).deleteById(link1.getId());

        // Act
        linkService.deleteLink(link1.getId(), user.getId());

        // Assert
        verify(linkRepository).findById(link1.getId());
        verify(linkRepository).deleteById(link1.getId());
    }

    @Test
    void shouldThrowExceptionWhenLinkNotFoundDuringDelete() {
        // Arrange
        when(linkRepository.findById(link2.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(LinkNotFoundException.class, () ->
            linkService.deleteLink(link2.getId(), user.getId()));

        verify(linkRepository).findById(link2.getId());
    }

    @Test
    void shouldThrowExceptionWhenLinkDeleteFails() {
        // Arrange
        when(linkRepository.findById(link2.getId())).thenReturn(Optional.of(link2));
        doThrow(new RuntimeException(ExceptionMessages.DATABASE_FAILURE))
            .when(linkRepository).deleteById(link2.getId());

        // Act & Assert
        assertThrows(LinkDeleteException.class, () ->
            linkService.deleteLink(link2.getId(), user.getId()));

        verify(linkRepository).findById(link2.getId());
        verify(linkRepository).deleteById(link2.getId());
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

    @Test
    void shouldReturnSilentlyWhenNoLinksToDelete() {
        // Arrange
        when(linkRepository.findByUserId(user.getId())).thenReturn(List.of());

        // Act & Assert
        linkService.deleteAllLinksByUser(user.getId());

        verify(linkRepository).findByUserId(user.getId());
        verify(linkRepository, never()).deleteAll(any());
    }

    @Test
    void shouldThrowExceptionWhenDeleteAllFails() {
        // Arrange
        List<Link> linkList = new ArrayList<>(List.of(link1, link2));
        when(linkRepository.findByUserId(user.getId())).thenReturn(linkList);
        doThrow(new RuntimeException(ExceptionMessages.DATABASE_FAILURE))
            .when(linkRepository).deleteAll(linkList);

        // Act & Assert
        assertThrows(LinksDeleteException.class, () ->
            linkService.deleteAllLinksByUser(user.getId()));

        verify(linkRepository).findByUserId(user.getId());
        verify(linkRepository).deleteAll(linkList);
    }
}
