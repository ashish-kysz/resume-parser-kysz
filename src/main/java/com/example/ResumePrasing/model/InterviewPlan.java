package com.example.ResumePrasing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "interview_plans")
@NoArgsConstructor
@AllArgsConstructor
public class InterviewPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String closure;

    @ElementCollection
    private List<String> technicalAssessment;

    @ElementCollection
    private List<String> behavioralAssessment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getClosure() {
        return closure;
    }

    public void setClosure(String closure) {
        this.closure = closure;
    }

    public List<String> getTechnicalAssessment() {
        return technicalAssessment;
    }

    public void setTechnicalAssessment(List<String> technicalAssessment) {
        this.technicalAssessment = technicalAssessment;
    }

    public List<String> getBehavioralAssessment() {
        return behavioralAssessment;
    }

    public void setBehavioralAssessment(List<String> behavioralAssessment) {
        this.behavioralAssessment = behavioralAssessment;
    }
}
