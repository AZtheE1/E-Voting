package com.example.evoting.repository;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReportingRepository {

    /**
     * SQL-1: Retrieve all voters.
     */
    public static final String SQL_ALL_VOTERS = """
             SELECT voter_id,
                     full_name,
                     nid_number,
                     date_of_birth,
                     gender
             FROM voter
             ORDER BY full_name
            """;

    /**
     * SQL-2: Retrieve all candidates for a specific election.
     */
    public static final String SQL_CANDIDATES_FOR_ELECTION = """
            SELECT candidate_id,
                   full_name,
                   party_name,
                   constituency_id,
                   election_id
            FROM candidate
            WHERE election_id = :electionId
            ORDER BY full_name
            """;

    /**
     * SQL-3: Count votes for each candidate in an election.
     */
    public static final String SQL_VOTES_PER_CANDIDATE = """
             SELECT c.candidate_id,
                     c.full_name AS candidate_name,
                     c.party_name AS party_name,
                     COUNT(v.vote_id) AS total_votes
             FROM vote v
             JOIN candidate c ON v.candidate_id = c.candidate_id
             WHERE v.election_id = :electionId
             GROUP BY c.candidate_id, c.full_name, c.party_name
             ORDER BY total_votes DESC
             """;

    /**
     * SQL-4: Show results by constituency for a given election.
     */
    public static final String SQL_RESULTS_BY_CONSTITUENCY = """
            SELECT c.full_name AS candidate_name,
                   COUNT(v.vote_id) AS total_votes
            FROM vote v
            JOIN candidate c
              ON v.candidate_id = c.candidate_id
            WHERE c.constituency_id = :constituencyId
              AND v.election_id = :electionId
            GROUP BY c.candidate_id, c.full_name
            ORDER BY total_votes DESC
            """;

    /**
     * SQL-5: Check if a specific voter has voted in an election.
     */
    public static final String SQL_HAS_VOTED = """
            SELECT COUNT(*) AS total
            FROM vote
            WHERE voter_id = :voterId
              AND election_id = :electionId
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ReportingRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> findAllVoters() {
        return jdbcTemplate.queryForList(SQL_ALL_VOTERS, Map.of());
    }

    public List<Map<String, Object>> findCandidatesForElection(long electionId) {
        return jdbcTemplate.queryForList(
                SQL_CANDIDATES_FOR_ELECTION,
                new MapSqlParameterSource("electionId", electionId)
        );
    }

    public List<Map<String, Object>> findVotesPerCandidate(long electionId) {
        return jdbcTemplate.queryForList(
                SQL_VOTES_PER_CANDIDATE,
                new MapSqlParameterSource("electionId", electionId)
        );
    }

    public List<Map<String, Object>> findConstituencyResults(long electionId, long constituencyId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("electionId", electionId)
                .addValue("constituencyId", constituencyId);

        return jdbcTemplate.queryForList(SQL_RESULTS_BY_CONSTITUENCY, params);
    }

    /**
     * Find a vote cast by a specific voter in a specific election and return candidate details.
     */
    public static final String SQL_FIND_VOTE_BY_VOTER_ELECTION = """
            SELECT v.vote_id, v.voter_id, v.candidate_id, v.election_id,
                   c.full_name AS candidate_name, c.party_name AS party_name, c.constituency_id
            FROM vote v
            JOIN candidate c ON v.candidate_id = c.candidate_id
            WHERE v.election_id = :electionId
              AND v.voter_id = :voterId
            LIMIT 1
            """;

    public Map<String, Object> findVoteByVoterAndElection(long electionId, long voterId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("electionId", electionId)
                .addValue("voterId", voterId);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_FIND_VOTE_BY_VOTER_ELECTION, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public boolean hasVoterCastBallot(long electionId, long voterId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("electionId", electionId)
                .addValue("voterId", voterId);

        Integer total = jdbcTemplate.queryForObject(SQL_HAS_VOTED, params, Integer.class);
        return total != null && total > 0;
    }

        /**
         * SQL to insert a vote. Assumes `vote` table has (voter_id, candidate_id, election_id) columns
         * and vote_id is auto-generated.
         */
        public static final String SQL_INSERT_VOTE = """
                        INSERT INTO vote (voter_id, candidate_id, election_id)
                        VALUES (:voterId, :candidateId, :electionId)
                        """;

        public int insertVote(long electionId, long voterId, long candidateId) {
                MapSqlParameterSource params = new MapSqlParameterSource()
                                .addValue("electionId", electionId)
                                .addValue("voterId", voterId)
                                .addValue("candidateId", candidateId);

                return jdbcTemplate.update(SQL_INSERT_VOTE, params);
        }

                // Find a voter by national id (nid)
                        public static final String SQL_FIND_VOTER_BY_NID = """
                                        SELECT voter_id, full_name, nid_number, date_of_birth, gender
                                        FROM voter
                                        WHERE nid_number = :nidNumber
                                        LIMIT 1
                                        """;

                        public Map<String, Object> findVoterByNid(String nid) {
                                MapSqlParameterSource params = new MapSqlParameterSource().addValue("nidNumber", nid);
                                List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_FIND_VOTER_BY_NID, params);
                                return rows.isEmpty() ? null : rows.get(0);
                        }

                        // Find a voter by voter_id
                        public static final String SQL_FIND_VOTER_BY_ID = """
                                        SELECT voter_id, full_name, nid_number, date_of_birth, gender
                                        FROM voter
                                        WHERE voter_id = :voterId
                                        LIMIT 1
                                        """;

                        public Map<String, Object> findVoterById(long voterId) {
                                MapSqlParameterSource params = new MapSqlParameterSource().addValue("voterId", voterId);
                                List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_FIND_VOTER_BY_ID, params);
                                return rows.isEmpty() ? null : rows.get(0);
                        }

                            // Admin queries
                            public static final String SQL_FIND_ADMIN_BY_USERNAME = """
                                    SELECT admin_id, username, password
                                    FROM admin
                                    WHERE username = :username
                                    LIMIT 1
                                    """;

                            public Map<String, Object> findAdminByUsername(String username) {
                                MapSqlParameterSource params = new MapSqlParameterSource().addValue("username", username);
                                List<Map<String, Object>> rows = jdbcTemplate.queryForList(SQL_FIND_ADMIN_BY_USERNAME, params);
                                return rows.isEmpty() ? null : rows.get(0);
                            }

                            public static final String SQL_INSERT_ADMIN = """
                                    INSERT INTO admin (username, password)
                                    VALUES (:username, :password)
                                    """;

                            public int insertAdmin(String username, String passwordHash) {
                                MapSqlParameterSource params = new MapSqlParameterSource()
                                        .addValue("username", username)
                                        .addValue("password", passwordHash);
                                return jdbcTemplate.update(SQL_INSERT_ADMIN, params);
                            }

                            // Insert voter (for demo seeding) - if your voter table has other NOT NULL constraints adjust accordingly
                            public static final String SQL_INSERT_VOTER = """
                                    INSERT INTO voter (full_name, nid_number, date_of_birth, gender, password)
                                    VALUES (:fullName, :nidNumber, :dob, :gender, :password)
                                    """;

                            public int insertVoter(String fullName, String nidNumber, String dob, String gender, String passwordHash) {
                                MapSqlParameterSource params = new MapSqlParameterSource()
                                        .addValue("fullName", fullName)
                                        .addValue("nidNumber", nidNumber)
                                        .addValue("dob", dob)
                                        .addValue("gender", gender)
                                        .addValue("password", passwordHash);
                                return jdbcTemplate.update(SQL_INSERT_VOTER, params);
                            }

                // Insert candidate
                public static final String SQL_INSERT_CANDIDATE = """
                                INSERT INTO candidate (full_name, party_name, constituency_id, election_id)
                                VALUES (:fullName, :partyName, :constituencyId, :electionId)
                                """;

                public int insertCandidate(String fullName, String partyName, long constituencyId, long electionId) {
                        MapSqlParameterSource params = new MapSqlParameterSource()
                                        .addValue("fullName", fullName)
                                        .addValue("partyName", partyName)
                                        .addValue("constituencyId", constituencyId)
                                        .addValue("electionId", electionId);
                        return jdbcTemplate.update(SQL_INSERT_CANDIDATE, params);
                }

                // Insert election - assumes a simple 'election' table exists with (election_name)
                    // Insert an election record. We set the status to 'upcoming' by default.
                    public static final String SQL_INSERT_ELECTION = """
                            INSERT INTO election (title, status)
                            VALUES (:electionName, :status)
                            """;

                    public int insertElection(String electionName) {
                        MapSqlParameterSource params = new MapSqlParameterSource()
                                .addValue("electionName", electionName)
                                .addValue("status", "upcoming");
                        return jdbcTemplate.update(SQL_INSERT_ELECTION, params);
                    }

                        // List elections
                        public static final String SQL_SELECT_ELECTIONS = """
                                        SELECT election_id, title, status
                                        FROM election
                                        ORDER BY election_id DESC
                                        """;

                        public List<Map<String, Object>> findAllElections() {
                                return jdbcTemplate.queryForList(SQL_SELECT_ELECTIONS, Map.of());
                        }
}
