import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [organisationName, setOrganisationName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    try {
      await register({ name, organisationName, email, password });
      navigate("/dashboard");
    } catch {
      setError("Registration failed. Please check your details.");
    }
  };

  return (
    <div className="page auth-page">
      <div className="card auth-card">
        <h1>Create Account</h1>
        <p>Start tracking uptime in minutes.</p>
        <form onSubmit={handleSubmit} className="form-grid">
          <label>
            Full Name
            <input
              type="text"
              value={name}
              onChange={(event) => setName(event.target.value)}
              required
            />
          </label>
          <label>
            Organisation Name
            <input
              type="text"
              value={organisationName}
              onChange={(event) => setOrganisationName(event.target.value)}
              required
            />
          </label>
          <label>
            Email
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </label>
          {error ? <div className="error-text">{error}</div> : null}
          <button type="submit">Create Account</button>
        </form>
        <p className="auth-switch">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
