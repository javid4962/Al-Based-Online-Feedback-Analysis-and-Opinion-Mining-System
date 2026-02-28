package com.datavalley.feedback.service;

import com.datavalley.feedback.dto.JobStatusResponse;

public interface JobService {
    JobStatusResponse getJob(Long jobId);
}
