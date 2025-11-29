package com.example.evoting.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.evoting.repository.ReportingRepository;

@Service
public class ReportingService {

    private final ReportingRepository repository;

    public ReportingService(ReportingRepository repository) {
        this.repository = repository;
    }

    public List<Map<String, Object>> loadVoters() {
        return repository.findAllVoters();
    }

    public List<Map<String, Object>> loadCandidates(long electionId) {
        return repository.findCandidatesForElection(electionId);
    }

    public List<Map<String, Object>> loadVotesPerCandidate(long electionId) {
        return repository.findVotesPerCandidate(electionId);
    }

    public List<Map<String, Object>> loadConstituencyResults(long electionId, long constituencyId) {
        return repository.findConstituencyResults(electionId, constituencyId);
    }

    public List<Map<String, Object>> loadElections() {
        return repository.findAllElections();
    }

    public boolean hasVoterVoted(long electionId, long voterId) {
        return repository.hasVoterCastBallot(electionId, voterId);
    }

    public Map<String, Object> findVoteForVoter(long electionId, long voterId) {
        return repository.findVoteByVoterAndElection(electionId, voterId);
    }

    /**
     * Cast a vote for a candidate by a voter in the specified election.
     * This method checks for double-voting and throws IllegalStateException if the
     * voter already voted.
     */
    @org.springframework.transaction.annotation.Transactional
    public void castVote(long electionId, long voterId, long candidateId) {
        boolean already = repository.hasVoterCastBallot(electionId, voterId);
        if (already) {
            throw new IllegalStateException("Voter has already cast a ballot in this election");
        }
        int rows = repository.insertVote(electionId, voterId, candidateId);
        if (rows != 1) {
            throw new IllegalStateException("Failed to insert vote");
        }
    }

    public Map<String, Object> findVoterByNid(String nid) {
        return repository.findVoterByNid(nid);
    }

    public Map<String, Object> findVoterById(long voterId) {
        return repository.findVoterById(voterId);
    }

    public Map<String, Object> findVoterByIdString(String idStr) {
        return repository.findVoterByIdString(idStr);
    }

    public void addCandidate(String fullName, String partyName, long constituencyId, long electionId, byte[] symbol,
            long voterId) {
        int rows = repository.insertCandidate(fullName, partyName, constituencyId, electionId, symbol, voterId);
        if (rows != 1)
            throw new IllegalStateException("Failed to insert candidate");
    }

    public byte[] getCandidateSymbol(long candidateId) {
        return repository.getCandidateSymbol(candidateId);
    }

    public void deleteCandidate(long candidateId) {
        repository.deleteVotesByCandidateId(candidateId);
        repository.deleteCandidate(candidateId);
    }

    public List<Map<String, Object>> getAllCandidates() {
        return repository.findAllCandidates();
    }

    public void addElection(String electionName, String startDate, String endDate) {
        String status = "upcoming";
        try {
            java.time.LocalDate start = java.time.LocalDate.parse(startDate);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate);
            java.time.LocalDate now = java.time.LocalDate.now();

            if (now.isAfter(end)) {
                status = "completed";
            } else if (now.isEqual(start) || (now.isAfter(start) && now.isBefore(end)) || now.isEqual(end)) {
                status = "active";
            } else {
                status = "upcoming";
            }
        } catch (Exception e) {
            // Fallback status if parsing fails
            status = "upcoming";
        }

        int rows = repository.insertElection(electionName, startDate, endDate, status);
        if (rows != 1)
            throw new IllegalStateException("Failed to insert election");
    }

    /**
     * Finds candidates for a specific election and constituency.
     */
    public List<Map<String, Object>> findCandidatesByElectionAndConstituency(long electionId, long constituencyId) {
        List<Map<String, Object>> allCandidates = loadCandidates(electionId);
        return allCandidates.stream()
                .filter(c -> {
                    Object cId = c.get("constituency_id");
                    return cId != null && ((Number) cId).longValue() == constituencyId;
                })
                .toList();
    }

    /**
     * Authenticates a voter by NID and Password, then returns their details.
     * Also filters candidates based on the voter's constituency.
     */
    public Map<String, Object> loginVoter(String nid, String password, long electionId) {
        Map<String, Object> voter = repository.findVoterByNid(nid);
        if (voter == null) {
            throw new IllegalArgumentException("Voter with NID " + nid + " not found.");
        }

        // Simple password check (in production, use BCrypt)
        String storedPassword = (String) voter.get("password");
        if (storedPassword != null && !storedPassword.equals(password)) {
            throw new IllegalArgumentException("Invalid password.");
        }

        long voterId = ((Number) voter.get("voter_id")).longValue();
        long constituencyId = voter.get("constituency_id") != null ? ((Number) voter.get("constituency_id")).longValue()
                : 0;

        boolean alreadyVoted = hasVoterVoted(electionId, voterId);

        // Create a mutable map or new map to return session data
        // We can just return the voter map and add extra fields, or create a dedicated
        // DTO.
        // For simplicity, we'll return a new map with all necessary info.
        java.util.HashMap<String, Object> sessionData = new java.util.HashMap<>(voter);
        sessionData.put("hasVoted", alreadyVoted);

        if (alreadyVoted) {
            sessionData.put("myVote", findVoteForVoter(electionId, voterId));
        } else {
            sessionData.put("candidates", findCandidatesByElectionAndConstituency(electionId, constituencyId));
        }

        return sessionData;
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteElection(long electionId) {
        repository.deleteVotesByElectionId(electionId);
        repository.deleteCandidatesByElectionId(electionId);
        repository.deleteElection(electionId);
    }

    public List<Map<String, Object>> getVotesByElection(long electionId) {
        return repository.findVotesByElection(electionId);
    }

}
