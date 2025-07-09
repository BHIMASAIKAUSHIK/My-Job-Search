package com.jobagregator.backend.repository;

import com.jobagregator.backend.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface JobRepository extends JpaRepository<Job,Long> {
    
}
