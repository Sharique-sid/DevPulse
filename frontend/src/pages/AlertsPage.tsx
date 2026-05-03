import { useEffect, useMemo, useState } from "react";
import { AxiosError } from "axios";
import { Link } from "react-router-dom";
import { fetchAlerts } from "../services/alerts";
import type { AlertFilter, AlertItem } from "../types/alert";
import { Notice } from "../components/Notice";

function getErrorMessage(error: unknown) {
  if (error instanceof AxiosError && error.response?.data?.message) {
    return String(error.response.data.message);
  }
  return "Failed to load alerts.";
}

export function AlertsPage() {
  const [filter, setFilter] = useState<AlertFilter>("all");
  const [items, setItems] = useState<AlertItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [autoRefresh, setAutoRefresh] = useState(false);

  async function load(currentFilter: AlertFilter) {
    setError("");
    setLoading(true);
    try {
      const data = await fetchAlerts(currentFilter);
      setItems(data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load(filter);
  }, [filter]);

  useEffect(() => {
    if (!autoRefresh) {
      return;
    }

    const id = window.setInterval(() => {
      load(filter);
    }, 15000);

    return () => window.clearInterval(id);
  }, [autoRefresh, filter]);

  const sortedItems = useMemo(
    () => [...items].sort((a, b) => +new Date(b.createdAt) - +new Date(a.createdAt)),
    [items],
  );

  return (
    <div className="page">
      <div className="card">
        <div className="toolbar">
          <h1>Alerts</h1>
          <Link className="ghost-link" to="/dashboard">
            Back to Dashboard
          </Link>
        </div>

        <div className="filters-row">
          <button className={filter === "all" ? "chip active-chip" : "chip"} onClick={() => setFilter("all")} type="button">
            All
          </button>
          <button
            className={filter === "unresolved" ? "chip active-chip" : "chip"}
            onClick={() => setFilter("unresolved")}
            type="button"
          >
            Unresolved
          </button>
          <button className={filter === "resolved" ? "chip active-chip" : "chip"} onClick={() => setFilter("resolved")} type="button">
            Resolved
          </button>
        </div>

        <label className="toggle-row">
          <input
            type="checkbox"
            checked={autoRefresh}
            onChange={(event) => setAutoRefresh(event.target.checked)}
          />
          Auto-refresh every 15 seconds
        </label>

        {error ? <Notice tone="error" text={error} /> : null}
        {loading ? <p>Loading alerts...</p> : null}

        {!loading && sortedItems.length === 0 ? (
          <div className="empty-panel">No alerts found for selected filter.</div>
        ) : null}

        {!loading && sortedItems.length > 0 ? (
          <div className="endpoint-table-wrap">
            <table className="endpoint-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Endpoint</th>
                  <th>Message</th>
                  <th>Status</th>
                  <th>Created</th>
                </tr>
              </thead>
              <tbody>
                {sortedItems.map((alert) => (
                  <tr key={alert.id}>
                    <td>{alert.id}</td>
                    <td>{alert.endpointId}</td>
                    <td>{alert.message}</td>
                    <td>
                      <span className={alert.isResolved ? "pill active" : "pill inactive"}>
                        {alert.isResolved ? "Resolved" : "Unresolved"}
                      </span>
                    </td>
                    <td>{new Date(alert.createdAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}
      </div>
    </div>
  );
}
