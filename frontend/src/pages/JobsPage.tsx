import { FormEvent, useEffect, useMemo, useState } from 'react';
import { getJob, reanalyze } from '../api/services';
import { JobStatus, SourceType } from '../types';

const terminalStatuses = new Set(['COMPLETED', 'FAILED']);

function isoDate(daysOffset = 0) {
  const d = new Date();
  d.setDate(d.getDate() + daysOffset);
  return d.toISOString().slice(0, 10);
}

export function JobsPage() {
  const [trackedJobs, setTrackedJobs] = useState<Record<number, JobStatus>>({});
  const [jobIdInput, setJobIdInput] = useState('');
  const [fromDate, setFromDate] = useState(isoDate(-7));
  const [toDate, setToDate] = useState(isoDate());
  const [sourceType, setSourceType] = useState<'' | SourceType>('');
  const [creatingReanalyzeJob, setCreatingReanalyzeJob] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const sortedJobs = useMemo(() => {
    return Object.values(trackedJobs).sort(
      (a, b) => Date.parse(b.startedAt ?? '') - Date.parse(a.startedAt ?? '')
    );
  }, [trackedJobs]);

  useEffect(() => {
    const running = Object.values(trackedJobs).filter((job) => !terminalStatuses.has(job.status));
    if (running.length === 0) {
      return;
    }
    const timer = window.setInterval(() => {
      running.forEach((job) => {
        void getJob(job.jobId)
          .then((updated) => {
            setTrackedJobs((prev) => ({ ...prev, [updated.jobId]: updated }));
          })
          .catch(() => {
            // no-op on transient poll errors
          });
      });
    }, 2500);

    return () => window.clearInterval(timer);
  }, [trackedJobs]);

  const addJobById = async (event: FormEvent) => {
    event.preventDefault();
    if (!jobIdInput.trim()) {
      return;
    }
    setError(null);
    try {
      const id = Number(jobIdInput);
      if (Number.isNaN(id)) {
        throw new Error('Job id must be numeric');
      }
      const status = await getJob(id);
      setTrackedJobs((prev) => ({ ...prev, [status.jobId]: status }));
      setJobIdInput('');
    } catch (jobError) {
      setError(jobError instanceof Error ? jobError.message : 'Failed to load job');
    }
  };

  const startReanalyze = async (event: FormEvent) => {
    event.preventDefault();
    setCreatingReanalyzeJob(true);
    setError(null);
    try {
      const response = await reanalyze({
        fromDate: fromDate || undefined,
        toDate: toDate || undefined,
        sourceType: sourceType || undefined,
      });
      const status = await getJob(response.jobId);
      setTrackedJobs((prev) => ({ ...prev, [status.jobId]: status }));
    } catch (jobError) {
      setError(jobError instanceof Error ? jobError.message : 'Failed to start reanalyze job');
    } finally {
      setCreatingReanalyzeJob(false);
    }
  };

  return (
    <div className="page-grid">
      <section className="panel">
        <h2>Start Reanalyze Job</h2>
        <form className="form-grid" onSubmit={startReanalyze}>
          <label className="field compact">
            <span>From Date</span>
            <input type="date" value={fromDate} onChange={(e) => setFromDate(e.target.value)} />
          </label>
          <label className="field compact">
            <span>To Date</span>
            <input type="date" value={toDate} onChange={(e) => setToDate(e.target.value)} />
          </label>
          <label className="field compact">
            <span>Source</span>
            <select value={sourceType} onChange={(e) => setSourceType(e.target.value as '' | SourceType)}>
              <option value="">All</option>
              <option value="REST">REST</option>
              <option value="CSV">CSV</option>
            </select>
          </label>
          <button className="btn btn-primary" type="submit" disabled={creatingReanalyzeJob}>
            {creatingReanalyzeJob ? 'Starting...' : 'Run Reanalyze'}
          </button>
        </form>
      </section>

      <aside className="panel">
        <h2>Track Existing Job</h2>
        <form className="inline-fields" onSubmit={addJobById}>
          <label className="field compact">
            <span>Job ID</span>
            <input
              value={jobIdInput}
              onChange={(e) => setJobIdInput(e.target.value)}
              placeholder="44"
            />
          </label>
          <button className="btn btn-ghost" type="submit">
            Add Job
          </button>
        </form>
      </aside>

      <section className="panel full-width">
        <h2>Tracked Jobs</h2>
        {error ? <p className="error">{error}</p> : null}
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Job ID</th>
                <th>Type</th>
                <th>Status</th>
                <th>Progress</th>
                <th>Started</th>
                <th>Ended</th>
                <th>Payload</th>
              </tr>
            </thead>
            <tbody>
              {sortedJobs.length === 0 ? (
                <tr>
                  <td colSpan={7}>No jobs tracked yet.</td>
                </tr>
              ) : null}
              {sortedJobs.map((job) => (
                <tr key={job.jobId}>
                  <td>{job.jobId}</td>
                  <td>{job.jobType}</td>
                  <td>{job.status}</td>
                  <td>{job.progress ?? 0}%</td>
                  <td>{job.startedAt ? new Date(job.startedAt).toLocaleString() : '-'}</td>
                  <td>{job.endedAt ? new Date(job.endedAt).toLocaleString() : '-'}</td>
                  <td>{job.payload || '{}'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
