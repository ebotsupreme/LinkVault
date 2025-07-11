package com.linkvault.config;

import com.linkvault.model.Link;
import com.linkvault.model.User;
import com.linkvault.repository.LinkRepository;
import com.linkvault.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {
    @Bean
    CommandLineRunner init(LinkRepository linkRepository, UserRepository userRepository) {
        return args -> {
            User user = new User("eddie", "password123");
            userRepository.save(user);

            Link link1 = new Link(
                "https://github.com", "Git Hub", "Repositories", user);
            Link link2 = new Link(
                "https://spring.io", "Spring Boot", "Learning Spring Boot", user);

            linkRepository.save(link1);
            linkRepository.save(link2);
        };
    }
}
