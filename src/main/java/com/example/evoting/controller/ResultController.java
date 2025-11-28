package com.example.evoting.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.evoting.service.ReportingService;

@Controller
public class ResultController {

    private static final Logger logger = LoggerFactory.getLogger(ResultController.class);
    private final ReportingService reportingService;

    public ResultController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/results/{electionId}")
    public String viewResults(@PathVariable long electionId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            model.addAttribute("votesPerCandidate", reportingService.loadVotesPerCandidate(electionId));
            // Assuming constituency results might be needed, but for now showing overall
            // model.addAttribute("constituencyResults",
            // reportingService.loadConstituencyResults(electionId, constituencyId));
        } catch (Exception e) {
            logger.error("Error loading results", e);
            model.addAttribute("errorMessage", "Unable to load results. Please try again later.");
        }

        model.addAttribute("electionId", electionId);
        model.addAttribute("voterName", principal.getName());

        return "results";
    }
}
