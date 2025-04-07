package com.example.ResumePrasing.services;

import com.example.ResumePrasing.controller.OpenAiController;
import com.example.ResumePrasing.dto.JobDTO;
import com.example.ResumePrasing.model.InterviewPlan;
import com.example.ResumePrasing.model.Job;
import com.example.ResumePrasing.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;
    private final OpenAiController openAiController;
    private final ResourceLoader resourceLoader;

    public JobService(JobRepository jobRepository, OpenAiController openAiController, ResourceLoader resourceLoader) {
        this.jobRepository = jobRepository;
        this.openAiController = openAiController;
        this.resourceLoader = resourceLoader;
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Optional<Job> getJobById(Long id) {
        return jobRepository.findById(id);
    }

    @Transactional
    public Job updateJob(JobDTO jobDTO) {
        Job job = jobRepository.findById(jobDTO.getId()).orElseGet(() -> {
            Job newJob = new Job();
            newJob.setCurrentStep(1);
            newJob.setCreatedAt(LocalDateTime.now());
            return newJob;
        });

        updateJobBasedOnStep(job, jobDTO);

        Job savedJob = jobRepository.save(job);
        LOGGER.info("Job ID {} updated successfully at Step {}", savedJob.getId(), savedJob.getCurrentStep());
        return savedJob;
    }

    private void updateJobBasedOnStep(Job job, JobDTO jobDTO) {
        int currentStep = Optional.ofNullable(jobDTO.getCurrentStep()).orElse(1);
        LOGGER.info("Processing Job ID: {}, Current Step: {}", job.getId(), currentStep);

        switch (currentStep) {
            case 1 -> processJobBasics(job, jobDTO);
            case 2 -> processJobResponsibilities(job);
            case 3 -> processJobDescription(job, jobDTO);
            case 4 -> processInterviewPlan(job);
            case 5 -> processJobSubmission(job);
            default -> throw new IllegalArgumentException("Invalid step: " + currentStep);
        }
    }

    private void processJobBasics(Job job, JobDTO jobDTO) {
        job.setTitle(jobDTO.getTitle());
        job.setRole(jobDTO.getRole());
        job.setExperienceRange(jobDTO.getExperienceRange());
        job.setIndustryType(jobDTO.getIndustryType());

        // Convert skill list to a comma-separated string
        String skillString = String.join(", ", jobDTO.getPrimarySkills());

        // Load the prompt template from classpath
        String templateContent;
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("prompts/job_responsibilities.st")) {
            if (is == null) {
                throw new RuntimeException("Template file not found in classpath: prompts/job_responsibilities.st");
            }
            templateContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read the template file", e);
        }

        // Create placeholders for the template
        Map<String, Object> variables = new HashMap<>();
        variables.put("title", jobDTO.getTitle());
        variables.put("role", jobDTO.getRole());
        variables.put("experienceRange", jobDTO.getExperienceRange());
        variables.put("industryType", jobDTO.getIndustryType());
        variables.put("skills", skillString);

        // Render the prompt
        String prompt = templateContent;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            prompt = prompt.replace("<" + entry.getKey() + ">", entry.getValue().toString());
        }

        LOGGER.info("Generated Prompt:\n{}", prompt); // optional for debugging

        // Call OpenAI API
        String response = openAiController.askQuestion(prompt);
        // Clean response
        List<String> responsibilities = Arrays.stream(response.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());

        job.setResponsibilities(responsibilities);
        job.setCurrentStep(2);
        LOGGER.info("Step 1 completed: Job Responsibilities generated for Job ID {}", job.getId());
    }


    private void processJobResponsibilities(Job job) {
        job.setCurrentStep(3);
        LOGGER.info("Step 2 completed: Responsibilities confirmed for Job ID {}", job.getId());
    }

    private void processJobDescription(Job job, JobDTO jobDTO) {
        // Update only fields provided in jobDTO
        job.setLocationType(jobDTO.getLocationType());
        job.setLocation(Optional.ofNullable(jobDTO.getLocation()).orElse(job.getLocation()));
        job.setCompanyName(Optional.ofNullable(jobDTO.getCompanyName()).orElse(job.getCompanyName()));
        job.setSalaryFrom(jobDTO.getSalaryFrom() != null ? jobDTO.getSalaryFrom() : job.getSalaryFrom());
        job.setSalaryTo(jobDTO.getSalaryTo() != null ? jobDTO.getSalaryTo() : job.getSalaryTo());
        job.setHideSalary(jobDTO.getHideSalary());

        // Load the job description template from .st file
        String jobDescriptionTemplate = loadPromptTemplate("prompts/job_description.st");

        // Safely extract all fields from either jobDTO or fallback to existing job entity
        String title = Optional.ofNullable(jobDTO.getTitle()).orElse(job.getTitle() != null ? job.getTitle() : "N/A");
        String role = Optional.ofNullable(jobDTO.getRole()).orElse(job.getRole() != null ? job.getRole() : "N/A");
        String experience = Optional.ofNullable(jobDTO.getExperienceRange()).orElse(job.getExperienceRange() != null ? job.getExperienceRange() : "N/A");
        String industry = Optional.ofNullable(jobDTO.getIndustryType()).orElse(job.getIndustryType() != null ? job.getIndustryType() : "N/A");
        String location = Optional.ofNullable(jobDTO.getLocation()).orElse(job.getLocation() != null ? job.getLocation() : "Remote");
        String company = Optional.ofNullable(jobDTO.getCompanyName()).orElse(job.getCompanyName() != null ? job.getCompanyName() : "Confidential");

        String salaryFrom = jobDTO.getSalaryFrom() != null
                ? jobDTO.getSalaryFrom().toString()
                : (job.getSalaryFrom() != null ? job.getSalaryFrom().toString() : "Not Disclosed");

        String salaryTo = jobDTO.getSalaryTo() != null
                ? jobDTO.getSalaryTo().toString()
                : (job.getSalaryTo() != null ? job.getSalaryTo().toString() : "Not Disclosed");

        List<String> skillsList = jobDTO.getPrimarySkills() != null
                ? jobDTO.getPrimarySkills()
                : (job.getPrimarySkills() != null ? job.getPrimarySkills() : Collections.emptyList());

        String skills = String.join(", ", skillsList);

        // Replace placeholders dynamically
        String jobDescriptionPrompt = jobDescriptionTemplate
                .replace("{title}", title)
                .replace("{role}", role)
                .replace("{experience}", experience)
                .replace("{industry}", industry)
                .replace("{skills}", skills)
                .replace("{location}", location)
                .replace("{company}", company)
                .replace("{salaryFrom}", salaryFrom)
                .replace("{salaryTo}", salaryTo);

        // Call OpenAI API to generate job description
        String aiGeneratedJobDescription = openAiController.askQuestion(jobDescriptionPrompt);

        System.out.println("AI-Generated Job Description: \n" + aiGeneratedJobDescription);

        // Store the AI-generated job description
        job.setJobDescription(aiGeneratedJobDescription);

        job.setCurrentStep(4);
    }

    private void processInterviewPlan(Job job) {
        // Convert skill list to a comma-separated string
        String skillString = String.join(", ", job.getPrimarySkills());

        // Load the prompt template
        PromptTemplate promptTemplate = new PromptTemplate("classpath:/prompts/interview-plan.st");

        // Create placeholders for the template
        Map<String, Object> variables = new HashMap<>();
        variables.put("title", job.getTitle());
        variables.put("experienceRange", job.getExperienceRange());
        variables.put("industryType", job.getIndustryType());
        variables.put("skills", skillString);

        // Generate the final prompt
        String prompt = promptTemplate.render(variables);

        // Call OpenAI API
        String response = openAiController.askQuestion(prompt);

        // Parse the response into an InterviewPlan object
        InterviewPlan interviewPlan = parseInterviewPlan(response);
        job.setInterviewPlan(interviewPlan);

        job.setCurrentStep(5);
        LOGGER.info("Step 4 completed: Interview Plan generated for Job ID {}", job.getId());
    }

    private void processJobSubmission(Job job) {
        job.setApplicationLink("http://localhost:3000/apply/" + job.getId());
        LOGGER.info("Step 5 completed: Job Submission link generated for Job ID {}", job.getId());
    }

    private InterviewPlan parseInterviewPlan(String aiResponse) {
        String[] sections = aiResponse.split("\n\n");

        if (sections.length < 4) {
            throw new IllegalArgumentException("Unexpected AI response format. Expected 4 sections but got " + sections.length);
        }

        InterviewPlan interviewPlan = new InterviewPlan();
        interviewPlan.setIntroduction(trimTo255(sections[0]));
        interviewPlan.setTechnicalAssessment(trimListTo255(Arrays.asList(sections[1].split("\n"))));
        interviewPlan.setBehavioralAssessment(trimListTo255(Arrays.asList(sections[2].split("\n"))));
        interviewPlan.setClosure(trimTo255(sections[3]));

        return interviewPlan;
    }

    private String trimTo255(String text) {
        return text.length() > 255 ? text.substring(0, 255) : text;
    }

    private List<String> trimListTo255(List<String> list) {
        return list.stream().map(this::trimTo255).toList();
    }

    private String loadPromptTemplate(String filePath) {
        try {
            Resource resource = resourceLoader.getResource("classpath:" + filePath);
            return new String(Files.readAllBytes(Paths.get(resource.getURI())));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load prompt template: " + filePath, e);
        }
    }

    @Transactional
    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
        LOGGER.info("Job ID {} deleted successfully", id);
    }
}
