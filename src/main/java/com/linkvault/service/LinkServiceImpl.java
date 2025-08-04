package com.linkvault.service;

import com.linkvault.dto.LinkDto;
import com.linkvault.exception.*;
import com.linkvault.mapper.LinkMapper;
import com.linkvault.model.Link;
import com.linkvault.model.User;
import com.linkvault.repository.LinkRepository;
import com.linkvault.repository.UserRepository;
import com.linkvault.util.LogMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.linkvault.util.LogUtils.*;

@Slf4j
@Service
public class LinkServiceImpl implements LinkService{
    private final LinkRepository linkRepository;
    private final UserRepository userRepository;

    public LinkServiceImpl(LinkRepository linkRepository, UserRepository userRepository) {
        this.linkRepository = linkRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<LinkDto> getAllLinksForUser(Long userId) {
        info(log, LogMessages.FETCH_LINKS_FOR_USER, userId);
        List<Link> links = linkRepository.findByUserId(userId);
        info(log, "Found {} links for user ID: {}", links.size(), userId);

        return links.stream()
            .map(link -> LinkMapper.toDto(link, userId)).toList();
    }

    @Transactional(readOnly = true)
    public Optional<LinkDto> getLinkById(Long linkId) {
        info(log,"Fetching link by ID: {}", linkId);
        Link link = linkRepository.findById(linkId)
           .orElseThrow(() -> new LinkNotFoundException(linkId));
        info(log, LogMessages.FOUND_LINK, linkId);

       return Optional.of(LinkMapper.toDto(link, link.getUser().getId()));
    }

    @Transactional
    public Optional<LinkDto> createLink(Long userId, LinkDto linkDto) {
        info(log, LogMessages.FETCH_USER, userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        Link link = new Link(linkDto.url(), linkDto.title(), linkDto.description(), user);

        try {
            info(log, "Saving link for user ID: {}", userId);
            debug(log, LogMessages.FETCH_USER, link);

            Link savedLink = linkRepository.save(link);
            info(log, "Link saved successfully: ID {}", savedLink.getId());
            return Optional.of(LinkMapper.toDto(savedLink, link.getUser().getId()));
        } catch (RuntimeException e) {
            throw new LinkSaveException(linkDto, e);
        }
    }

    @Transactional
    public Optional<LinkDto> updateLink(Long linkId, LinkDto linkDto, Long requestingUserId) {
        Link existingLink = linkRepository.findById(linkId)
            .orElseThrow(() -> new LinkNotFoundException(linkId));
        info(log, LogMessages.FOUND_LINK, linkId);

        Long ownerId = existingLink.getUser().getId();
        info(log, "Owner ID: {}", ownerId);

        info(log, LogMessages.VALIDATE_USER, requestingUserId);
        if (!ownerId.equals(requestingUserId)) {
            throw new UnauthorizedAccessException(
                ExceptionMessages.USER_NOT_AUTHORIZED_TO_UPDATE, requestingUserId
            );
        }

        debug(log, "Received LinkDto for update: {}", linkDto);
        existingLink.setUrl(linkDto.url());
        existingLink.setTitle(linkDto.title());
        existingLink.setDescription(linkDto.description());

        try {
            info(log, "Updating link by ID: {}", existingLink.getId());
            debug(log, "Link to be updated: {}", existingLink);

            Link updatedLink = linkRepository.save(existingLink);
            info(log, "Link updated successfully: ID {}", existingLink.getId());
            return Optional.of(LinkMapper.toDto(updatedLink, ownerId));
        } catch (RuntimeException e) {
            throw new LinkSaveException(linkDto, e);
        }
    }

    @Transactional
    public void deleteLink(Long linkId, Long requestingUserId) {
        Link linkToDelete = linkRepository.findById(linkId)
            .orElseThrow(() -> new LinkNotFoundException(linkId));
        info(log, LogMessages.FOUND_LINK, linkId);

        Long ownerId = linkToDelete.getUser().getId();
        info(log, "Owner ID: {}", ownerId);

        info(log, LogMessages.VALIDATE_USER, requestingUserId);
        if (!ownerId.equals(requestingUserId)) {
            throw new UnauthorizedAccessException(
                "User not authorized to delete this link", requestingUserId
            );
        }

        LinkDto linkToDeleteDto = new LinkDto(
            linkToDelete.getId(), linkToDelete.getUrl(), linkToDelete.getTitle(),
            linkToDelete.getDescription(), ownerId);

        try {
            info(log, "Deleting link ID: {}", linkId);
            debug(log, "Link to be deleted: {}", linkToDelete);

            linkRepository.deleteById(linkId);
            info(log, "Successfully deleted link for user ID: {}", ownerId);
        } catch (RuntimeException e) {
            throw new LinkDeleteException(linkToDeleteDto, e);
        }
    }

    @Transactional
    public void deleteAllLinksByUser(Long userId) {
        info(log, LogMessages.FETCH_LINKS_FOR_USER, userId);
        List<Link> links = linkRepository.findByUserId(userId);

        if (links.isEmpty()) {
            throw new LinksNotFoundException(userId);
        }

        try {
            debug(log, "Number of links to be deleted: {}", links.size());

            linkRepository.deleteAll(links);
            info(log, "Successfully deleted all links for user ID: {}", userId);
        } catch (RuntimeException e) {
            throw new LinksDeleteException(userId, e);
        }
    }
}
