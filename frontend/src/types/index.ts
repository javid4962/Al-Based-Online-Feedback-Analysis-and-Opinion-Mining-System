export type LoginRequest = {
  username: string;
  password: string;
};

export type AuthTokenResponse = {
  accessToken: string;
  expiresAt: string;
};

export type SourceType = 'REST' | 'CSV';
export type SentimentLabel = 'POSITIVE' | 'NEGATIVE';

export type OverviewMetrics = {
  total: number;
  positive: number;
  negative: number;
  positivePct: number;
  negativePct: number;
  trendSeries: Array<{ date: string; total: number; positive: number; negative: number }>;
};

export type TopicItem = {
  token: string;
  count: number;
  sentiment: string;
};

export type FeedbackListItem = {
  feedbackId: number;
  sourceType: SourceType;
  sourceRef: string | null;
  text: string;
  rating: number | null;
  locale: string;
  submittedAt: string;
  sentiment: SentimentLabel | null;
  confidence: number | null;
};

export type FeedbackListResponse = {
  content: FeedbackListItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type FeedbackQuery = {
  sourceType?: SourceType;
  sentiment?: SentimentLabel;
  confidence?: number;
  keyword?: string;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
};

export type PredictionResponse = {
  feedbackId: number;
  label: SentimentLabel;
  confidence: number;
  modelVersion: string;
  inferredAt: string;
};

export type JobResponse = {
  jobId: number;
  status: string;
};

export type ModelVersion = {
  id: number;
  versionName: string;
  active: boolean;
  createdAt: string;
};

export type TrainRequest = {
  datasetPolicy: string;
  epochs?: number;
  batchSize?: number;
  learningRate?: number;
  rebalance?: boolean;
};

export type ReanalyzeRequest = {
  fromDate?: string;
  toDate?: string;
  sourceType?: SourceType;
};

export type JobStatus = {
  jobId: number;
  jobType: string;
  status: string;
  progress: number | null;
  payload: string;
  startedAt: string;
  endedAt?: string;
};
