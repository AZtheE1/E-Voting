package com.example.evoting.controller;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.evoting.service.ReportingService;

@Controller
public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    private final ReportingService reportingService;

    public DashboardController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/vote")
    public String votePage(
            @RequestParam(defaultValue = "1") long electionId,
            @RequestParam(required = false) String voterIdInput,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String error,
            Model model) {
        model.addAttribute("electionId", electionId);

        if (voterIdInput != null && !voterIdInput.isBlank()) {
            handleVoterLogin(electionId, voterIdInput, password, model);
        } else {
            model.addAttribute("showVoterInput", true);
            // Load all candidates initially if no voter is logged in, or just empty
            try {
                List<Map<String, Object>> candidates = reportingService.loadCandidates(electionId);
                model.addAttribute("candidates", candidates);
            } catch (Exception e) {
                logger.error("Error loading candidates", e);
            }
        }

        if (error != null) {
            model.addAttribute("errorMessage", decodeError(error));
        }

        return "vote";
    }

    private void handleVoterLogin(long electionId, String voterIdInput, String password, Model model) {
        try {
            Map<String, Object> sessionData = reportingService.loginVoter(voterIdInput, password, electionId);

            model.addAttribute("sessionVoterId", sessionData.get("voter_id"));
            model.addAttribute("sessionVoterName", sessionData.get("full_name"));
            model.addAttribute("sessionVoterNid", sessionData.get("nid_number"));
            model.addAttribute("sessionVoterGender", sessionData.get("gender"));
            model.addAttribute("hasVoted", sessionData.get("hasVoted"));

            if ((boolean) sessionData.get("hasVoted")) {
                model.addAttribute("myVote", sessionData.get("myVote"));
            } else {
                List<?> candidates = (List<?>) sessionData.get("candidates");
                if (candidates == null || candidates.isEmpty()) {
                    model.addAttribute("errorMessage", "No candidates found for your constituency.");
                }
                model.addAttribute("candidates", candidates);
            }

        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("showVoterInput", true);
        } catch (Exception e) {
            logger.error("Error processing voter login", e);
            model.addAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            model.addAttribute("showVoterInput", true);
        }
    }

    @PostMapping("/vote")
    public String castVote(
            @RequestParam long electionId,
            @RequestParam long voterId,
            @RequestParam long candidateId) {
        try {
            if (reportingService.hasVoterVoted(electionId, voterId)) {
                return "redirect:/results?electionId=" + electionId;
            }
            reportingService.castVote(electionId, voterId, candidateId);
            return "redirect:/vote?electionId=" + electionId + "&voterIdInput="
                    + reportingService.findVoterById(voterId).get("nid_number");
        } catch (Exception e) {
            logger.error("Error casting vote", e);
            return "redirect:/vote?electionId=" + electionId + "&error=vote_failed";
        }
    }

    @GetMapping("/results")
    public String resultsPage(
            @RequestParam(defaultValue = "1") long electionId,
            @RequestParam(defaultValue = "1") long constituencyId,
            Model model) {
        try {
            model.addAttribute("votesPerCandidate", reportingService.loadVotesPerCandidate(electionId));
            model.addAttribute("constituencyResults",
                    reportingService.loadConstituencyResults(electionId, constituencyId));
        } catch (Exception e) {
            logger.error("Error loading results", e);
            model.addAttribute("errorMessage", "Unable to load results. Please try again later.");
        }
        model.addAttribute("electionId", electionId);
        model.addAttribute("constituencyId", constituencyId);
        return "results";
    }

    @GetMapping("/admin")
    public String adminPage(Model model) {
        try {
            model.addAttribute("elections", reportingService.loadElections());
        } catch (Exception e) {
            logger.error("Error loading elections", e);
            model.addAttribute("errorMessage", "Could not load elections.");
        }
        model.addAttribute("reportingService", reportingService);
        return "admin";
    }

    @PostMapping("/admin/addElection")
    public String addElection(@RequestParam String title) {
        try {
            reportingService.addElection(title);
        } catch (Exception e) {
            logger.error("Error creating election", e);
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/addCandidate")
    public String addCandidate(
            @RequestParam String fullName,
            @RequestParam String partyName,
            @RequestParam long constituencyId,
            @RequestParam long electionId) {
        try {
            reportingService.addCandidate(fullName, partyName, constituencyId, electionId);
        } catch (Exception e) {
            logger.error("Error adding candidate", e);
        }
        return "redirect:/admin";
    }

    private String decodeError(String error) {
        if ("vote_failed".equals(error)) {
            return "Failed to cast vote. Please try again.";
        }
        return "An unknown error occurred.";
    }
}
