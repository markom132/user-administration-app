package com.user_admin.app.config;

import com.user_admin.app.model.User;
import com.user_admin.app.model.UserStatus;
import com.user_admin.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Database seeder for injecting initial data into the application.
 * This seeder inserts a set of default users if no users exist in the database.
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor for injecting dependencies.
     *
     * @param userRepository  the repository for user data
     * @param passwordEncoder encoder for securely hashing passwords
     */
    public DatabaseSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Runs on application startup to seed initial data if the database is empty.
     *
     * @param args command line arguments
     * @throws Exception if there is an issue during the seeding process
     */
    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User user1 = new User("john.doe@example.com", passwordEncoder.encode("password123"), "John", "Doe", UserStatus.ACTIVE);
            User user2 = new User("jane.smith@example.com", passwordEncoder.encode("password123"), "Jane", "Smith", UserStatus.ACTIVE);
            User user3 = new User("markomarkoviccb@gmail.com", passwordEncoder.encode("password123"), "Jane", "Smith", UserStatus.ACTIVE);

            // Saving initial users to the database
            userRepository.save(user1);
            userRepository.save(user2);
            //userRepository.save(user3);

            logger.info("Initial users have been successfully injected into the database.");
        } else {
            logger.info("Database already contains users. Seeding skipped");
        }
    }
}
