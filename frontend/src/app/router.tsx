import { Navigate, createBrowserRouter } from "react-router-dom";
import { AppShell } from "../components/AppShell";
import { ProtectedRoute } from "../components/ProtectedRoute";
import { DashboardPage } from "../pages/DashboardPage";
import { EndpointsPage } from "../pages/EndpointsPage";
import { AlertsPage } from "../pages/AlertsPage";
import { InsightsPage } from "../pages/InsightsPage";
import { LoginPage } from "../pages/LoginPage";
import { RegisterPage } from "../pages/RegisterPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <Navigate to="/dashboard" replace />,
  },
  {
    path: "/login",
    element: <LoginPage />,
  },
  {
    path: "/register",
    element: <RegisterPage />,
  },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppShell />,
        children: [
          {
            path: "/dashboard",
            element: <DashboardPage />,
          },
          {
            path: "/endpoints",
            element: <EndpointsPage />,
          },
          {
            path: "/alerts",
            element: <AlertsPage />,
          },
          {
            path: "/insights",
            element: <InsightsPage />,
          },
        ],
      },
    ],
  },
]);
