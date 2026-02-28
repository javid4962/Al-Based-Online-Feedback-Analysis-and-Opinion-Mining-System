package com.datavalley.feedback.controller;

import com.datavalley.feedback.dto.JobStatusResponse;
import com.datavalley.feedback.service.JobService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/{jobId}")
    public JobStatusResponse getJob(@PathVariable("jobId") Long jobId) {
        return jobService.getJob(jobId);
    }
}
