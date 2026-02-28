package com.datavalley.feedback.service.impl;

import com.datavalley.feedback.dto.JobStatusResponse;
import com.datavalley.feedback.entity.JobRun;
import com.datavalley.feedback.repository.JobRunRepository;
import com.datavalley.feedback.service.JobService;
import org.springframework.stereotype.Service;

@Service
public class JobServiceImpl implements JobService {

    private final JobRunRepository jobRunRepository;

    public JobServiceImpl(JobRunRepository jobRunRepository) {
        this.jobRunRepository = jobRunRepository;
    }

    @Override
    public JobStatusResponse getJob(Long jobId) {
        JobRun job = jobRunRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        return new JobStatusResponse(
            job.getId(),
            job.getJobType().name(),
            job.getStatus().name(),
            job.getProgress(),
            job.getPayloadJson(),
            job.getStartedAt(),
            job.getEndedAt()
        );
    }
}
