export type AuthResponse = {
  token: string;
  userId: number;
  email: string;
  organisationId: number;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type RegisterRequest = {
  name: string;
  email: string;
  password: string;
  organisationName: string;
};
