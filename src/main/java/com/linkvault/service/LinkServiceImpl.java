package com.linkvault.service;

import com.linkvault.dto.LinkDto;
import com.linkvault.exception.*;
import com.linkvault.mapper.LinkMapper;
import com.linkvault.model.Link;
import com.linkvault.model.User;
import com.linkvault.repository.LinkRepository;
import com.linkvault.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class LinkServiceImpl implements LinkService{
    private final LinkRepository linkRepository;
    private final UserRepository userRepository;

    public LinkServiceImpl(LinkRepository linkRepository, UserRepository userRepository) {
        this.linkRepository = linkRepository;
        this.userRepository = userRepository;
    }

    public List<LinkDto> getAllLinksForUser(Long userId) {
        List<Link> links = linkRepository.findByUserId(userId);
        return links.stream()
            .map(link -> LinkMapper.toDto(link, userId)).toList();
    }

    public Optional<LinkDto> getLinkById(Long linkId) {
       Link link = linkRepository.findById(linkId)
           .orElseThrow(() -> new LinkNotFoundException(linkId));
       return Optional.of(LinkMapper.toDto(link, link.getUser().getId()));
    }

    public Optional<LinkDto> createLink(Long userId, LinkDto linkDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        Link link = new Link(linkDto.url(), linkDto.title(), linkDto.description(), user);

        try {
            Link savedLink = linkRepository.save(link);
            return Optional.of(LinkMapper.toDto(savedLink, link.getUser().getId()));
        } catch (RuntimeException e) {
            throw new LinkSaveException(linkDto, e);
        }
    }

    public Optional<LinkDto> updateLink(Long linkId, LinkDto linkDto) {
        User user = userRepository.findById(linkDto.userId())
            .orElseThrow(() -> new UserNotFoundException(linkDto.userId()));
        Link existingLink = linkRepository.findById(linkId)
            .orElseThrow(() -> new LinkNotFoundException(linkId));
        existingLink.setUrl(linkDto.url());
        existingLink.setTitle(linkDto.title());
        existingLink.setDescription(linkDto.description());
        existingLink.setUser(user);

        if (!existingLink.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException(ExceptionMessages.USER_NOT_AUTHORIZED_TO_UPDATE);
        }

        try {
            Link updatedLink = linkRepository.save(existingLink);
            return Optional.of(LinkMapper.toDto(updatedLink, existingLink.getUser().getId()));
        } catch (RuntimeException e) {
            throw new LinkSaveException(linkDto, e);
        }
    }

    public void deleteLink(Long linkId) {
        Link linkToDelete = linkRepository.findById(linkId)
            .orElseThrow(() -> new LinkNotFoundException(linkId));
        User user = userRepository.findById(linkToDelete.getUser().getId())
            .orElseThrow(() -> new UserNotFoundException(linkToDelete.getUser().getId()));
        LinkDto linkToDeleteDto = new LinkDto(
            linkToDelete.getId(), linkToDelete.getUrl(), linkToDelete.getTitle(),
            linkToDelete.getDescription(), linkToDelete.getUser().getId());

        if (!linkToDelete.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException(ExceptionMessages.USER_NOT_AUTHORIZED_TO_DELETE);
        }

        try {
            linkRepository.deleteById(linkId);
        } catch (RuntimeException e) {
            throw new LinkDeleteException(linkToDeleteDto, e);
        }
    }

    public void deleteAllLinksByUser(Long userId) {
        List<Link> links = linkRepository.findByUserId(userId);

        if (links.isEmpty()) {
            throw new LinksNotFoundException(userId);
        }

        try {
            linkRepository.deleteAll(links);
        } catch (RuntimeException e) {
            throw new LinksDeleteException(userId, e);
        }
    }
}
