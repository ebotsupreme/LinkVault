package com.linkvault.unit.util;

import com.linkvault.constants.apiPaths.LinkEndpoints;
import com.linkvault.dto.LinkResponse;
import com.linkvault.model.Link;
import com.linkvault.model.User;

public class TestDataFactory {

    public static final Long TEST_ID1= 1L;
    public static final Long TEST_ID2 = 2L;
    public static final Long TEST_ID3 = 42L;

    public static User createTestUser() {
        User user = new User("eddie", "password123");
        user.setId(TEST_ID1);
        return user;
    }

    public static Link createLink1() {
        Link link1 = new Link("https://github.com", "Git Hub",
            "Repositories", createTestUser());
        link1.setId(TEST_ID1);
        return link1;
    }

    public static Link createLink2() {
        Link link2 = new Link("https://spring.io", "Spring Boot",
            "Learning Spring Boot", createTestUser());
        link2.setId(TEST_ID2);
        return link2;
    }

    public static String buildLinkEndpointWithId(String path, Long linkDtoId) {
        return LinkEndpoints.BASE_LINKS + LinkEndpoints.BY_LINK_ID
            .replace(path, linkDtoId.toString());
    }

    public static LinkResponse createLinkResponseOne() {
        return new LinkResponse(TEST_ID1, "https://github.com",
            "Git Hub", "Repositories", createTestUser().getId());
    }

    public static LinkResponse createLinkResponseTwo() {
        return new LinkResponse(TEST_ID2, "https://spring.io",
            "Spring Boot", "Learning Spring Boot", createTestUser().getId());
    }
}
