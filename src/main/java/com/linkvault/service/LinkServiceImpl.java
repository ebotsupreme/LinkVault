package com.linkvault.service;

import com.linkvault.dto.LinkRequest;
import com.linkvault.dto.LinkResponse;
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
    public List<LinkResponse> getAllLinksForUser(Long userId) {
        info(log, LogMessages.FETCH_LINKS_FOR_USER, userId);
        List<Link> links = linkRepository.findByUserIdOrderByCreatedAtAsc(userId);

        info(log, "Found {} links for user ID: {}", links.size(), userId);
        return links.stream()
            .map(LinkMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public LinkResponse getLinkById(Long linkId, Long requestingUserId) {
        info(log,"Fetching link by ID: {}", linkId);
        Link link = linkRepository.findById(linkId)
           .orElseThrow(() -> new LinkNotFoundException(linkId));

        Long ownerId = link.getUser().getId();
        info(log, "Owner ID: {}", ownerId);

        info(log, LogMessages.VALIDATE_USER, requestingUserId);
        if (!ownerId.equals(requestingUserId)) {
            throw new UnauthorizedAccessException(
                "User not authorized to fetch this link", requestingUserId
            );
        }

        info(log, LogMessages.FOUND_LINK, link.getId());
       return LinkMapper.toResponse(link);
    }

    @Transactional
    public LinkResponse createLink(Long userId, LinkRequest linkRequest) {
        info(log, LogMessages.FETCH_USER, userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        Link link = new Link(linkRequest.url(), linkRequest.title(), linkRequest.description(), user);

        try {
            info(log, "Saving link for user ID: {}", userId);
            debug(log, LogMessages.FETCH_USER, link);

            Link savedLink = linkRepository.save(link);
            info(log, "Link saved successfully: ID {}", savedLink.getId());
            return LinkMapper.toResponse(savedLink);
        } catch (RuntimeException e) {
            throw new LinkSaveException(link.getId(), userId, e);
        }
    }

    @Transactional
    public LinkResponse updateLink(Long linkId, LinkRequest linkRequest, Long requestingUserId) {
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

        debug(log, "Received linkRequest for update: {}", linkRequest);
        existingLink.setUrl(linkRequest.url());
        existingLink.setTitle(linkRequest.title());
        existingLink.setDescription(linkRequest.description());

        try {
            info(log, "Updating link by ID: {}", existingLink.getId());
            debug(log, "Link to be updated: {}", existingLink);

            Link updatedLink = linkRepository.save(existingLink);
            info(log, "Link updated successfully: ID {}", existingLink.getId());
            return LinkMapper.toResponse(updatedLink);
        } catch (RuntimeException e) {
            throw new LinkSaveException(existingLink.getId(), ownerId, e);
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

        try {
            info(log, "Deleting link ID: {}", linkId);
            debug(log, "Link to be deleted: {}", linkToDelete);

            linkRepository.deleteById(linkId);
            info(log, "Successfully deleted link for user ID: {}", ownerId);
        } catch (RuntimeException e) {
            throw new LinkDeleteException(linkId, ownerId, e);
        }
    }

    @Transactional
    public void deleteAllLinksByUser(Long userId) {
        info(log, LogMessages.FETCH_LINKS_FOR_USER, userId);
        List<Link> links = linkRepository.findByUserIdOrderByCreatedAtAsc(userId);

        if (links.isEmpty()) {
            info(log, "No links found for user ID: {}, nothing to delete", userId);
            return;
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
