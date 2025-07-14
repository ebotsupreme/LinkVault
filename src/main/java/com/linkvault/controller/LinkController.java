package com.linkvault.controller;

import com.linkvault.constants.apiPaths.LinkEndpoints;
import com.linkvault.dto.LinkDto;
import com.linkvault.service.LinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.linkvault.util.LogUtils.*;

@Slf4j
@RestController
@RequestMapping(LinkEndpoints.BASE_LINKS)
public class LinkController {
    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping(LinkEndpoints.BY_USER)
    public List<LinkDto> getAllLinksForUser(@PathVariable Long userId) {
        info(log, "Getting all links for user ID: {}", userId);
        return linkService.getAllLinksForUser(userId);
    }

    @GetMapping(LinkEndpoints.BY_LINK_ID)
    public ResponseEntity<LinkDto> getLinkById(@PathVariable Long linkId) {
        info(log, "Getting link by ID: {}", linkId);
        return linkService.getLinkById(linkId)
            .map(ResponseEntity::ok)
            .orElseGet(()-> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LinkDto> createLink(@RequestBody LinkDto linkDto) {
        info(log, "Creating link for user ID: {}", linkDto.userId());
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
            warn(log, "Failed to update link by ID: {}", linkId);
            return ResponseEntity.badRequest().build();
        }
        info(log, "Updating link by ID: {}", linkId);
        return linkService.updateLink(linkId, linkDto)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping(LinkEndpoints.BY_LINK_ID)
    public ResponseEntity<Void> deleteLink(@PathVariable Long linkId) {
        info(log, "Deleting link by ID: {}", linkId);
        linkService.deleteLink(linkId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(LinkEndpoints.BY_USER)
    public ResponseEntity<Void> deleteAllLinksByUser(@PathVariable Long userId) {
        info(log, "Deleting links by user ID: {}", userId);
        linkService.deleteAllLinksByUser(userId);
        return ResponseEntity.noContent().build();
    }
}
