package com.linkvault.exception;

public class ExceptionMessages {
    public static final String USER_NOT_FOUND = "User with ID %d not found.";
    public static final String LINK_NOT_FOUND = "Link with ID %d not found.";
    public static final String LINKS_NOT_FOUND = "Links not found for user ID: %d.";

    public static final String LINK_SAVE_FAILED = "Failed to save link for user ID: %d. URL: %s";
    public static final String LINK_DELETE_FAILED = "Failed to delete link ID: %d for user ID: %d";
    public static final String LINKS_DELETE_FAILED = "Failed to delete all links for user ID: %d";

    public static final String DATABASE_FAILURE = "Simulated database failure";

    public static final String USER_NOT_AUTHORIZED_TO_UPDATE =
        "User not authorized to update this link with user ID: %d";
    public static final String USER_NOT_AUTHORIZED_TO_DELETE =
        "User not authorized to delete this link with user ID: %d";

    public static final String INVALID_FIELDS = "One or more fields are invalid";

    public static final String METHOD_URI_MESSAGE_FORMAT = "{} {} - {}";
    public static final String FAILED_TO_CREATE_USER_FORMAT = "Failed to create user: {} {} - {}";
}
