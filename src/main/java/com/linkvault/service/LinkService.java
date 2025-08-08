package com.linkvault.service;

import com.linkvault.dto.LinkRequest;
import com.linkvault.dto.LinkResponse;

import java.util.List;
import java.util.Optional;

public interface LinkService {
    List<LinkResponse> getAllLinksForUser(Long userId);
    LinkResponse getLinkById(Long linkId, Long requestingUserId);
    LinkResponse createLink(Long userId, LinkRequest linkRequest);
    LinkResponse updateLink(Long linkId, LinkRequest linkRequest, Long requestingUserId);
    void deleteLink(Long linkId, Long requestingUserId);
    void deleteAllLinksByUser(Long userId);
}
