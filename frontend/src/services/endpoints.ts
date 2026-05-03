import { api } from "./api";
import type { CreateEndpointRequest, Endpoint, UpdateEndpointRequest } from "../types/endpoint";

export async function fetchEndpoints() {
  const { data } = await api.get<Endpoint[]>("/endpoints");
  return data;
}

export async function createEndpoint(payload: CreateEndpointRequest) {
  const { data } = await api.post<Endpoint>("/endpoints", payload);
  return data;
}

export async function updateEndpoint(id: number, payload: UpdateEndpointRequest) {
  const { data } = await api.put<Endpoint>(`/endpoints/${id}`, payload);
  return data;
}

export async function deleteEndpoint(id: number) {
  await api.delete(`/endpoints/${id}`);
}
