package com.linkvault.service;

import com.linkvault.dto.LinkDto;

import java.util.List;
import java.util.Optional;

public interface LinkService {
    List<LinkDto> getAllLinksForUser(Long userId);
    Optional<LinkDto> getLinkById(Long linkId);
}
