import { NavLink, useNavigate } from 'react-router-dom';
import styles from './BottomNav.module.css';

export default function BottomNav() {
  const navigate = useNavigate();
  const currentUser = JSON.parse(localStorage.getItem('yap_user') || '{}');

  function handleCreateClick(e) {
    e.preventDefault();
    if (!currentUser.username) {
      navigate('/login');
    } else {
      navigate('/create-post/discussion');
    }
  }

  function handleProfileClick(e) {
    e.preventDefault();
    if (!currentUser.username) {
      navigate('/login');
    } else {
      navigate(`/blog/${currentUser.username}`);
    }
  }

  return (
    <nav className={styles.bottomNav} aria-label="Mobile navigation">
      <NavLink
        to="/"
        end
        className={({ isActive }) =>
          `${styles.navItem} ${isActive ? styles.active : ''}`
        }
        aria-label="Home"
      >
        <svg
          className={styles.icon}
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
          <polyline points="9 22 9 12 15 12 15 22" />
        </svg>
        <span className={styles.label}>Home</span>
      </NavLink>

      <NavLink
        to="/search"
        className={({ isActive }) =>
          `${styles.navItem} ${isActive ? styles.active : ''}`
        }
        aria-label="Search"
      >
        <svg
          className={styles.icon}
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <circle cx="11" cy="11" r="8" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
        <span className={styles.label}>Search</span>
      </NavLink>

      <a
        href="/create-post/discussion"
        className={styles.navItem}
        onClick={handleCreateClick}
        aria-label="Create"
      >
        <svg
          className={styles.icon}
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <line x1="12" y1="5" x2="12" y2="19" />
          <line x1="5" y1="12" x2="19" y2="12" />
        </svg>
        <span className={styles.label}>Create</span>
      </a>

      <a
        href="#"
        className={styles.navItem}
        onClick={handleProfileClick}
        aria-label="Profile"
      >
        <svg
          className={styles.icon}
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
          <circle cx="12" cy="7" r="4" />
        </svg>
        <span className={styles.label}>Profile</span>
      </a>
    </nav>
  );
}
