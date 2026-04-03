package com.example.evoting.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Handles initial database schema setup and manual migrations for the E-Voting system.
 * This class ensures that all necessary columns and indexes exist, regardless of the target database.
 */
@Component
public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final JdbcTemplate jdbc;

    public DatabaseInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("DatabaseInitializer: Starting schema verification and manual migrations...");

        // 1. Voter table migrations
        try {
            // Add password column to voter if missing (H2 specific check, but safe for others if schema matches)
            String checkVoterPassSql = "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                                     "WHERE TABLE_NAME = 'VOTER' AND COLUMN_NAME = 'PASSWORD'";
            Integer cnt = jdbc.queryForObject(checkVoterPassSql, Integer.class);
            if (cnt == null || cnt == 0) {
                logger.info("Migration: Adding 'password' column to VOTER table");
                jdbc.execute("ALTER TABLE voter ADD COLUMN password VARCHAR(255)");
            }
        } catch (Exception e) {
            logger.debug("VOTER table check/migration skipped: {}", e.getMessage());
        }

        // 2. Admin table migrations
        try {
            String checkAdminPassSql = "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                                      "WHERE TABLE_NAME = 'ADMIN' AND COLUMN_NAME = 'PASSWORD'";
            Integer cnt = jdbc.queryForObject(checkAdminPassSql, Integer.class);
            if (cnt == null || cnt == 0) {
                logger.info("Migration: Adding 'password' column to ADMIN table");
                jdbc.execute("ALTER TABLE admin ADD COLUMN password VARCHAR(255)");
            }
        } catch (Exception e) {
            logger.debug("ADMIN table check/migration skipped: {}", e.getMessage());
        }

        // 3. Candidate table migrations
        try {
            logger.info("Migration: Ensuring CANDIDATE.SYMBOL is LONGBLOB and providing VOTER_ID link");
            jdbc.execute("ALTER TABLE candidate MODIFY COLUMN symbol LONGBLOB");
            try {
                jdbc.execute("ALTER TABLE candidate ADD COLUMN voter_id BIGINT");
            } catch (Exception ex) {
                // Ignore if column already exists
            }
        } catch (Exception e) {
            logger.debug("CANDIDATE table migration skipped: {}", e.getMessage());
        }

        // 4. Vote table - Unique Index
        try {
            logger.info("Migration: Ensuring unique index on VOTE (voter_id, election_id)");
            jdbc.execute("CREATE UNIQUE INDEX IF NOT EXISTS uq_vote_voter_election ON vote (voter_id, election_id)");
        } catch (Exception e) {
            logger.debug("Unique index creation skipped (it may already exist): {}", e.getMessage());
        }

        logger.info("DatabaseInitializer: Schema verification completed.");
    }
}
