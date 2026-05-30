import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Logo from '../components/common/Logo';
import styles from './auth.module.css';

/**
 * RegisterPage
 *
 * Connects to: POST /api/auth/register  (Spring Boot AuthController)
 * On success: stores JWT in localStorage, redirects to "/"
 */
export default function RegisterPage() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    setError('');
  }

  async function handleSubmit() {
    if (!form.username || !form.email || !form.password || !form.confirmPassword) {
      setError('Please fill in all fields.');
      return;
    }
    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }
    if (form.password.length < 6) {
      setError('Password must be at least 6 characters.');
      return;
    }

    setLoading(true);
    try {
      // TODO: swap URL for your Spring Boot base URL / env variable
      const res = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: form.username,
          email: form.email,
          password: form.password,
        }),
      });

      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        setError(data.message || 'Registration failed. Please try again.');
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

        <h1 className={styles.heading}>Create your account</h1>
        <p className={styles.subheading}>Join the conversation</p>

        <div className={styles.field}>
          <label className={styles.label} htmlFor="username">Username</label>
          <input
            id="username"
            name="username"
            type="text"
            className={styles.input}
            placeholder="yourhandle"
            value={form.username}
            onChange={handleChange}
            autoComplete="username"
          />
        </div>

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
            placeholder="Min. 6 characters"
            value={form.password}
            onChange={handleChange}
            autoComplete="new-password"
          />
        </div>

        <div className={styles.field}>
          <label className={styles.label} htmlFor="confirmPassword">Confirm Password</label>
          <input
            id="confirmPassword"
            name="confirmPassword"
            type="password"
            className={styles.input}
            placeholder="••••••••"
            value={form.confirmPassword}
            onChange={handleChange}
            autoComplete="new-password"
          />
        </div>

        {error && <p className={styles.error}>{error}</p>}

        <button
          className={styles.submitBtn}
          onClick={handleSubmit}
          disabled={loading}
        >
          {loading ? 'Creating account…' : 'Join Yap'}
        </button>

        <p className={styles.footerText}>
          Already have an account?{' '}
          <Link to="/login" className={styles.link}>
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
