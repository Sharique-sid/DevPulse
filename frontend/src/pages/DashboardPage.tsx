import { useAuth } from "../context/AuthContext";
import { Link } from "react-router-dom";
import { useEffect, useMemo, useState } from "react";
import { AxiosError } from "axios";
import { fetchDashboard } from "../services/dashboard";
import type { DashboardResponse } from "../types/dashboard";
import { useWebSocket } from "../hooks/useWebSocket";
import { ResponsiveContainer, LineChart, CartesianGrid, XAxis, YAxis, Tooltip, Line } from "recharts";

const initialData: DashboardResponse = {
  totalEndpoints: 0,
  activeEndpoints: 0,
  uptimePercentage: 0,
  avgResponseTimeMs: 0,
  totalPinged: 0,
  successfulPings: 0,
  failedPings: 0,
};

function getErrorMessage(error: unknown) {
  if (error instanceof AxiosError && error.response?.data?.message) {
    return String(error.response.data.message);
  }
  return "Failed to load dashboard data.";
}

export function DashboardPage() {
  const { logout } = useAuth();
  const [data, setData] = useState<DashboardResponse>(initialData);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [recentPings, setRecentPings] = useState<any[]>([]);

  // Connect to the WebSocket topic for real-time pings
  const { connected } = useWebSocket("pings", (ping: any) => {
    // Add the new ping to the feed (keep last 20 for the chart)
    setRecentPings((prev) => {
      // Create a formatted time for the chart X-axis
      const formattedPing = {
        ...ping,
        timeLabel: new Date(ping.checkedAt).toLocaleTimeString([], { hour12: false }),
      };
      return [formattedPing, ...prev].slice(0, 20);
    });
    // Refresh the aggregated stats
    loadDashboard(false);
  });

  async function loadDashboard(showLoading = true) {
    setError("");
    if (showLoading) setLoading(true);
    try {
      const response = await fetchDashboard();
      setData(response);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      if (showLoading) setLoading(false);
    }
  }

  useEffect(() => {
    loadDashboard();
  }, []);

  const uptimeLabel = useMemo(() => `${data.uptimePercentage.toFixed(2)}%`, [data.uptimePercentage]);
  const avgResponseLabel = useMemo(() => `${data.avgResponseTimeMs.toFixed(2)} ms`, [data.avgResponseTimeMs]);

  // Data for Recharts (needs to be oldest-to-newest, so reverse the recentPings array)
  const chartData = useMemo(() => [...recentPings].reverse(), [recentPings]);

  return (
    <div className="page">
      <div className="card">
        <div className="toolbar">
          <h1>DevPulse Dashboard</h1>
          <div className="toolbar-actions">
            <button onClick={() => loadDashboard(true)} className="ghost-btn" type="button">
              Refresh
            </button>
            <button onClick={logout} className="ghost-btn" type="button">
              Logout
            </button>
          </div>
        </div>
        <p>Live metrics below are synchronized via WebSockets.</p>
        <div className="toggle-row">
          <span className={`status-dot ${connected ? "connected" : "disconnected"}`}></span>
          {connected ? "Live Updates Connected" : "Connecting to Live Updates..."}
        </div>
        <div className="quick-links">
          <Link className="ghost-link" to="/endpoints">
            Go to Endpoint Management
          </Link>
          <Link className="ghost-link" to="/alerts">
            View Alerts
          </Link>
          <Link className="ghost-link" to="/insights">
            Open AI Insights
          </Link>
        </div>
        {error ? <div className="error-text">{error}</div> : null}
        {loading ? <p>Loading dashboard...</p> : null}
        <div className="stats-grid">
          <article className="stat-box">
            <h3>Endpoints</h3>
            <strong>{data.totalEndpoints}</strong>
          </article>
          <article className="stat-box">
            <h3>Uptime</h3>
            <strong>{uptimeLabel}</strong>
          </article>
          <article className="stat-box">
            <h3>Avg Response</h3>
            <strong>{avgResponseLabel}</strong>
          </article>
          <article className="stat-box">
            <h3>Active Endpoints</h3>
            <strong>{data.activeEndpoints}</strong>
          </article>
          <article className="stat-box">
            <h3>Total Pings</h3>
            <strong>{data.totalPinged}</strong>
          </article>
          <article className="stat-box">
            <h3>Failed Pings</h3>
            <strong>{data.failedPings}</strong>
          </article>
        </div>

        {recentPings.length > 0 && (
          <div className="recent-pings">
            <h3>Live Performance Chart</h3>
            <div style={{ width: "100%", height: 300, marginTop: "16px" }}>
              <ResponsiveContainer>
                <LineChart data={chartData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e0e0e0" />
                  <XAxis dataKey="timeLabel" tick={{ fontSize: 12, fill: "#888" }} />
                  <YAxis 
                    tick={{ fontSize: 12, fill: "#888" }} 
                    label={{ value: "Response (ms)", angle: -90, position: "insideLeft", fontSize: 12, fill: "#888" }} 
                  />
                  <Tooltip 
                    contentStyle={{ borderRadius: '8px', border: '1px solid #eee', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
                    labelStyle={{ fontWeight: 'bold', color: '#333' }}
                  />
                  <Line 
                    type="monotone" 
                    dataKey="responseTimeMs" 
                    stroke="#1f8a4c" 
                    strokeWidth={3}
                    dot={{ r: 4, strokeWidth: 2 }}
                    activeDot={{ r: 6, strokeWidth: 0 }}
                    animationDuration={500}
                  />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
