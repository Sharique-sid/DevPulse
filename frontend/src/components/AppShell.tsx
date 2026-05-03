import { Link, Outlet, useLocation } from "react-router-dom";
import { useState } from "react";
import { useWebSocket } from "../hooks/useWebSocket";

const labels: Record<string, string> = {
  dashboard: "Dashboard",
  endpoints: "Endpoints",
  alerts: "Alerts",
  insights: "AI Insights",
};

export function AppShell() {
  const location = useLocation();
  const parts = location.pathname.split("/").filter(Boolean);
  const current = parts[parts.length - 1] ?? "dashboard";
  
  const [hasNewAlert, setHasNewAlert] = useState(false);

  useWebSocket("alerts", (alert: any) => {
    if (!alert.isResolved) {
      setHasNewAlert(true);
    }
  });

  return (
    <div>
      <header className="top-nav">
        <div className="brand">DevPulse</div>
        <nav className="top-links">
          <Link to="/dashboard">Dashboard</Link>
          <Link to="/endpoints">Endpoints</Link>
          <Link 
            to="/alerts" 
            onClick={() => setHasNewAlert(false)}
            style={{ position: "relative" }}
          >
            Alerts
            {hasNewAlert && (
              <span className="glowing-beam">🔔</span>
            )}
          </Link>
          <Link to="/insights">AI Insights</Link>
        </nav>
      </header>
      <div className="crumbs">Home / {labels[current] ?? current}</div>
      <Outlet />
    </div>
  );
}
