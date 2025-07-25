package com.linkvault.service;

import com.linkvault.exception.RegistrationFailedException;
import com.linkvault.exception.UsernameAlreadyExistsException;
import com.linkvault.exception.WeakPasswordException;
import com.linkvault.model.Role;
import com.linkvault.model.User;
import com.linkvault.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.linkvault.util.LogUtils.info;


@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(String username, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }

        if (rawPassword.length() < 8) {
            throw new WeakPasswordException(username);
        }

        try {
            info(log, "Creating user for: {}", username);
            User user = new User(username, passwordEncoder.encode((rawPassword)));
            user.setRole(Role.USER);
            User savedUser = userRepository.save(user);
            info(log, "User saved successfully: ID {}", savedUser.getId());
        } catch (Exception e) {
            throw new RegistrationFailedException(username, e);
        }
    }
}
