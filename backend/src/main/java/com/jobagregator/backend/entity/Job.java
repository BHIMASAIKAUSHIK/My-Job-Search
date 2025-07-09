package com.jobagregator.backend.entity; 


import jakarta.persistence.*;
import java.time.LocalDateTime;// ← ADD THIS LINE


@Entity
@Table (name = "jobs")
public class Job {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String jobtitle;

    @Column(nullable = false)
    private String company;

    private String location;
    @Column(columnDefinition = "TEXT")  // Allows very long text

    private String description;
    private String sourceUrl;

    @Column(name = "posted_time")
    private LocalDateTime postedTime;

    @Column(name = "saved_time")
    private LocalDateTime savedTime;



    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getJobtitle() {
        return jobtitle;
    }


    public void setJobtitle(String jobtitle) {
        this.jobtitle = jobtitle;
    }


    public String getCompany() {
        return company;
    }


    public void setCompany(String company) {
        this.company = company;
    }


    public String getLocation() {
        return location;
    }


    public void setLocation(String location) {
        this.location = location;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public String getSourceUrl() {
        return sourceUrl;
    }


    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }


    public LocalDateTime getPostedTime() {
        return postedTime;
    }


    public void setPostedTime(LocalDateTime postedTime) {
        this.postedTime = postedTime;
    }


    public LocalDateTime getSavedTime() {
        return savedTime;
    }


    public void setSavedTime(LocalDateTime savedTime) {
        this.savedTime = savedTime;
    }


    public Job()
    {
        
    }


    public Job(String jobtitle, String company)
    {
        this.jobtitle = jobtitle;
        this.company = company;
        this.savedTime = LocalDateTime.now();
    }
    
}
