package com.example.evoting.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.evoting.service.ReportingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ReportingService reportingService;

    public AdminController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping
    public String adminDashboard(@RequestParam(required = false) Long electionId, Model model) {
        List<Map<String, Object>> elections = reportingService.loadElections();
        model.addAttribute("elections", elections);

        Map<String, Object> selectedElection = null;
        List<Map<String, Object>> candidates = java.util.Collections.emptyList();
        List<Map<String, Object>> votes = java.util.Collections.emptyList();

        if (electionId != null) {
            // Find the selected election from the list (avoiding extra DB call if possible,
            // or just fetch it)
            selectedElection = elections.stream()
                    .filter(e -> ((Number) e.get("election_id")).longValue() == electionId)
                    .findFirst()
                    .orElse(null);

            if (selectedElection != null) {
                candidates = reportingService.loadCandidates(electionId);
                votes = reportingService.getVotesByElection(electionId);
            }
        } else if (!elections.isEmpty()) {
            // Optional: Default to the first election if none selected?
            // Or just show nothing. User said "when a admin click on candidate option...
            // all candidates on that election will shown"
            // So initially maybe nothing is selected.
        }

        model.addAttribute("selectedElection", selectedElection);
        model.addAttribute("candidates", candidates);
        model.addAttribute("votes", votes);

        return "admin";
    }

    @RequestMapping(value = "/saveElection", method = { org.springframework.web.bind.annotation.RequestMethod.GET,
            org.springframework.web.bind.annotation.RequestMethod.POST })
    public String saveElection(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {

        if (title == null || title.trim().isEmpty()) {
            return "redirect:/admin?error=TitleRequired";
        }
        if (startDate == null || startDate.trim().isEmpty() || endDate == null || endDate.trim().isEmpty()) {
            return "redirect:/admin?error=DatesRequired";
        }

        try {
            reportingService.addElection(title, startDate, endDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin";
    }

    @GetMapping("/api/voter-info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getVoterInfo(@RequestParam String nid) {
        Map<String, Object> voter = reportingService.findVoterByNid(nid);
        if (voter == null) {
            // Try by ID
            voter = reportingService.findVoterByIdString(nid);
        }
        if (voter == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(voter);
    }

    @PostMapping("/addCandidate")
    public String addCandidate(
            @RequestParam String fullName,
            @RequestParam String partyName,
            @RequestParam long constituencyId,
            @RequestParam long electionId,
            @RequestParam(required = false) Long voterId,
            @RequestParam("symbolImage") org.springframework.web.multipart.MultipartFile symbolImage) {
        try {
            byte[] symbolBytes = null;
            if (symbolImage != null && !symbolImage.isEmpty()) {
                symbolBytes = symbolImage.getBytes();
            }

            // If voterId is provided, use it. If not (legacy support or error), we might
            // need to handle it.
            // But based on new requirement, we expect voterId.
            long vId = (voterId != null) ? voterId : 0;

            reportingService.addCandidate(fullName, partyName, constituencyId, electionId, symbolBytes, vId);
        } catch (Exception e) {
            e.printStackTrace(); // Log error
        }
        // Redirect back to the specific election view
        return "redirect:/admin?electionId=" + electionId;
    }

    @PostMapping("/deleteCandidate")
    public String deleteCandidate(@RequestParam long candidateId, @RequestParam(required = false) Long electionId) {
        try {
            reportingService.deleteCandidate(candidateId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (electionId != null) {
            return "redirect:/admin?electionId=" + electionId;
        }
        return "redirect:/admin";
    }

    @PostMapping("/deleteElection")
    public String deleteElection(@RequestParam long electionId) {
        try {
            reportingService.deleteElection(electionId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin";
    }
}
