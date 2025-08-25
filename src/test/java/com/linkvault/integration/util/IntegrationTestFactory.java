package com.linkvault.integration.util;

public class IntegrationTestFactory {

    public static String createJsonForUser(String username, String password) {
        return  String.format("""
            {
                "username": "%s",
                "password": "%s"
            }
            """, username, password);
    }

    public static String createJsonForLink(String url, String title, String description) {
        return  String.format("""
            {
                "url": "%s",
                "title": "%s",
                "description": "%s"
            }
            """, url, title, description);
    }
}
