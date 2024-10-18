package com.user_admin.app.config;

import com.user_admin.app.model.User;
import com.user_admin.app.model.UserStatus;
import com.user_admin.app.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User user1 = new User("john.doe@example.com", passwordEncoder.encode("password123"), "John", "Doe", UserStatus.ACTIVE);
            User user2 = new User("jane.smith@example.com", passwordEncoder.encode("password123"), "Jane", "Smith", UserStatus.ACTIVE);

            userRepository.save(user1);
            userRepository.save(user2);

            System.out.println("Initial Users are injected into database");
        }
    }
}
