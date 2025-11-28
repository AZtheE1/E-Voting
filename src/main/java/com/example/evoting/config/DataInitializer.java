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
    public void seed() {
        try {
            // Admin demo user
            String adminUser = "9999999999";
            String adminPwd = "adminPass";
            Map<String, Object> existingAdmin = repository.findAdminByUsername(adminUser);
            if (existingAdmin == null) {
                String hash = passwordEncoder.encode(adminPwd);
                try {
                    java.util.List<String> cols = jdbc.queryForList(
                            "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = 'ADMIN'",
                            String.class).stream().map(String::toLowerCase).collect(Collectors.toList());

                    java.util.List<String> insertCols = new java.util.ArrayList<>();
                    java.util.List<Object> values = new java.util.ArrayList<>();

                    if (cols.contains("username")) {
                        insertCols.add("username");
                        values.add(adminUser);
                    }
                    if (cols.contains("password")) {
                        insertCols.add("password");
                        values.add(hash);
                    }
                    if (cols.contains("full_name")) {
                        insertCols.add("full_name");
                        values.add("Administrator");
                    }

                    String colsSql = String.join(", ", insertCols);
                    String placeholders = String.join(", ", java.util.Collections.nCopies(insertCols.size(), "?"));
                    String sql = String.format("INSERT INTO admin (%s) VALUES (%s)", colsSql, placeholders);
                    jdbc.update(sql, values.toArray());
                    logger.info("Inserted demo admin '{}'", adminUser);
                } catch (Exception ex) {
                    logger.warn("Failed to seed admin: {}", ex.getMessage());
                }
            }

            // Seed Constituencies if empty
            Integer constituencyCount = jdbc.queryForObject("SELECT COUNT(*) FROM constituency", Integer.class);
            if (constituencyCount != null && constituencyCount == 0) {
                jdbc.update("INSERT INTO constituency (name) VALUES ('Dhaka-1'), ('Dhaka-2'), ('Chittagong-1')");
                logger.info("Seeded constituencies");
            }

            // Seed Election if empty
            Integer electionCount = jdbc.queryForObject("SELECT COUNT(*) FROM election", Integer.class);
            long electionId = 1;
            if (electionCount != null && electionCount == 0) {
                jdbc.update(
                        "INSERT INTO election (title, start_date, end_date, status) VALUES ('National Election 2025', '2025-12-01', '2025-12-02', 'active')");
                logger.info("Seeded election");
            } else {
                // Get an existing election ID
                try {
                    electionId = jdbc.queryForObject("SELECT election_id FROM election LIMIT 1", Long.class);
                } catch (Exception e) {
                    logger.warn("No election found to link candidates to.");
                }
            }

            // Seed Candidates if empty
            Integer candidateCount = jdbc.queryForObject("SELECT COUNT(*) FROM candidate", Integer.class);
            if (candidateCount != null && candidateCount == 0) {
                jdbc.update(
                        "INSERT INTO candidate (full_name, party_name, constituency_id, election_id, symbol) VALUES " +
                                "('Sheikh Hasina', 'Awami League', 1, ?, '/images/symbols/boat.png'), " +
                                "('Khaleda Zia', 'BNP', 1, ?, '/images/symbols/paddy.png'), " +
                                "('GM Quader', 'Jatiya Party', 1, ?, '/images/symbols/plough.png'), " +
                                "('Dr. Yunus', 'Independent', 2, ?, '/images/symbols/ektara.png'), " +
                                "('Barrister Andaleeve', 'BJP', 2, ?, '/images/symbols/lantern.png'), " +
                                "('Mohiuddin Chowdhury', 'Awami League', 3, ?, '/images/symbols/boat.png')",
                        electionId, electionId, electionId, electionId, electionId, electionId);
                logger.info("Seeded candidates");
            }

            // Seed Voters if empty
            Integer voterCount = jdbc.queryForObject("SELECT COUNT(*) FROM voter", Integer.class);
            if (voterCount != null && voterCount == 0) {
                String commonPass = passwordEncoder.encode("VOTER_SECRET");
                jdbc.update(
                        "INSERT INTO voter (full_name, nid_number, date_of_birth, gender, address, constituency_id, password) VALUES "
                                +
                                "('Abdul Karim', '1998123401', '1985-03-12', 'Male', 'Mirpur, Dhaka', 1, ?), " +
                                "('Farhana Akter', '1998123402', '1997-11-02', 'Female', 'Uttara, Dhaka', 1, ?), " +
                                "('Rafiul Islam', '1998123403', '1990-05-25', 'Male', 'Dhanmondi, Dhaka', 2, ?), " + // Changed
                                                                                                                     // to
                                                                                                                     // Dhaka-2
                                                                                                                     // for
                                                                                                                     // testing
                                "('Jannatul Ferdous', '1998123404', '1998-08-18', 'Female', 'Banani, Dhaka', 2, ?), " +
                                "('Nusrat Jahan', '1998123406', '1994-06-19', 'Female', 'Agrabad, Chittagong', 3, ?)",
                        commonPass, commonPass, commonPass, commonPass, commonPass);
                logger.info("Seeded voters");
            }

            logger.info("Data initialization complete.");

        } catch (Exception e) {
            logger.error("Error during data initialization", e);
        }
    }
}
