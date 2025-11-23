package com.example.evoting.config;

import java.util.Map;

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
    public void seed() {
        // We attempt to seed demo accounts. If the DB schema doesn't include
        // the expected columns (password), the queries will throw SQL errors.
        // In that case we log and skip seeding so the app can continue to run.
        try {
            // Admin demo user (use NID as username per requirements)
            String adminUser = "9999999999"; // demo admin NID
            String adminPwd = "adminPass"; // demo admin password
            Map<String, Object> existingAdmin = repository.findAdminByUsername(adminUser);
            if (existingAdmin == null) {
                String hash = passwordEncoder.encode(adminPwd);
                try {
                    // Inspect admin table columns and insert appropriate columns only
                    java.util.List<String> cols = jdbc.queryForList(
                            "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'admin'",
                            String.class
                    );

                    java.util.List<String> insertCols = new java.util.ArrayList<>();
                    java.util.List<Object> values = new java.util.ArrayList<>();

                    if (cols.contains("username")) {
                        insertCols.add("username");
                        values.add(adminUser);
                    }
                    // include both password-like columns if present
                    if (cols.contains("password")) {
                        insertCols.add("password");
                        values.add(hash);
                    }
                    if (cols.contains("password_hash")) {
                        insertCols.add("password_hash");
                        values.add(hash);
                    }
                    if (cols.contains("full_name")) {
                        insertCols.add("full_name");
                        values.add("Administrator");
                    }

                    if (insertCols.isEmpty()) {
                        throw new IllegalStateException("No suitable columns found in admin table to insert demo admin");
                    }

                    String colsSql = String.join(", ", insertCols);
                    String placeholders = String.join(", ", java.util.Collections.nCopies(insertCols.size(), "?"));
                    String sql = String.format("INSERT INTO admin (%s) VALUES (%s)", colsSql, placeholders);
                    jdbc.update(sql, values.toArray());
                    logger.info("Inserted demo admin '{}' with columns {}", adminUser, insertCols);
                } catch (Exception ex) {
                    throw ex; // let outer catch handle/log
                }
            } else {
                logger.info("Admin '{}' already exists, skipping insert", adminUser);
            }

            // Demo voter
            String demoNid = "1111111111";
            Map<String, Object> v = repository.findVoterByNid(demoNid);
            if (v == null) {
                String voterHash = passwordEncoder.encode("demoPass");
                // Note: date_of_birth and gender are assumed to be string-compatible with your schema
                repository.insertVoter("Demo Voter", demoNid, "1990-01-01", "M", voterHash);
                logger.info("Inserted demo voter with NID {}", demoNid);
            } else {
                logger.info("Demo voter with NID {} already exists", demoNid);
            }

            // Optionally insert a demo election and candidate if not present
            // We try to insert an election named "Demo Election" and a candidate "Demo Candidate".
            try {
                repository.insertElection("Demo Election");
                logger.info("Inserted demo election 'Demo Election'");
            } catch (Exception ex) {
                logger.debug("Could not insert demo election (may already exist or DB restricts duplicates): {}", ex.getMessage());
            }

            try {
                // We don't know the election id inserted above; however admin UI can create candidates manually.
                logger.info("Data initialization complete.");
            } catch (Exception ex) {
                logger.debug("Candidate insert skipped: {}", ex.getMessage());
            }

        } catch (org.springframework.dao.DataAccessException e) {
            // Likely the DB schema isn't migrated to include password columns or other expected fields.
            logger.warn("Skipping automatic data seeding because DB schema does not match expected layout: {}", e.getMessage());
            logger.warn("Please run the SQL migration in src/main/resources/db/migration/V1__add_constraints_and_passwords.sql manually on your database, then restart the app to enable auto-seeding.");
        } catch (Exception e) {
            logger.error("Error during data initialization", e);
        }
    }
}
