package com.user_admin.app.config;

import com.user_admin.app.model.User;
import com.user_admin.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;

/**
 * Configuration class that implements UserDetailsService for Spring Security.
 * This service is responsible for loading user-specific data during authentication.
 */
@Configuration
public class UserDetailsServiceConfig implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceConfig.class);
    private final UserRepository userRepository;

    /**
     * Constructor for injecting the UserRepository dependency.
     *
     * @param userRepository the repository to interact with user data
     */
    public UserDetailsServiceConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads user details by email for authentication.
     *
     * @param email the username of the user to be loaded
     * @return UserDetails containing user information
     * @throws UsernameNotFoundException if no user is found with the given email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("Email not found: " + email);
                });

        logger.info("User found: {}", user.getEmail());

        // Returning the user details with an empty authority list
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.emptyList() // Here you can specify user authorities if needed
        );
    }
}
