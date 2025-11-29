package com.example.evoting.config;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.evoting.repository.ReportingRepository;

@Component
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final ReportingRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbc;

    public DataInitializer(ReportingRepository repository, PasswordEncoder passwordEncoder, JdbcTemplate jdbc) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jdbc = jdbc;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    public void cleanUpTestData() {
        // Ensure candidate table has correct schema for images and voter link
        try {
            jdbc.execute("ALTER TABLE candidate MODIFY COLUMN symbol LONGBLOB");
        } catch (Exception e) {
            logger.info("Could not modify symbol column (might already be correct): " + e.getMessage());
        }

        try {
            jdbc.execute("ALTER TABLE candidate ADD COLUMN voter_id BIGINT");
        } catch (Exception e) {
            logger.info("Could not add voter_id column (might already exist): " + e.getMessage());
        }

        // No default data insertion as per user request.
        // Only schema updates are performed here.
    }
}
