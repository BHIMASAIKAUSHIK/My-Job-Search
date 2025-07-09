package com.jobagregator.backend.controller;


import com.jobagregator.backend.entity.Job;
import com.jobagregator.backend.repository.JobRepository;
import com.jobagregator.backend.service.JobFetchingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/BSK")
@CrossOrigin(origins = "http://localhost:3000")
public class JobController {


    @Autowired
    private JobRepository jobRepository;


    @Autowired
    private JobFetchingService jobFetchingService;


    @GetMapping("/jobs")
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }
    

    @GetMapping("/jobs/{id}")
    public Job getById(@PathVariable Long id) {
        return jobRepository.findById(id).orElse(null);
    }


    @DeleteMapping("/jobs/{id}")
    public void deleteJob(@PathVariable Long id) {
        jobRepository.deleteById(id);
    }

    @PostMapping("/jobs/refresh-jobs")
public String manualRefreshJobs() {
    jobFetchingService.manualRefresh();
    return "Job refresh completed! Check your database for new jobs.";
}
    





    
}
