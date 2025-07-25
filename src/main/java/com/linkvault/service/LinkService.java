package com.linkvault.service;

import com.linkvault.dto.LinkDto;

import java.util.List;
import java.util.Optional;

public interface LinkService {
    List<LinkDto> getAllLinksForUser(Long userId);
    Optional<LinkDto> getLinkById(Long linkId);
    Optional<LinkDto> createLink(Long userId, LinkDto linkDto);
    Optional<LinkDto> updateLink(Long linkId, LinkDto linkDto);
    void deleteLink(Long linkId);
    void deleteAllLinksByUser(Long userId);
}
