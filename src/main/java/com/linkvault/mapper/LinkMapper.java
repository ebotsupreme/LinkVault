package com.linkvault.mapper;

import com.linkvault.dto.LinkDto;
import com.linkvault.model.Link;
import com.linkvault.model.User;

public class LinkMapper {
    public static LinkDto toDto(Link link, Long userId) {
        return new LinkDto(
            link.getId(),
            link.getUrl(),
            link.getTitle(),
            link.getDescription(),
            userId
        );
    }

    public static Link toEntity(LinkDto linkDto, User user) {
        return new Link(linkDto.url(), linkDto.title(), linkDto.description(), user);
    }
}
