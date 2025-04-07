package com.example.ResumePrasing.services;


import com.example.ResumePrasing.model.Resume;
import com.example.ResumePrasing.repository.ResumeUploadRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class ResumeUploadService {

    private final ResumeUploadRepository resumeUploadRepository;

    public ResumeUploadService(ResumeUploadRepository resumeUploadRepository) {
        this.resumeUploadRepository = resumeUploadRepository;
    }

    public String uploadResume(MultipartFile[] files) {
        StringBuilder result = new StringBuilder();

        for (MultipartFile file: files) {
            try {
                Resume resume = Resume.builder()
                        .fileName(file.getOriginalFilename())
                        .fileType(file.getContentType())
                        .data(file.getBytes())
                        .build();

                resumeUploadRepository.save(resume);
                result.append("Uploaded: ").append(file.getOriginalFilename()).append("\n");
            } catch (Exception e) {
                e.printStackTrace();
                result.append("Failed to upload: ").append(file.getOriginalFilename()).append(" - ").append(e.getMessage()).append("\n");
            }
        }
        return result.toString();
    }
}
