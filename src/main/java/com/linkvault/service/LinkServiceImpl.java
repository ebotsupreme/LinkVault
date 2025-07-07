package com.linkvault.service;

import com.linkvault.dto.LinkDto;
import com.linkvault.mapper.LinkMapper;
import com.linkvault.model.Link;
import com.linkvault.repository.LinkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LinkServiceImpl implements LinkService{
    private final LinkRepository linkRepository;

    public LinkServiceImpl(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    public List<LinkDto> getAllLinksForUser(Long userId) {
        List<Link> links = linkRepository.findByUserId(userId);

        return links.stream()
            .map(LinkMapper::toDto).toList();
    }
}
