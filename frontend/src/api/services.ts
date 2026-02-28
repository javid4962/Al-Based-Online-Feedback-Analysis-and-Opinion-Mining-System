import { apiFetch } from './client';
import {
  AuthTokenResponse,
  FeedbackListResponse,
  FeedbackQuery,
  JobResponse,
  JobStatus,
  LoginRequest,
  ModelVersion,
  OverviewMetrics,
  PredictionResponse,
  ReanalyzeRequest,
  SourceType,
  TrainRequest,
  TopicItem,
} from '../types';

export function login(request: LoginRequest) {
  return apiFetch<AuthTokenResponse>('/api/v1/auth/login', {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

type DateRange = {
  from?: string;
  to?: string;
};

function toQuery(params: Record<string, string | number | undefined>) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === '') {
      return;
    }
    query.set(key, String(value));
  });
  const value = query.toString();
  return value ? `?${value}` : '';
}

export function getOverview(range: DateRange = {}) {
  return apiFetch<OverviewMetrics>(`/api/v1/analytics/overview${toQuery(range)}`);
}

export function getTopics(range: DateRange = {}) {
  return apiFetch<TopicItem[]>(`/api/v1/analytics/topics${toQuery(range)}`);
}

export function listFeedback(query: FeedbackQuery = {}) {
  return apiFetch<FeedbackListResponse>(`/api/v1/feedback${toQuery({
    sourceType: query.sourceType,
    sentiment: query.sentiment,
    confidence: query.confidence,
    keyword: query.keyword,
    from: query.from,
    to: query.to,
    page: query.page ?? 0,
    size: query.size ?? 20,
  })}`);
}

export function submitFeedback(payload: {
  sourceType: SourceType;
  sourceRef?: string;
  text: string;
  rating?: number;
  locale: string;
}) {
  return apiFetch<{ feedbackId: number; status: string }>('/api/v1/feedback', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function uploadCsv(file: File) {
  const data = new FormData();
  data.append('file', file);
  return apiFetch<JobResponse>('/api/v1/feedback/bulk-csv', {
    method: 'POST',
    body: data,
  });
}

export function listModels() {
  return apiFetch<ModelVersion[]>('/api/v1/models');
}

export function trainModel(request: TrainRequest) {
  return apiFetch<{ runId: number; status: string }>('/api/v1/models/train', {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export function activateModel(versionId: number) {
  return apiFetch<{ activeVersionId: number }>(`/api/v1/models/${versionId}/activate`, {
    method: 'POST',
  });
}

export function reanalyze(request: ReanalyzeRequest) {
  return apiFetch<JobResponse>('/api/v1/predictions/reanalyze', {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

export function getJob(jobId: number) {
  return apiFetch<JobStatus>(`/api/v1/jobs/${jobId}`);
}

export function getPrediction(feedbackId: number) {
  return apiFetch<PredictionResponse>(`/api/v1/predictions/${feedbackId}`);
}
