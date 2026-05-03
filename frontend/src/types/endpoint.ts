export type Endpoint = {
  id: number;
  name: string;
  url: string;
  method: string;
  checkIntervalMinutes: number;
  isActive: boolean;
  expectedKeyword?: string;
  createdAt: string;
  updatedAt: string;
};

export type CreateEndpointRequest = {
  name: string;
  url: string;
  method: "GET" | "POST" | "PUT" | "DELETE" | "PATCH" | "HEAD" | "OPTIONS";
  checkIntervalMinutes: number;
  expectedKeyword?: string;
};

export type UpdateEndpointRequest = Partial<
  Pick<Endpoint, "name" | "url" | "method" | "checkIntervalMinutes" | "isActive" | "expectedKeyword">
>;
