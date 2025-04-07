package com.example.ResumePrasing.repository;


import com.example.ResumePrasing.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeUploadRepository extends JpaRepository<Resume, Long> {

}
