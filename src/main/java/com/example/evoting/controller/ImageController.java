package com.example.evoting.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.evoting.service.ReportingService;

@Controller
public class ImageController {

    private final ReportingService reportingService;

    public ImageController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/candidate/image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getCandidateImage(@PathVariable long id) {
        byte[] imageBytes = reportingService.getCandidateSymbol(id);
        if (imageBytes != null && imageBytes.length > 0) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG) // Defaulting to PNG, browsers usually handle others fine
                    .body(imageBytes);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
