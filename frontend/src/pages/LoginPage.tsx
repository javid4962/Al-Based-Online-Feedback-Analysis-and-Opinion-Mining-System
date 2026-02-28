import { FormEvent, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { login } from '../api/services';
import { setAuthSession } from '../auth/token';

type RedirectState = {
  from?: {
    pathname?: string;
  };
};

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const redirect = (location.state as RedirectState | null)?.from?.pathname ?? '/dashboard';

  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('ChangeMeNow123!');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      const response = await login({ username, password });
      setAuthSession(response.accessToken, response.expiresAt);
      navigate(redirect, { replace: true });
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : 'Login failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <p className="brand-kicker">Admin Access</p>
        <h1>Feedback Intelligence Console</h1>
        <p className="supporting">
          Sign in to manage ingestion, run model retraining, and monitor sentiment operations.
        </p>
        <form onSubmit={onSubmit} className="form-grid">
          <label className="field">
            <span>Username</span>
            <input value={username} onChange={(e) => setUsername(e.target.value)} required />
          </label>
          <label className="field">
            <span>Password</span>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </label>
          {error ? <p className="error">{error}</p> : null}
          <button className="btn btn-primary" type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Signing in...' : 'Login'}
          </button>
        </form>
      </div>
    </div>
  );
}
