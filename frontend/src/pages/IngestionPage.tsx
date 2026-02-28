import { FormEvent, useEffect, useState } from 'react';
import { getJob, submitFeedback, uploadCsv } from '../api/services';
import { JobStatus } from '../types';

type ManualFeedbackState = {
  sourceRef: string;
  text: string;
  rating: string;
  locale: string;
};

const terminalStates = new Set(['COMPLETED', 'FAILED']);

export function IngestionPage() {
  const [manual, setManual] = useState<ManualFeedbackState>({
    sourceRef: '',
    text: '',
    rating: '',
    locale: 'en',
  });
  const [csvFile, setCsvFile] = useState<File | null>(null);
  const [manualResult, setManualResult] = useState<string | null>(null);
  const [csvJob, setCsvJob] = useState<JobStatus | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [submittingManual, setSubmittingManual] = useState(false);
  const [uploadingCsv, setUploadingCsv] = useState(false);

  useEffect(() => {
    if (!csvJob || terminalStates.has(csvJob.status)) {
      return;
    }
    const timer = window.setInterval(async () => {
      try {
        const updated = await getJob(csvJob.jobId);
        setCsvJob(updated);
      } catch {
        // keep existing state and continue polling
      }
    }, 2500);
    return () => window.clearInterval(timer);
  }, [csvJob]);

  const onManualSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setManualResult(null);
    setSubmittingManual(true);
    try {
      const response = await submitFeedback({
        sourceType: 'REST',
        sourceRef: manual.sourceRef || undefined,
        text: manual.text,
        rating: manual.rating ? Number(manual.rating) : undefined,
        locale: manual.locale || 'en',
      });
      setManualResult(`Feedback accepted with id ${response.feedbackId}.`);
      setManual((prev) => ({ ...prev, text: '', rating: '' }));
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : 'Feedback submission failed');
    } finally {
      setSubmittingManual(false);
    }
  };

  const onCsvUpload = async () => {
    if (!csvFile) {
      setError('Select a CSV file to upload.');
      return;
    }
    setError(null);
    setUploadingCsv(true);
    try {
      const response = await uploadCsv(csvFile);
      const initialStatus = await getJob(response.jobId);
      setCsvJob(initialStatus);
    } catch (uploadError) {
      setError(uploadError instanceof Error ? uploadError.message : 'CSV upload failed');
    } finally {
      setUploadingCsv(false);
    }
  };

  return (
    <div className="page-grid">
      <section className="panel">
        <h2>Single Feedback Ingestion</h2>
        <form className="form-grid" onSubmit={onManualSubmit}>
          <label className="field">
            <span>Source Reference</span>
            <input
              value={manual.sourceRef}
              onChange={(e) => setManual((prev) => ({ ...prev, sourceRef: e.target.value }))}
              placeholder="ticket-1458"
            />
          </label>
          <label className="field">
            <span>Locale</span>
            <input
              value={manual.locale}
              onChange={(e) => setManual((prev) => ({ ...prev, locale: e.target.value }))}
              placeholder="en"
              required
            />
          </label>
          <label className="field">
            <span>Rating (optional 1-5)</span>
            <input
              type="number"
              min="1"
              max="5"
              value={manual.rating}
              onChange={(e) => setManual((prev) => ({ ...prev, rating: e.target.value }))}
            />
          </label>
          <label className="field">
            <span>Feedback Text</span>
            <textarea
              value={manual.text}
              onChange={(e) => setManual((prev) => ({ ...prev, text: e.target.value }))}
              rows={5}
              required
            />
          </label>
          <button type="submit" className="btn btn-primary" disabled={submittingManual}>
            {submittingManual ? 'Submitting...' : 'Submit Feedback'}
          </button>
        </form>
        {manualResult ? <p className="success">{manualResult}</p> : null}
      </section>

      <aside className="panel">
        <h2>Bulk CSV Upload</h2>
        <p className="supporting">
          CSV processing runs asynchronously. Supported: <code>.csv</code> only.
        </p>
        <label className="field">
          <span>CSV File</span>
          <input
            type="file"
            accept=".csv"
            onChange={(e) => setCsvFile(e.target.files?.[0] ?? null)}
          />
        </label>
        <button type="button" className="btn btn-primary" onClick={onCsvUpload} disabled={uploadingCsv}>
          {uploadingCsv ? 'Uploading...' : 'Upload CSV'}
        </button>

        {csvJob ? (
          <dl className="detail-list">
            <div>
              <dt>Job ID</dt>
              <dd>{csvJob.jobId}</dd>
            </div>
            <div>
              <dt>Status</dt>
              <dd>{csvJob.status}</dd>
            </div>
            <div>
              <dt>Progress</dt>
              <dd>{csvJob.progress ?? 0}%</dd>
            </div>
            <div>
              <dt>Payload</dt>
              <dd>{csvJob.payload || '{}'}</dd>
            </div>
          </dl>
        ) : (
          <p className="supporting">No CSV jobs started in this session.</p>
        )}
      </aside>

      {error ? <p className="error full-width">{error}</p> : null}
    </div>
  );
}
