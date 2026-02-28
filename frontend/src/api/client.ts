import { clearToken, getToken } from '../auth/token';

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080';

function redirectToLogin() {
  if (window.location.pathname !== '/login') {
    window.location.replace('/login');
  }
}

export async function apiFetch<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers = new Headers(init.headers ?? {});

  if (!headers.has('Content-Type') && !(init.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  let response: Response;
  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...init,
      headers,
    });
  } catch (error) {
    if (error instanceof TypeError) {
      throw new Error(`Cannot reach backend API at ${API_BASE}. Ensure backend is running.`);
    }
    throw error;
  }

  if (response.status === 401 || response.status === 403) {
    clearToken();
    redirectToLogin();
  }

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `HTTP ${response.status}`);
  }

  if (response.status === 204) {
    return null as T;
  }

  return (await response.json()) as T;
}
