import { ReactNode } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { clearToken, getTokenExpiry } from '../auth/token';

const navItems = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/feedback', label: 'Feedback Explorer' },
  { to: '/ingestion', label: 'Ingestion' },
  { to: '/models', label: 'Model Center' },
  { to: '/jobs', label: 'Jobs Monitor' },
];

type AppShellProps = {
  children: ReactNode;
};

export function AppShell({ children }: AppShellProps) {
  const navigate = useNavigate();
  const expiresAt = getTokenExpiry();

  const handleLogout = () => {
    clearToken();
    navigate('/login', { replace: true });
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand-block">
          <p className="brand-kicker">AI Feedback Platform</p>
          <h1>Opinion Intelligence</h1>
        </div>
        <nav className="nav-menu">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <main className="workspace">
        <header className="workspace-header">
          <div>
            <p className="meta-label">Session</p>
            <p className="meta-value">
              Expires {expiresAt ? new Date(expiresAt).toLocaleString() : 'unknown'}
            </p>
          </div>
          <button type="button" className="btn btn-ghost" onClick={handleLogout}>
            Logout
          </button>
        </header>
        <section className="workspace-content">{children}</section>
      </main>
    </div>
  );
}
