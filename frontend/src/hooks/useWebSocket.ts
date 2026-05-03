import { useEffect, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";
import type { IMessage } from "@stomp/stompjs";
import { useAuth } from "../context/AuthContext";

function getOrgIdFromToken(token: string): string | null {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.org_id ? String(payload.org_id) : null;
  } catch {
    return null;
  }
}

export function useWebSocket<T>(topicSuffix: string, onMessage: (data: T) => void) {
  const { token } = useAuth();
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!token) return;

    const orgId = getOrgIdFromToken(token);
    if (!orgId) return;

    const apiBase = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8082";
    const wsUrl = apiBase.replace(/^http/, "ws") + "/ws-endpoint";
    const topic = `/topic/org/${orgId}/${topicSuffix}`;

    const client = new Client({
      brokerURL: wsUrl,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      debug: function () {
        // Uncomment for debug logs
        // console.log(str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      setConnected(true);
      client.subscribe(topic, (message: IMessage) => {
        if (message.body) {
          const body = JSON.parse(message.body);
          onMessage(body);
        }
      });
    };

    client.onStompError = (frame) => {
      console.error("Broker reported error: " + frame.headers["message"]);
      console.error("Additional details: " + frame.body);
    };

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      setConnected(false);
    };
  }, [token, topicSuffix]); // Exclude onMessage from deps to avoid reconnecting, assuming onMessage is stable or handled correctly

  return { connected };
}
