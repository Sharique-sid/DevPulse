import { createContext, useContext, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { api } from "../services/api";
import type { AuthResponse, LoginRequest, RegisterRequest } from "../types/auth";

type AuthContextValue = {
  token: string | null;
  isAuthenticated: boolean;
  login: (payload: LoginRequest) => Promise<void>;
  register: (payload: RegisterRequest) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(localStorage.getItem("devpulse_token"));

  const login = async (payload: LoginRequest) => {
    const { data } = await api.post<AuthResponse>("/auth/login", payload);
    localStorage.setItem("devpulse_token", data.token);
    setToken(data.token);
  };

  const register = async (payload: RegisterRequest) => {
    const { data } = await api.post<AuthResponse>("/auth/register", payload);
    localStorage.setItem("devpulse_token", data.token);
    setToken(data.token);
  };

  const logout = () => {
    localStorage.removeItem("devpulse_token");
    setToken(null);
  };

  const value = useMemo(
    () => ({
      token,
      isAuthenticated: Boolean(token),
      login,
      register,
      logout,
    }),
    [token],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return context;
}
