package com.linkvault.controller;

import com.linkvault.dto.LinkDto;
import com.linkvault.service.LinkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/links")
public class LinkController {
    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping("/user/{userId}")
    public List<LinkDto> getAllLinksForUser(@PathVariable Long userId) {
        return linkService.getAllLinksForUser(userId);
    }

    @GetMapping("/{linkId}")
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

}
