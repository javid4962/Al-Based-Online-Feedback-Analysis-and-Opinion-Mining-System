import { useEffect, useMemo, useState } from 'react';
import { getPrediction, listFeedback } from '../api/services';
import {
  FeedbackListItem,
  FeedbackListResponse,
  PredictionResponse,
  SentimentLabel,
  SourceType,
} from '../types';

type FeedbackFilterState = {
  sourceType: '' | SourceType;
  sentiment: '' | SentimentLabel;
  keyword: string;
  confidence: string;
  from: string;
  to: string;
};

const emptyResponse: FeedbackListResponse = {
  content: [],
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
};

export function FeedbackPage() {
  const [filters, setFilters] = useState<FeedbackFilterState>({
    sourceType: '',
    sentiment: '',
    keyword: '',
    confidence: '',
    from: '',
    to: '',
  });
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [data, setData] = useState<FeedbackListResponse>(emptyResponse);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selected, setSelected] = useState<FeedbackListItem | null>(null);
  const [prediction, setPrediction] = useState<PredictionResponse | null>(null);
  const [predictionError, setPredictionError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await listFeedback({
          sourceType: filters.sourceType || undefined,
          sentiment: filters.sentiment || undefined,
          keyword: filters.keyword || undefined,
          confidence: filters.confidence ? Number(filters.confidence) : undefined,
          from: filters.from || undefined,
          to: filters.to || undefined,
          page,
          size,
        });
        if (!cancelled) {
          setData(response);
          if (selected) {
            const stillExists = response.content.some((item) => item.feedbackId === selected.feedbackId);
            if (!stillExists) {
              setSelected(null);
              setPrediction(null);
              setPredictionError(null);
            }
          }
        }
      } catch (loadError) {
        if (!cancelled) {
          setError(loadError instanceof Error ? loadError.message : 'Failed to load feedback');
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    void load();
    return () => {
      cancelled = true;
    };
  }, [filters, page, selected, size]);

  useEffect(() => {
    if (!selected) {
      return;
    }
    let cancelled = false;
    const loadPrediction = async () => {
      setPrediction(null);
      setPredictionError(null);
      try {
        const response = await getPrediction(selected.feedbackId);
        if (!cancelled) {
          setPrediction(response);
        }
      } catch (loadError) {
        if (!cancelled) {
          setPredictionError(loadError instanceof Error ? loadError.message : 'Prediction not available');
        }
      }
    };
    void loadPrediction();
    return () => {
      cancelled = true;
    };
  }, [selected]);

  const pageOptions = useMemo(() => [10, 20, 50], []);

  return (
    <div className="page-grid">
      <section className="panel">
        <div className="panel-header">
          <h2>Feedback Explorer</h2>
          <p className="supporting">Filter by source, sentiment, confidence, keyword, and date range.</p>
        </div>

        <div className="filters-grid">
          <label className="field compact">
            <span>Source</span>
            <select
              value={filters.sourceType}
              onChange={(e) => {
                setPage(0);
                setFilters((prev) => ({ ...prev, sourceType: e.target.value as FeedbackFilterState['sourceType'] }));
              }}
            >
              <option value="">All</option>
              <option value="REST">REST</option>
              <option value="CSV">CSV</option>
            </select>
          </label>

          <label className="field compact">
            <span>Sentiment</span>
            <select
              value={filters.sentiment}
              onChange={(e) => {
                setPage(0);
                setFilters((prev) => ({ ...prev, sentiment: e.target.value as FeedbackFilterState['sentiment'] }));
              }}
            >
              <option value="">All</option>
              <option value="POSITIVE">POSITIVE</option>
              <option value="NEGATIVE">NEGATIVE</option>
            </select>
          </label>

          <label className="field compact">
            <span>Min Confidence</span>
            <input
              type="number"
              min="0"
              max="1"
              step="0.01"
              value={filters.confidence}
              onChange={(e) => {
                setPage(0);
                setFilters((prev) => ({ ...prev, confidence: e.target.value }));
              }}
            />
          </label>

          <label className="field compact">
            <span>Keyword</span>
            <input
              value={filters.keyword}
              onChange={(e) => {
                setPage(0);
                setFilters((prev) => ({ ...prev, keyword: e.target.value }));
              }}
              placeholder="delivery, support, refund..."
            />
          </label>

          <label className="field compact">
            <span>From</span>
            <input
              type="date"
              value={filters.from}
              onChange={(e) => {
                setPage(0);
                setFilters((prev) => ({ ...prev, from: e.target.value }));
              }}
            />
          </label>

          <label className="field compact">
            <span>To</span>
            <input
              type="date"
              value={filters.to}
              onChange={(e) => {
                setPage(0);
                setFilters((prev) => ({ ...prev, to: e.target.value }));
              }}
            />
          </label>
        </div>

        {loading ? <p className="supporting">Loading feedback...</p> : null}
        {error ? <p className="error">{error}</p> : null}

        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Source</th>
                <th>Text</th>
                <th>Rating</th>
                <th>Sentiment</th>
                <th>Confidence</th>
                <th>Submitted</th>
              </tr>
            </thead>
            <tbody>
              {data.content.length === 0 ? (
                <tr>
                  <td colSpan={7}>No feedback matches current filters.</td>
                </tr>
              ) : null}
              {data.content.map((item) => (
                <tr
                  key={item.feedbackId}
                  className={selected?.feedbackId === item.feedbackId ? 'row-active' : ''}
                  onClick={() => setSelected(item)}
                >
                  <td>{item.feedbackId}</td>
                  <td>{item.sourceType}</td>
                  <td>{item.text.slice(0, 120)}</td>
                  <td>{item.rating ?? '-'}</td>
                  <td>{item.sentiment ?? '-'}</td>
                  <td>{item.confidence?.toFixed(3) ?? '-'}</td>
                  <td>{new Date(item.submittedAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="pager">
          <div>
            <strong>{data.totalElements}</strong> records
          </div>
          <div className="pager-controls">
            <label className="field compact">
              <span>Page Size</span>
              <select
                value={size}
                onChange={(e) => {
                  setSize(Number(e.target.value));
                  setPage(0);
                }}
              >
                {pageOptions.map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </select>
            </label>
            <button type="button" className="btn btn-ghost" onClick={() => setPage((p) => Math.max(0, p - 1))}>
              Prev
            </button>
            <p>
              Page {data.page + 1} / {Math.max(data.totalPages, 1)}
            </p>
            <button
              type="button"
              className="btn btn-ghost"
              onClick={() => setPage((p) => (p + 1 < data.totalPages ? p + 1 : p))}
            >
              Next
            </button>
          </div>
        </div>
      </section>

      <aside className="panel detail-panel">
        <h2>Prediction Detail</h2>
        {!selected ? <p className="supporting">Select a feedback row to inspect prediction metadata.</p> : null}
        {selected ? (
          <>
            <p>
              <strong>Feedback #{selected.feedbackId}</strong>
            </p>
            <p className="supporting">{selected.text}</p>
            {prediction ? (
              <dl className="detail-list">
                <div>
                  <dt>Label</dt>
                  <dd>{prediction.label}</dd>
                </div>
                <div>
                  <dt>Confidence</dt>
                  <dd>{prediction.confidence.toFixed(4)}</dd>
                </div>
                <div>
                  <dt>Model Version</dt>
                  <dd>{prediction.modelVersion}</dd>
                </div>
                <div>
                  <dt>Inferred At</dt>
                  <dd>{new Date(prediction.inferredAt).toLocaleString()}</dd>
                </div>
              </dl>
            ) : (
              <p className="supporting">Loading prediction...</p>
            )}
            {predictionError ? <p className="error">{predictionError}</p> : null}
          </>
        ) : null}
      </aside>
    </div>
  );
}
