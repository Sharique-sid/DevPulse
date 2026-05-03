export type DashboardResponse = {
  totalEndpoints: number;
  activeEndpoints: number;
  uptimePercentage: number;
  avgResponseTimeMs: number;
  totalPinged: number;
  successfulPings: number;
  failedPings: number;
};
