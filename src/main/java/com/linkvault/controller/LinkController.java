package com.linkvault.controller;

import com.linkvault.constants.apiPaths.LinkEndpoints;
import com.linkvault.dto.LinkDto;
import com.linkvault.exception.LinkDeleteException;
import com.linkvault.service.LinkService;
import com.linkvault.service.UserServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.linkvault.util.LogUtils.*;

@Validated
@Slf4j
@RestController
@RequestMapping(LinkEndpoints.BASE_LINKS)
public class LinkController {
    private final LinkService linkService;
    private final UserServiceImpl userServiceImpl;

    public LinkController(LinkService linkService, UserServiceImpl userServiceImpl) {
        this.linkService = linkService;
        this.userServiceImpl = userServiceImpl;
    }

    @GetMapping
    public ResponseEntity<List<LinkDto>> getAllLinksForUser(
        @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        Long userId = userServiceImpl.getUserIdByUsername(username);

        info(log, "Getting all links for user ID: {}", userId);
        return ResponseEntity.ok(linkService.getAllLinksForUser(userId));
    }

    @GetMapping(LinkEndpoints.BY_LINK_ID)
    public ResponseEntity<LinkDto> getLinkById(@PathVariable @Min(1) Long linkId) {
        info(log, "Getting link by ID: {}", linkId);
        return linkService.getLinkById(linkId)
            .map(ResponseEntity::ok)
            .orElseGet(()-> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LinkDto> createLink(@Valid @RequestBody LinkDto linkDto) {
        info(log, "Creating link for user ID: {}", linkDto.userId());
        return linkService.createLink(linkDto.userId(), linkDto)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(LinkEndpoints.BY_LINK_ID)
    public ResponseEntity<LinkDto> updateLink(
        @PathVariable @Min(1) Long linkId,
        @Valid @RequestBody LinkDto linkDto
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
    public ResponseEntity<Void> deleteLink(
        @PathVariable @Min(1) Long linkId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        info(log, "Deleting link by ID: {}", linkId);

        String username = userDetails.getUsername();
        Long userId = userServiceImpl.getUserIdByUsername(username);

        linkService.deleteLink(linkId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(LinkEndpoints.BY_USER)
    public ResponseEntity<Void> deleteAllLinksByUser(@PathVariable @Min(1) Long userId) {
        info(log, "Deleting links by user ID: {}", userId);
        linkService.deleteAllLinksByUser(userId);
        return ResponseEntity.noContent().build();
    }
}
