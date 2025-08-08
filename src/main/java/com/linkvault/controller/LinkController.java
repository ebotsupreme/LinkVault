package com.linkvault.controller;

import com.linkvault.constants.apiPaths.LinkEndpoints;
import com.linkvault.dto.LinkRequest;
import com.linkvault.dto.LinkResponse;
import com.linkvault.service.LinkService;
import com.linkvault.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    private final UserService userService;

    public LinkController(LinkService linkService, UserService userService) {
        this.linkService = linkService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<LinkResponse>> getAllLinksForUser(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = getCurrentUserId(userDetails);

        info(log, "Getting all links for user ID: {}", userId);
        return ResponseEntity.ok(linkService.getAllLinksForUser(userId));
    }

    @GetMapping(LinkEndpoints.BY_LINK_ID)
    public ResponseEntity<LinkResponse> getLinkById(
        @PathVariable @Min(1) Long linkId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = getCurrentUserId(userDetails);

        info(log, "Getting link by ID: {}", linkId);
        return ResponseEntity.ok(linkService.getLinkById(linkId, userId));
    }

    @PostMapping
    public ResponseEntity<LinkResponse> createLink(
        @Valid @RequestBody LinkRequest linkRequest,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = getCurrentUserId(userDetails);

        info(log, "Creating link for user ID: {}", userId);
        LinkResponse response = linkService.createLink(userId, linkRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(LinkEndpoints.BY_LINK_ID)
    public ResponseEntity<LinkResponse> updateLink(
        @PathVariable @Min(1) Long linkId,
        @Valid @RequestBody LinkRequest linkRequest,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = getCurrentUserId(userDetails);

        info(log, "Updating link by ID: {}", linkId);
        return ResponseEntity.ok(linkService.updateLink(linkId, linkRequest, userId));
    }

    @DeleteMapping(LinkEndpoints.BY_LINK_ID)
    public ResponseEntity<Void> deleteLink(
        @PathVariable @Min(1) Long linkId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = getCurrentUserId(userDetails);

        info(log, "Deleting link by ID: {}", linkId);

        linkService.deleteLink(linkId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllLinksByUser(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = getCurrentUserId(userDetails);

        info(log, "Deleting links by user ID: {}", userId);
        linkService.deleteAllLinksByUser(userId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        String username = userDetails.getUsername();
        return userService.getUserIdByUsername(username);
    }
}
