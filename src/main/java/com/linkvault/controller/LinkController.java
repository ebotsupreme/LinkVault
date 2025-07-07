package com.linkvault.controller;

import com.linkvault.model.Link;
import com.linkvault.repository.LinkRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/links")
public class LinkController {
    private final LinkRepository linkRepository;

    public LinkController(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    // Get /links
    @GetMapping
    public List<Link> getAllLinks(){
        return linkRepository.findAll();
    }
}
