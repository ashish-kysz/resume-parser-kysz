package com.example.ResumePrasing.controller;

import com.example.ResumePrasing.services.ResumeUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
public class ResumeUploadController {

    @Autowired
    private ResumeUploadService resumeUploadService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadResume(@RequestParam("file") MultipartFile[] file) {
        String response = resumeUploadService.uploadResume(file);
        return ResponseEntity.ok(response);
    }
}
