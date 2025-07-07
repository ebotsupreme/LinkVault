package com.linkvault.service;

import com.linkvault.dto.LinkDto;

import java.util.List;

public interface LinkService {
    List<LinkDto> getAllLinksForUser(Long userId);
}
