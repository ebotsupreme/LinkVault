package com.linkvault.controller;

import com.linkvault.dto.LinkDto;
import com.linkvault.model.Link;
import com.linkvault.repository.LinkRepository;
import com.linkvault.service.LinkServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/links")
public class LinkController {
    private final LinkServiceImpl linkServiceImpl;

    public LinkController(LinkServiceImpl linkServiceImpl) {
        this.linkServiceImpl = linkServiceImpl;
    }

    // Get /links
    @GetMapping("/user/{userId}")
    public List<LinkDto> getAllLinksForUser(@PathVariable Long userId) {
        return linkServiceImpl.getAllLinksForUser(userId);
    }
}
