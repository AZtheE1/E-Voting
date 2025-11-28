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

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ReportingService reportingService;

    public AdminController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping
    public String adminDashboard(Model model) {
        List<Map<String, Object>> elections = reportingService.loadElections();
        List<Map<String, Object>> candidates = reportingService.getAllCandidates();
        model.addAttribute("elections", elections);
        model.addAttribute("candidates", candidates);
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

    @PostMapping("/addCandidate")
    public String addCandidate(
            @RequestParam String fullName,
            @RequestParam String partyName,
            @RequestParam long constituencyId,
            @RequestParam long electionId,
            @RequestParam("symbolImage") org.springframework.web.multipart.MultipartFile symbolImage) {
        try {
            String symbolPath = null;
            if (symbolImage != null && !symbolImage.isEmpty()) {
                // Save image
                String fileName = java.util.UUID.randomUUID().toString() + "_" + symbolImage.getOriginalFilename();
                java.nio.file.Path uploadPath = java.nio.file.Paths.get("src/main/resources/static/images/symbols/");
                if (!java.nio.file.Files.exists(uploadPath)) {
                    java.nio.file.Files.createDirectories(uploadPath);
                }
                java.nio.file.Path filePath = uploadPath.resolve(fileName);
                java.nio.file.Files.copy(symbolImage.getInputStream(), filePath,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                symbolPath = "/images/symbols/" + fileName;
            }

            reportingService.addCandidate(fullName, partyName, constituencyId, electionId, symbolPath);
        } catch (Exception e) {
            e.printStackTrace(); // Log error
        }
        return "redirect:/admin";
    }

    @PostMapping("/deleteCandidate")
    public String deleteCandidate(@RequestParam long candidateId) {
        try {
            reportingService.deleteCandidate(candidateId);
        } catch (Exception e) {
            e.printStackTrace();
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
