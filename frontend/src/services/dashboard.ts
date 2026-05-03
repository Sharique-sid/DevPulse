import { api } from "./api";
import type { DashboardResponse } from "../types/dashboard";

export async function fetchDashboard() {
  const { data } = await api.get<DashboardResponse>("/dashboard");
  return data;
}
