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
                                new MapSqlParameterSource("electionId", electionId));
        }

        public List<Map<String, Object>> findVotesPerCandidate(long electionId) {
                return jdbcTemplate.queryForList(
                                SQL_VOTES_PER_CANDIDATE,
                                new MapSqlParameterSource("electionId", electionId));
        }

        public List<Map<String, Object>> findConstituencyResults(long electionId, long constituencyId) {
                MapSqlParameterSource params = new MapSqlParameterSource()
                                .addValue("electionId", electionId)
                                .addValue("constituencyId", constituencyId);

                return jdbcTemplate.queryForList(SQL_RESULTS_BY_CONSTITUENCY, params);
        }

        /**
         * Find a vote cast by a specific voter in a specific election and return
         * candidate details.
         */
        public static final String SQL_FIND_VOTE_BY_VOTER_ELECTION = """
                        SELECT v.vote_id, v.voter_id, v.candidate_id, v.election_id,
                               c.full_name AS candidate_name, c.party_name AS party_name, c.constituency_id
                        FROM vote v
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
         * SQL to insert a vote. Assumes `vote` table has (voter_id, candidate_id,
         * election_id) columns
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

        public Map<String, Object> findVoterByIdString(String idStr) {
                try {
                        long id = Long.parseLong(idStr);
                        String sql = "SELECT * FROM voter WHERE voter_id = ?";
                        return jdbcTemplate.getJdbcTemplate().queryForMap(sql, id);
                } catch (NumberFormatException | org.springframework.dao.EmptyResultDataAccessException e) {
                        return null;
                }
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

        // Insert voter (for demo seeding) - if your voter table has other NOT NULL
        // constraints adjust accordingly
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

        public static final String SQL_CANDIDATES_FOR_ELECTION = """
                        SELECT candidate_id,
                               full_name,
                               party_name,
                               constituency_id,
                               election_id
                               -- symbol is excluded here to avoid heavy load, fetched via controller
                        FROM candidate
                        WHERE election_id = :electionId
                        ORDER BY candidate_id
                        """;

        // ... (other queries)

        // Insert candidate
        public static final String SQL_INSERT_CANDIDATE = """
                        INSERT INTO candidate (full_name, party_name, constituency_id, election_id, symbol, voter_id)
                        VALUES (:fullName, :partyName, :constituencyId, :electionId, :symbol, :voterId)
                        """;

        public int insertCandidate(String fullName, String partyName, long constituencyId, long electionId,
                        byte[] symbol, long voterId) {
                MapSqlParameterSource params = new MapSqlParameterSource()
                                .addValue("fullName", fullName)
                                .addValue("partyName", partyName)
                                .addValue("constituencyId", constituencyId)
                                .addValue("electionId", electionId)
                                .addValue("symbol", symbol) // JDBC handles byte[] as BLOB
                                .addValue("voterId", voterId);
                return jdbcTemplate.update(SQL_INSERT_CANDIDATE, params);
        }

        public byte[] getCandidateSymbol(long candidateId) {
                String sql = "SELECT symbol FROM candidate WHERE candidate_id = :candidateId";
                MapSqlParameterSource params = new MapSqlParameterSource().addValue("candidateId", candidateId);
                try {
                        return jdbcTemplate.queryForObject(sql, params, byte[].class);
                } catch (Exception e) {
                        return null;
                }
        }

        public static final String SQL_DELETE_CANDIDATE = "DELETE FROM candidate WHERE candidate_id = :candidateId";
        public static final String SQL_DELETE_VOTES_BY_CANDIDATE = "DELETE FROM vote WHERE candidate_id = :candidateId";

        public int deleteVotesByCandidateId(long candidateId) {
                MapSqlParameterSource params = new MapSqlParameterSource().addValue("candidateId", candidateId);
                return jdbcTemplate.update(SQL_DELETE_VOTES_BY_CANDIDATE, params);
        }

        public int deleteCandidate(long candidateId) {
                MapSqlParameterSource params = new MapSqlParameterSource().addValue("candidateId", candidateId);
                return jdbcTemplate.update(SQL_DELETE_CANDIDATE, params);
        }

        // Insert election - assumes a simple 'election' table exists with
        // (election_name)
        // Insert an election record. We set the status to 'upcoming' by default.
        // Insert election
        public static final String SQL_INSERT_ELECTION = """
                        INSERT INTO election (title, start_date, end_date, status)
                        VALUES (:electionName, :startDate, :endDate, :status)
                        """;

        public int insertElection(String electionName, String startDate, String endDate, String status) {
                MapSqlParameterSource params = new MapSqlParameterSource()
                                .addValue("electionName", electionName)
                                .addValue("startDate", startDate)
                                .addValue("endDate", endDate)
                                .addValue("status", status);
                return jdbcTemplate.update(SQL_INSERT_ELECTION, params);
        }

        // List elections
        public static final String SQL_SELECT_ELECTIONS = """
                        SELECT election_id, title, status, start_date, end_date
                        FROM election
                        ORDER BY election_id DESC
                        """;

        public List<Map<String, Object>> findAllElections() {
                return jdbcTemplate.queryForList(SQL_SELECT_ELECTIONS, Map.of());
        }

        public static final String SQL_ALL_CANDIDATES = "SELECT * FROM candidate ORDER BY election_id DESC, constituency_id ASC";

        public List<Map<String, Object>> findAllCandidates() {
                return jdbcTemplate.queryForList(SQL_ALL_CANDIDATES, Map.of());
        }

        public static final String SQL_DELETE_VOTES_BY_ELECTION = "DELETE FROM vote WHERE election_id = :electionId";
        public static final String SQL_DELETE_CANDIDATES_BY_ELECTION = "DELETE FROM candidate WHERE election_id = :electionId";
        public static final String SQL_DELETE_ELECTION = "DELETE FROM election WHERE election_id = :electionId";

        public int deleteVotesByElectionId(long electionId) {
                MapSqlParameterSource params = new MapSqlParameterSource().addValue("electionId", electionId);
                return jdbcTemplate.update(SQL_DELETE_VOTES_BY_ELECTION, params);
        }

        public int deleteCandidatesByElectionId(long electionId) {
                MapSqlParameterSource params = new MapSqlParameterSource().addValue("electionId", electionId);
                return jdbcTemplate.update(SQL_DELETE_CANDIDATES_BY_ELECTION, params);
        }

        public int deleteElection(long electionId) {
                MapSqlParameterSource params = new MapSqlParameterSource().addValue("electionId", electionId);
                return jdbcTemplate.update(SQL_DELETE_ELECTION, params);
        }

        public static final String SQL_VOTES_BY_ELECTION = """
                        SELECT v.vote_id,
                               vr.full_name AS voter_name,
                               vr.nid_number AS voter_nid,
                               c.full_name AS candidate_name,
                               c.party_name
                        FROM vote v
                        JOIN voter vr ON v.voter_id = vr.voter_id
                        JOIN candidate c ON v.candidate_id = c.candidate_id
                        WHERE v.election_id = :electionId
                        ORDER BY vr.full_name
                        """;

        public List<Map<String, Object>> findVotesByElection(long electionId) {
                return jdbcTemplate.queryForList(SQL_VOTES_BY_ELECTION,
                                new MapSqlParameterSource("electionId", electionId));
        }
}
