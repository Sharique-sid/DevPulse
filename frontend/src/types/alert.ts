export type AlertItem = {
  id: number;
  endpointId: number;
  message: string;
  isResolved: boolean;
  createdAt: string;
  lastTriggeredAt: string;
};

export type AlertFilter = "all" | "unresolved" | "resolved";
