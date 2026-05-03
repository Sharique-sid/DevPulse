import { api } from "./api";
import type { Insight } from "../types/insight";

export async function fetchInsights() {
  const { data } = await api.get<Insight[]>("/ai-insights");
  return data;
}

export async function fetchLatestInsight() {
  const { data } = await api.get<Insight>("/ai-insights/latest");
  return data;
}

export async function generateInsight() {
  const { data } = await api.post<Insight>("/ai-insights/generate");
  return data;
}
