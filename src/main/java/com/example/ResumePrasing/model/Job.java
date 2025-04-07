package com.example.ResumePrasing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "jobs")
@NoArgsConstructor
@AllArgsConstructor
public class Job {


    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Step 1: Job Basics
    private String title;
    private String role;
    private String experienceRange;
    private String industryType;
    
    @ElementCollection
    private List<String> primarySkills;
    
    // Step 2: Job Responsibilities
    @ElementCollection
    private List<String> responsibilities;
    
    // Step 3: Job Description
    private String locationType;
    private String location;
    private String companyName;
    private Integer salaryFrom;
    private Integer salaryTo;
    private Boolean hideSalary;
    
    @Column(columnDefinition = "TEXT")
    private String jobDescription;
    
    // Step 4: Interview Plan
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "interview_plan_id", referencedColumnName = "id")
    private InterviewPlan interviewPlan;
    
    // Step 5: Job Submission
    private String applicationLink;
    private LocalDateTime createdAt;
    
    // Current step tracking
    private Integer currentStep;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getExperienceRange() {
        return experienceRange;
    }

    public void setExperienceRange(String experienceRange) {
        this.experienceRange = experienceRange;
    }

    public String getIndustryType() {
        return industryType;
    }

    public void setIndustryType(String industryType) {
        this.industryType = industryType;
    }

    public List<String> getPrimarySkills() {
        return primarySkills;
    }

    public void setPrimarySkills(List<String> primarySkills) {
        this.primarySkills = primarySkills;
    }

    public List<String> getResponsibilities() {
        return responsibilities;
    }

    public void setResponsibilities(List<String> responsibilities) {
        this.responsibilities = responsibilities;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getSalaryFrom() {
        return salaryFrom;
    }

    public void setSalaryFrom(Integer salaryFrom) {
        this.salaryFrom = salaryFrom;
    }

    public Integer getSalaryTo() {
        return salaryTo;
    }

    public void setSalaryTo(Integer salaryTo) {
        this.salaryTo = salaryTo;
    }

    public Boolean getHideSalary() {
        return hideSalary;
    }

    public void setHideSalary(Boolean hideSalary) {
        this.hideSalary = hideSalary;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public InterviewPlan getInterviewPlan() {
        return interviewPlan;
    }

    public void setInterviewPlan(InterviewPlan interviewPlan) {
        this.interviewPlan = interviewPlan;
    }

    public String getApplicationLink() {
        return applicationLink;
    }

    public void setApplicationLink(String applicationLink) {
        this.applicationLink = applicationLink;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(Integer currentStep) {
        this.currentStep = currentStep;
    }
}