import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Logo from '../components/common/Logo';
import styles from './auth.module.css';

/**
 * LoginPage
 *
 * Connects to: POST /api/auth/login  (Spring Boot AuthController)
 * On success: stores JWT in localStorage, redirects to "/"
 */
export default function LoginPage() {
  const navigate = useNavigate();

  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    setError('');
  }

  async function handleSubmit() {
    if (!form.email || !form.password) {
      setError('Please fill in all fields.');
      return;
    }

    setLoading(true);
    try {
      // TODO: swap URL for your Spring Boot base URL / env variable
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      });

      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        setError(data.message || 'Invalid email or password.');
        return;
      }

      const data = await res.json();
      localStorage.setItem('yap_token', data.token);
      navigate('/');
    } catch {
      setError('Something went wrong. Please try again.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <div className={styles.logoArea}>
            <Logo useSvg size="36px" />
            <p className={styles.tagline}>a space for women to speak freely</p>
        </div>

        <h1 className={styles.heading}>Welcome back</h1>
        <p className={styles.subheading}>Sign in to your account</p>

        <div className={styles.field}>
          <label className={styles.label} htmlFor="email">Email</label>
          <input
            id="email"
            name="email"
            type="email"
            className={styles.input}
            placeholder="you@example.com"
            value={form.email}
            onChange={handleChange}
            autoComplete="email"
          />
        </div>

        <div className={styles.field}>
          <label className={styles.label} htmlFor="password">Password</label>
          <input
            id="password"
            name="password"
            type="password"
            className={styles.input}
            placeholder="••••••••"
            value={form.password}
            onChange={handleChange}
            autoComplete="current-password"
          />
        </div>

        {error && <p className={styles.error}>{error}</p>}

        <button
          className={styles.submitBtn}
          onClick={handleSubmit}
          disabled={loading}
        >
          {loading ? 'Signing in…' : 'Sign In'}
        </button>

        <p className={styles.footerText}>
          Don&apos;t have an account?{' '}
          <Link to="/register" className={styles.link}>
            Join Yap
          </Link>
        </p>
      </div>
    </div>
  );
}
