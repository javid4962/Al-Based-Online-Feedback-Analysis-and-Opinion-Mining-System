const TOKEN_KEY = 'feedback_token';
const TOKEN_EXPIRES_KEY = 'feedback_token_expires';

export function setAuthSession(token: string, expiresAt: string) {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(TOKEN_EXPIRES_KEY, expiresAt);
}

export function getToken(): string | null {
  const token = localStorage.getItem(TOKEN_KEY);
  const expiresAt = getTokenExpiry();
  if (!token || !expiresAt) {
    clearToken();
    return null;
  }

  if (Date.parse(expiresAt) <= Date.now()) {
    clearToken();
    return null;
  }
  return token;
}

export function getTokenExpiry(): string | null {
  return localStorage.getItem(TOKEN_EXPIRES_KEY);
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(TOKEN_EXPIRES_KEY);
}

export function isAuthenticated(): boolean {
  return Boolean(getToken());
}
