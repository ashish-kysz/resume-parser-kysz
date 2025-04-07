package com.example.ResumePrasing.services;

import com.example.ResumePrasing.controller.OpenAiController;
import com.example.ResumePrasing.dto.JobDTO;
import com.example.ResumePrasing.model.InterviewPlan;
import com.example.ResumePrasing.model.Job;
import com.example.ResumePrasing.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;
    private final OpenAiController openAiController;

    public JobService(JobRepository jobRepository, OpenAiController openAiController) {
        this.jobRepository = jobRepository;
        this.openAiController = openAiController;
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
        job.setPrimarySkills(jobDTO.getPrimarySkills());

        String prompt = "List job responsibilities for a " + jobDTO.getTitle() +
                " in " + jobDTO.getIndustryType() + " industry with " +
                jobDTO.getExperienceRange() + " years of experience. Provide as a list.";

        String response = openAiController.askQuestion(prompt);
        List<String> responsibilities = Arrays.asList(response.split("\n"));

        job.setResponsibilities(responsibilities);
        job.setCurrentStep(2);
        LOGGER.info("Step 1 completed: Job Responsibilities generated for Job ID {}", job.getId());
    }

    private void processJobResponsibilities(Job job) {
        job.setCurrentStep(3);
        LOGGER.info("Step 2 completed: Responsibilities confirmed for Job ID {}", job.getId());
    }

    private void processJobDescription(Job job, JobDTO jobDTO) {
        job.setLocationType(jobDTO.getLocationType());
        job.setLocation(jobDTO.getLocation());
        job.setCompanyName(jobDTO.getCompanyName());
        job.setSalaryFrom(jobDTO.getSalaryFrom());
        job.setSalaryTo(jobDTO.getSalaryTo());
        job.setHideSalary(jobDTO.getHideSalary());

        String formattedResponsibilities = String.join("\n", job.getResponsibilities());

        String prompt = "Create a detailed job description for a " + job.getTitle() +
                " in " + job.getIndustryType() + " industry with " +
                job.getExperienceRange() + " years of experience.\n\n" +
                "Primary skills: " + job.getPrimarySkills() + "\n" +
                "Responsibilities:\n" + formattedResponsibilities;

        String response = openAiController.askQuestion(prompt);
        job.setJobDescription(response);

        job.setCurrentStep(4);
        LOGGER.info("Step 3 completed: Job Description generated for Job ID {}", job.getId());
    }

    private void processInterviewPlan(Job job) {
        String prompt = "Create an interview plan for a " + job.getTitle() +
                " role with " + job.getExperienceRange() + " years of experience in the " + job.getIndustryType() +
                " industry.\n\n" +
                "Include:\n" +
                "- Introduction by the recruiter\n" +
                "- 5 technical assessment questions (each under 255 characters) related to " + job.getPrimarySkills() + "\n" +
                "- 5 behavioral assessment questions (each under 255 characters) focusing on teamwork, problem-solving, and leadership.\n" +
                "- A closing statement.";

        String response = openAiController.askQuestion(prompt);
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
        InterviewPlan interviewPlan = new InterviewPlan();
        String[] sections = aiResponse.split("\n\n");

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

    @Transactional
    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
        LOGGER.info("Job ID {} deleted successfully", id);
    }
}
