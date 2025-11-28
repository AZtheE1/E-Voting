package com.example.evoting.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.evoting.service.ReportingService;

@Controller
public class VoteController {

    private static final Logger logger = LoggerFactory.getLogger(VoteController.class);
    private final ReportingService reportingService;

    public VoteController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/vote/{electionId}")
    public String showBallot(@PathVariable long electionId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        Map<String, Object> voter = reportingService.findVoterByNid(username);

        if (voter == null) {
            // Try looking up by Voter ID
            voter = reportingService.findVoterByIdString(username);
        }

        if (voter == null) {
            return "redirect:/login?error=voter_not_found";
        }

        long voterId = ((Number) voter.get("voter_id")).longValue();
        long constituencyId = voter.get("constituency_id") != null ? ((Number) voter.get("constituency_id")).longValue()
                : 0;

        // Check if already voted
        if (reportingService.hasVoterVoted(electionId, voterId)) {
            return "redirect:/results/" + electionId;
        }

        // Load candidates for voter's constituency
        List<Map<String, Object>> candidates = reportingService.findCandidatesByElectionAndConstituency(electionId,
                constituencyId);

        model.addAttribute("electionId", electionId);
        model.addAttribute("candidates", candidates);
        model.addAttribute("voterName", voter.get("full_name"));
        // In a real app, fetch constituency name from DB
        model.addAttribute("constituencyName", "Constituency " + constituencyId);
        model.addAttribute("voterId", voterId);

        return "vote";
    }

    @PostMapping("/vote")
    public String castVote(
            @RequestParam long electionId,
            @RequestParam long voterId,
            @RequestParam long candidateId) {
        try {
            if (reportingService.hasVoterVoted(electionId, voterId)) {
                return "redirect:/results/" + electionId;
            }
            reportingService.castVote(electionId, voterId, candidateId);
            return "redirect:/results/" + electionId;
        } catch (Exception e) {
            logger.error("Error casting vote", e);
            return "redirect:/vote/" + electionId + "?error=vote_failed";
        }
    }
}
