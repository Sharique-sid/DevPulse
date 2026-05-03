import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { AxiosError } from "axios";
import { createEndpoint, deleteEndpoint, fetchEndpoints, updateEndpoint } from "../services/endpoints";
import type { CreateEndpointRequest, Endpoint } from "../types/endpoint";
import { Notice } from "../components/Notice";

const methods: CreateEndpointRequest["method"][] = [
  "GET",
  "POST",
  "PUT",
  "DELETE",
  "PATCH",
  "HEAD",
  "OPTIONS",
];

function getErrorMessage(error: unknown) {
  if (error instanceof AxiosError && error.response?.data?.message) {
    return String(error.response.data.message);
  }
  return "Request failed. Please try again.";
}

export function EndpointsPage() {
  const [items, setItems] = useState<Endpoint[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [editing, setEditing] = useState<Endpoint | null>(null);
  const [form, setForm] = useState<CreateEndpointRequest>({
    name: "",
    url: "",
    method: "GET",
    checkIntervalMinutes: 5,
    expectedKeyword: "",
  });

  async function load() {
    setLoading(true);
    setError("");
    setSuccess("");
    try {
      const data = await fetchEndpoints();
      setItems(data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  const sortedItems = useMemo(
    () => [...items].sort((a, b) => Number(b.id) - Number(a.id)),
    [items],
  );

  async function handleCreate(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setSuccess("");
    try {
      const created = await createEndpoint(form);
      setItems((prev) => [created, ...prev]);
      setForm({ name: "", url: "", method: "GET", checkIntervalMinutes: 5, expectedKeyword: "" });
      setSuccess("Endpoint added successfully.");
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(id: number) {
    setError("");
    setSuccess("");
    try {
      await deleteEndpoint(id);
      setItems((prev) => prev.filter((item) => item.id !== id));
      setSuccess("Endpoint deleted successfully.");
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleToggleActive(item: Endpoint) {
    setError("");
    setSuccess("");
    try {
      const updated = await updateEndpoint(item.id, { isActive: !item.isActive });
      setItems((prev) => prev.map((row) => (row.id === item.id ? updated : row)));
      setSuccess(`Endpoint ${updated.isActive ? "resumed" : "paused"} successfully.`);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  }

  async function handleEditSave(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!editing) {
      return;
    }

    setError("");
    setSuccess("");
    setSaving(true);

    try {
      const updated = await updateEndpoint(editing.id, {
        name: editing.name,
        url: editing.url,
        method: editing.method,
        checkIntervalMinutes: editing.checkIntervalMinutes,
        expectedKeyword: editing.expectedKeyword,
      });
      setItems((prev) => prev.map((item) => (item.id === updated.id ? updated : item)));
      setEditing(null);
      setSuccess("Endpoint updated successfully.");
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="page">
      <div className="card">
        <div className="toolbar">
          <h1>Endpoint Management</h1>
          <Link className="ghost-link" to="/dashboard">
            Back to Dashboard
          </Link>
        </div>

        <p>Add endpoints and control whether they are actively monitored.</p>

        <form className="endpoint-form" onSubmit={handleCreate}>
          <input
            placeholder="Endpoint name"
            value={form.name}
            onChange={(event) => setForm((prev) => ({ ...prev, name: event.target.value }))}
            required
          />
          <input
            placeholder="https://example.com/health"
            type="url"
            value={form.url}
            onChange={(event) => setForm((prev) => ({ ...prev, url: event.target.value }))}
            required
          />
          <select
            value={form.method}
            onChange={(event) =>
              setForm((prev) => ({ ...prev, method: event.target.value as CreateEndpointRequest["method"] }))
            }
          >
            {methods.map((method) => (
              <option key={method} value={method}>
                {method}
              </option>
            ))}
          </select>
          <input
            type="number"
            min={1}
            value={form.checkIntervalMinutes}
            onChange={(event) =>
              setForm((prev) => ({ ...prev, checkIntervalMinutes: Number(event.target.value) || 1 }))
            }
            required
          />
          <input
            placeholder="Expected Keyword (Optional)"
            value={form.expectedKeyword}
            onChange={(event) => setForm((prev) => ({ ...prev, expectedKeyword: event.target.value }))}
          />
          <button type="submit" disabled={saving}>
            {saving ? "Saving..." : "Add Endpoint"}
          </button>
        </form>

        {error ? <Notice tone="error" text={error} /> : null}
        {success ? <Notice tone="success" text={success} /> : null}

        {loading ? (
          <p>Loading endpoints...</p>
        ) : sortedItems.length === 0 ? (
          <div className="empty-panel">No endpoints yet. Add your first endpoint above.</div>
        ) : (
          <div className="endpoint-table-wrap">
            <table className="endpoint-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>URL</th>
                  <th>Method</th>
                  <th>Interval</th>
                  <th>Keyword</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {sortedItems.map((item) => (
                  <tr key={item.id}>
                    <td>{item.name}</td>
                    <td className="url-cell">{item.url}</td>
                    <td>{item.method}</td>
                    <td>{item.checkIntervalMinutes}m</td>
                    <td>{item.expectedKeyword || "-"}</td>
                    <td>
                      <span className={item.isActive ? "pill active" : "pill inactive"}>
                        {item.isActive ? "Active" : "Paused"}
                      </span>
                    </td>
                    <td className="actions-cell">
                      <button
                        type="button"
                        className="ghost-btn"
                        onClick={() => setEditing(item)}
                      >
                        Edit
                      </button>
                      <button
                        type="button"
                        className="ghost-btn"
                        onClick={() => handleToggleActive(item)}
                      >
                        {item.isActive ? "Pause" : "Resume"}
                      </button>
                      <button
                        type="button"
                        className="danger-btn"
                        onClick={() => handleDelete(item.id)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {editing ? (
          <div className="modal-overlay" role="dialog" aria-modal="true">
            <div className="modal-card">
              <h2>Edit Endpoint</h2>
              <form className="form-grid" onSubmit={handleEditSave}>
                <label>
                  Name
                  <input
                    value={editing.name}
                    onChange={(event) => setEditing((prev) => (prev ? { ...prev, name: event.target.value } : prev))}
                    required
                  />
                </label>
                <label>
                  URL
                  <input
                    type="url"
                    value={editing.url}
                    onChange={(event) => setEditing((prev) => (prev ? { ...prev, url: event.target.value } : prev))}
                    required
                  />
                </label>
                <label>
                  Method
                  <select
                    value={editing.method}
                    onChange={(event) =>
                      setEditing((prev) =>
                        prev ? { ...prev, method: event.target.value as CreateEndpointRequest["method"] } : prev,
                      )
                    }
                  >
                    {methods.map((method) => (
                      <option key={method} value={method}>
                        {method}
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  Check Interval (minutes)
                  <input
                    type="number"
                    min={1}
                    value={editing.checkIntervalMinutes}
                    onChange={(event) =>
                      setEditing((prev) =>
                        prev ? { ...prev, checkIntervalMinutes: Number(event.target.value) || 1 } : prev,
                      )
                    }
                    required
                  />
                </label>
                <label>
                  Expected Keyword (Optional)
                  <input
                    value={editing.expectedKeyword || ""}
                    onChange={(event) =>
                      setEditing((prev) =>
                        prev ? { ...prev, expectedKeyword: event.target.value } : prev,
                      )
                    }
                  />
                </label>
                <div className="toolbar-actions">
                  <button type="submit" disabled={saving}>
                    {saving ? "Saving..." : "Save Changes"}
                  </button>
                  <button type="button" className="ghost-btn" onClick={() => setEditing(null)}>
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          </div>
        ) : null}
      </div>
    </div>
  );
}
