package com.example.evoting.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.evoting.service.ReportingService;

@Controller
public class ElectionController {

    private final ReportingService reportingService;

    public ElectionController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/elections")
    public String listElections(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        // In a real app, we might filter elections by voter's eligibility
        List<Map<String, Object>> elections = reportingService.loadElections();
        model.addAttribute("elections", elections);
        model.addAttribute("voterName", principal.getName()); // This will be the NID for voters

        return "elections";
    }
}
