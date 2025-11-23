package com.example.evoting.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class MigrationRunner {
    private static final Logger logger = LoggerFactory.getLogger(MigrationRunner.class);
    private final JdbcTemplate jdbc;

    public MigrationRunner(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(0)
    public void runMigrations() {
        logger.info("MigrationRunner: checking schema and applying safe migrations if needed");

        try {
            // 1) Add password column to voter if missing
            Integer cntVoterPassword = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'voter' AND COLUMN_NAME = 'password'",
                    Integer.class
            );
            if (cntVoterPassword == null || cntVoterPassword == 0) {
                logger.info("Adding password column to voter table");
                jdbc.execute("ALTER TABLE voter ADD COLUMN password VARCHAR(255)");
            } else {
                logger.debug("voter.password column already exists");
            }
        } catch (Exception e) {
            logger.warn("Could not add voter.password column (it may not be supported or table missing): {}", e.getMessage());
        }

        try {
            // 2) Add password column to admin if missing
            Integer cntAdminPassword = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'admin' AND COLUMN_NAME = 'password'",
                    Integer.class
            );
            if (cntAdminPassword == null || cntAdminPassword == 0) {
                logger.info("Adding password column to admin table");
                jdbc.execute("ALTER TABLE admin ADD COLUMN password VARCHAR(255)");
            } else {
                logger.debug("admin.password column already exists");
            }
        } catch (Exception e) {
            logger.warn("Could not add admin.password column (it may not be supported or table missing): {}", e.getMessage());
        }

        try {
            // 3) Create unique index on vote(voter_id, election_id) if not exists
            // Attempting to create the index; if it exists this will throw and we ignore.
            logger.info("Ensuring unique index uq_vote_voter_election on vote(voter_id,election_id)");
            try {
                jdbc.execute("CREATE UNIQUE INDEX uq_vote_voter_election ON vote (voter_id, election_id)");
                logger.info("Created unique index uq_vote_voter_election");
            } catch (Exception ex) {
                logger.debug("Could not create unique index (it may already exist): {}", ex.getMessage());
            }
        } catch (Exception e) {
            logger.warn("Could not ensure unique index on vote table: {}", e.getMessage());
        }

        logger.info("MigrationRunner: finished schema checks");
    }
}
