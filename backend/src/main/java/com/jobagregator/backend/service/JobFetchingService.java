package com.jobagregator.backend.service;
import com.jobagregator.backend.entity.Job;
import com.jobagregator.backend.repository.JobRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.scheduling.annotation.Async;

import java.util.List;


@Service
public class JobFetchingService {

    @Autowired
    private JobRepository jobRepository;


    @Autowired
    private RestTemplate restTemplate;


    private final String API_KEY = "da4db4e539msh05aea270166b280p1fee2ajsn0dcdd7d1fb32";
    private final String BASE_URL = "https://jsearch.p.rapidapi.com/search";

    @Scheduled(fixedRate = 10800000)
    @Async
    public  void refreshjobs()
    {

        System.out.println("refreshing jobs");

        jobRepository.deleteAll();

        System.out.println("CLEARED OLD  jobs FROM DATABSE ");

        fetchLatestJobs();



    }


    public void fetchLatestJobs()
    {
        String[] roles = {"software engineer", "full stack developer", "backend developer", "frontend developer"};
        String[] locations = {"USA", "remote"};
        String[] jobTypes = {"full time", "intern"};

        for (String role : roles) {
            for (String location : locations) {
                for (String jobType : jobTypes) {
                    fetchJobsFromAPI(role, location, jobType);
                    
                    try {
                        Thread.sleep(1000); // 1 second delay between API calls
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        
    }


    public void fetchJobsFromAPI(String role, String location , String jobType)
    {
        try{

            String searchQuery = role + " " + jobType + " " + location;

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-rapidapi-host", "jsearch.p.rapidapi.com");
            headers.set("x-rapidapi-key", API_KEY);

            String url = BASE_URL + "?query=" + searchQuery.replace(" ", "%20") + "&page=1&num_pages=1";
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            List<StandardJobData> standardizedJobs = extractStandardJobData(response.getBody());


            parseAndSaveJobs(standardizedJobs);
            System.out.println("Completed: " + searchQuery);




        }
        catch(Exception e)
        {

        }
    }
    private static class StandardJobData {
        String title;
        String company;
        String location;
        String description;
        String applyLink;
        String postedDate;
        
        StandardJobData(String title, String company, String location, String description, String applyLink, String postedDate) {
            this.title = title;
            this.company = company;
            this.location = location;
            this.description = description;
            this.applyLink = applyLink;
            this.postedDate = postedDate;
        }
    }

    private List<StandardJobData> extractStandardJobData(String jsonResponse) {
    List<StandardJobData> standardizedJobs = new ArrayList<>();
    
    try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonResponse);
        JsonNode jobs = root.path("data");
        
        for (JsonNode jobNode : jobs) {
            
            // SMART TITLE EXTRACTION - try all possible field names
            String title = "";
            String[] titleFields = {"job_title", "title", "position", "role", "job_position", "position_title"};
            for (String field : titleFields) {
                title = jobNode.path(field).asText().trim();
                if (!title.isEmpty() && !title.equals("null")) {
                    break; // Found a valid title
                }
            }
            
            // SMART COMPANY EXTRACTION - try all possible field names
            String company = "";
            String[] companyFields = {"employer_name", "company", "organization", "employer", "company_name", "org_name"};
            for (String field : companyFields) {
                company = jobNode.path(field).asText().trim();
                if (!company.isEmpty() && !company.equals("null")) {
                    break; // Found a valid company
                }
            }
            
            // SMART LOCATION EXTRACTION - try all possible combinations
            String location = "";
            // Try combined location first
            String[] locationFields = {"location", "job_location", "full_location", "address"};
            for (String field : locationFields) {
                location = jobNode.path(field).asText().trim();
                if (!location.isEmpty() && !location.equals("null")) {
                    break;
                }
            }
            
            // If no combined location, try city + state
            if (location.isEmpty()) {
                String city = "";
                String state = "";
                
                String[] cityFields = {"job_city", "city", "location_city"};
                for (String field : cityFields) {
                    city = jobNode.path(field).asText().trim();
                    if (!city.isEmpty() && !city.equals("null")) {
                        break;
                    }
                }
                
                String[] stateFields = {"job_state", "state", "location_state", "region"};
                for (String field : stateFields) {
                    state = jobNode.path(field).asText().trim();
                    if (!state.isEmpty() && !state.equals("null")) {
                        break;
                    }
                }
                
                location = city + (state.isEmpty() ? "" : ", " + state);
            }
            
            // SMART DESCRIPTION EXTRACTION
            String description = "";
            String[] descFields = {"job_description", "description", "summary", "job_summary", "details"};
            for (String field : descFields) {
                description = jobNode.path(field).asText().trim();
                if (!description.isEmpty() && !description.equals("null")) {
                    break;
                }
            }
            
            // SMART APPLY LINK EXTRACTION
            String applyLink = "";
            String[] linkFields = {"job_apply_link", "apply_link", "url", "job_url", "application_url", "link"};
            for (String field : linkFields) {
                applyLink = jobNode.path(field).asText().trim();
                if (!applyLink.isEmpty() && !applyLink.equals("null")) {
                    break;
                }
            }
            
            // SMART POSTED DATE EXTRACTION
            String postedDate = "";
            String[] dateFields = {"job_posted_at_datetime_utc", "posted_date", "created_at", "date_posted", "publish_date"};
            for (String field : dateFields) {
                postedDate = jobNode.path(field).asText().trim();
                if (!postedDate.isEmpty() && !postedDate.equals("null")) {
                    break;
                }
            }
            
            // Only add if we have essential data (title and company)
            if (!title.isEmpty() && !company.isEmpty()) {
                standardizedJobs.add(new StandardJobData(title, company, location, description, applyLink, postedDate));
                System.out.println("Extracted: " + title + " at " + company + " (" + location + ")");
            } else {
                System.out.println("Skipped job: Missing essential data (title or company)");
            }
        }
        
        System.out.println("Successfully extracted " + standardizedJobs.size() + " jobs");
        
    } catch (Exception e) {
        System.out.println("Error extracting job data: " + e.getMessage());
    }
    
    return standardizedJobs;
}


private void parseAndSaveJobs(List<StandardJobData> standardizedJobs) {
    for (StandardJobData jobData : standardizedJobs) {
        try {
            Job job = new Job();
            job.setJobtitle(jobData.title);
            job.setCompany(jobData.company);
            job.setLocation(jobData.location);
            job.setDescription(jobData.description.length() > 500 ? 
                              jobData.description.substring(0, 500) + "..." : jobData.description);
            job.setSourceUrl(jobData.applyLink);
            job.setSavedTime(LocalDateTime.now());
            
            // Handle posted date
            if (!jobData.postedDate.isEmpty()) {
                try {
                    job.setPostedTime(LocalDateTime.parse(jobData.postedDate.replace("Z", "")));
                } catch (Exception e) {
                    job.setPostedTime(LocalDateTime.now().minusDays(1));
                }
            } else {
                job.setPostedTime(LocalDateTime.now().minusDays(1));
            }
            
            jobRepository.save(job);
            System.out.println("Saved: " + jobData.title + " at " + jobData.company);
            
        } catch (Exception e) {
            System.out.println("Error saving job: " + e.getMessage());
        }
    }
    
}


public void manualRefresh() {
    System.out.println("Manual refresh triggered");
    jobRepository.deleteAll();
    System.out.println("Cleared old jobs from database");
    fetchLatestJobs();
    System.out.println("Manual refresh completed!");
}

}
