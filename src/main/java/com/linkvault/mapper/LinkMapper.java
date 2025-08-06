package com.linkvault.mapper;

import com.linkvault.dto.LinkResponse;
import com.linkvault.model.Link;

public class LinkMapper {
    public static LinkResponse toResponse(Link link) {
        return new LinkResponse(
            link.getId(),
            link.getUrl(),
            link.getTitle(),
            link.getDescription(),
            link.getUser().getId()
        );
    }
}
