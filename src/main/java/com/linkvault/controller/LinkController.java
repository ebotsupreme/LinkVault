package com.linkvault.controller;

import com.linkvault.constants.apiPaths.LinkEndpoints;
import com.linkvault.dto.LinkDto;
import com.linkvault.model.Link;
import com.linkvault.service.LinkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(LinkEndpoints.BASE_LINKS)
public class LinkController {
    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping(LinkEndpoints.BY_USER)
    public List<LinkDto> getAllLinksForUser(@PathVariable Long userId) {
        return linkService.getAllLinksForUser(userId);
    }

    @GetMapping(LinkEndpoints.BY_LINK_ID)
    public ResponseEntity<LinkDto> getLinkById(@PathVariable Long linkId) {
        return linkService.getLinkById(linkId)
            .map(ResponseEntity::ok)
            .orElseGet(()-> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LinkDto> createLink(@RequestBody LinkDto linkDto) {
        return linkService.createLink(linkDto.userId(), linkDto)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(LinkEndpoints.BY_LINK_ID)
    public ResponseEntity<LinkDto> updateLink(
        @PathVariable Long linkId,
        @RequestBody LinkDto linkDto
    ) {
        if (!linkId.equals(linkDto.id())) {
            return ResponseEntity.badRequest().build();
        }
        return linkService.updateLink(linkId, linkDto)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping(LinkEndpoints.BY_LINK_ID)
    public ResponseEntity<Void> deleteLink(@PathVariable Long linkId) {
        linkService.deleteLink(linkId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(LinkEndpoints.BY_USER)
    public ResponseEntity<Void> deleteAllLinksByUser(@PathVariable Long userId) {
        linkService.deleteAllLinksByUser(userId);
        return ResponseEntity.noContent().build();
    }
}
