import { api } from "./api";
import type { AlertFilter, AlertItem } from "../types/alert";

export async function fetchAlerts(filter: AlertFilter = "all") {
  const params = filter === "all" ? {} : { status: filter };
  const { data } = await api.get<AlertItem[]>("/alerts", { params });
  return data;
}
