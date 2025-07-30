package com.linkvault.service;

public interface UserService {
    void registerUser(String username, String rawPassword);
    Long getUserIdByUsername(String username);
}
