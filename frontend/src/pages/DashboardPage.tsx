import { useEffect, useMemo, useState } from 'react';
import { getOverview, getTopics } from '../api/services';
import { OverviewMetrics, TopicItem } from '../types';

function isoDate(daysOffset = 0) {
  const d = new Date();
  d.setDate(d.getDate() + daysOffset);
  return d.toISOString().slice(0, 10);
}

export function DashboardPage() {
  const [from, setFrom] = useState(isoDate(-14));
  const [to, setTo] = useState(isoDate());
  const [overview, setOverview] = useState<OverviewMetrics | null>(null);
  const [topics, setTopics] = useState<TopicItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setLoading(true);
      setError(null);
      try {
        const [overviewRes, topicsRes] = await Promise.all([
          getOverview({ from, to }),
          getTopics({ from, to }),
        ]);
        if (!cancelled) {
          setOverview(overviewRes);
          setTopics(topicsRes);
        }
      } catch (loadError) {
        if (!cancelled) {
          setError(loadError instanceof Error ? loadError.message : 'Failed to load dashboard');
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
  }, [from, to]);

  const positiveTopics = useMemo(
    () => topics.filter((topic) => topic.sentiment === 'POSITIVE').slice(0, 8),
    [topics]
  );
  const negativeTopics = useMemo(
    () => topics.filter((topic) => topic.sentiment === 'NEGATIVE').slice(0, 8),
    [topics]
  );
  const maxTrendTotal = useMemo(() => {
    if (!overview || overview.trendSeries.length === 0) {
      return 1;
    }
    return Math.max(...overview.trendSeries.map((point) => point.total), 1);
  }, [overview]);

  return (
    <div className="page-grid">
      <section className="panel">
        <div className="panel-header">
          <h2>Sentiment Overview</h2>
          <div className="inline-fields">
            <label className="field compact">
              <span>From</span>
              <input type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
            </label>
            <label className="field compact">
              <span>To</span>
              <input type="date" value={to} onChange={(e) => setTo(e.target.value)} />
            </label>
          </div>
        </div>

        {loading ? <p className="supporting">Loading overview...</p> : null}
        {error ? <p className="error">{error}</p> : null}

        {overview ? (
          <>
            <div className="stats-row">
              <article className="stat-card">
                <p className="meta-label">Total Feedback</p>
                <p className="stat-value">{overview.total}</p>
              </article>
              <article className="stat-card">
                <p className="meta-label">Positive Share</p>
                <p className="stat-value">{overview.positivePct.toFixed(1)}%</p>
              </article>
              <article className="stat-card">
                <p className="meta-label">Negative Share</p>
                <p className="stat-value">{overview.negativePct.toFixed(1)}%</p>
              </article>
            </div>

            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Total</th>
                    <th>Positive</th>
                    <th>Negative</th>
                    <th>Volume</th>
                  </tr>
                </thead>
                <tbody>
                  {overview.trendSeries.map((point) => (
                    <tr key={point.date}>
                      <td>{point.date}</td>
                      <td>{point.total}</td>
                      <td>{point.positive}</td>
                      <td>{point.negative}</td>
                      <td>
                        <div className="trend-bar">
                          <span style={{ width: `${(point.total / maxTrendTotal) * 100}%` }} />
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        ) : null}
      </section>

      <section className="panel dual-panel">
        <div>
          <h2>Positive Topics</h2>
          <ul className="token-list">
            {positiveTopics.length === 0 ? <li>No data in range.</li> : null}
            {positiveTopics.map((topic) => (
              <li key={`pos-${topic.token}`}>
                <span>{topic.token}</span>
                <strong>{topic.count}</strong>
              </li>
            ))}
          </ul>
        </div>
        <div>
          <h2>Negative Topics</h2>
          <ul className="token-list">
            {negativeTopics.length === 0 ? <li>No data in range.</li> : null}
            {negativeTopics.map((topic) => (
              <li key={`neg-${topic.token}`}>
                <span>{topic.token}</span>
                <strong>{topic.count}</strong>
              </li>
            ))}
          </ul>
        </div>
      </section>
    </div>
  );
}
