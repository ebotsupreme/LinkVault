package com.linkvault.service;

import com.linkvault.exception.*;
import com.linkvault.model.Role;
import com.linkvault.model.User;
import com.linkvault.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.linkvault.util.LogUtils.info;


@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(String username, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
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

    public Long getUserIdByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
        return user.getId();
    }
}
