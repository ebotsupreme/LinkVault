package com.linkvault.service;

import com.linkvault.dto.LinkDto;
import com.linkvault.mapper.LinkMapper;
import com.linkvault.model.Link;
import com.linkvault.model.User;
import com.linkvault.repository.LinkRepository;
import com.linkvault.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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
            .map(LinkMapper::toDto).toList();
    }

    public Optional<LinkDto> getLinkById(Long linkId) {
       return linkRepository.findById(linkId).map(LinkMapper::toDto);
    }

    public Optional<LinkDto> createLink(Long userId, LinkDto linkDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Link link = new Link(linkDto.url(), linkDto.title(), linkDto.description(), user);

        Link savedlink = linkRepository.save(link);
        return Optional.of(LinkMapper.toDto(savedlink));
    }
}
