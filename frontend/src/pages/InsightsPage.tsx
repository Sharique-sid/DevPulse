import { useEffect, useMemo, useState } from "react";
import { AxiosError } from "axios";
import { Link } from "react-router-dom";
import { fetchInsights, generateInsight } from "../services/insights";
import type { Insight } from "../types/insight";
import { Notice } from "../components/Notice";

function getErrorMessage(error: unknown) {
  if (error instanceof AxiosError && error.response?.data?.message) {
    return String(error.response.data.message);
  }
  return "Failed to load AI insights.";
}

export function InsightsPage() {
  const [items, setItems] = useState<Insight[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function load() {
    setError("");
    setSuccess("");
    setLoading(true);
    try {
      const data = await fetchInsights();
      setItems(data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  const sortedItems = useMemo(
    () => [...items].sort((a, b) => +new Date(b.generatedAt) - +new Date(a.generatedAt)),
    [items],
  );

  async function handleGenerate() {
    setGenerating(true);
    setError("");
    setSuccess("");
    try {
      const latest = await generateInsight();
      setItems((prev) => [latest, ...prev.filter((item) => item.id !== latest.id)]);
      setSuccess("AI insight generated successfully.");
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setGenerating(false);
    }
  }

  return (
    <div className="page">
      <div className="card">
        <div className="toolbar">
          <h1>AI Insights</h1>
          <Link className="ghost-link" to="/dashboard">
            Back to Dashboard
          </Link>
        </div>

        <div className="toolbar-actions">
          <button className="ghost-btn" onClick={load} type="button">
            Refresh
          </button>
          <button onClick={handleGenerate} type="button" disabled={generating}>
            {generating ? "Generating..." : "Generate Insight"}
          </button>
        </div>

        {error ? <Notice tone="error" text={error} /> : null}
        {success ? <Notice tone="success" text={success} /> : null}
        {loading ? <p>Loading insights...</p> : null}

        {!loading && sortedItems.length === 0 ? (
          <div className="empty-panel">No insights yet. Generate your first report.</div>
        ) : null}

        <div className="insight-grid">
          {sortedItems.map((insight) => (
            <article key={insight.id} className="insight-card">
              <div className="insight-meta">Generated: {new Date(insight.generatedAt).toLocaleString()}</div>
              <pre className="insight-text">{insight.insightText}</pre>
            </article>
          ))}
        </div>
      </div>
    </div>
  );
}
