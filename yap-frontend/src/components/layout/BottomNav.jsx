import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import styles from './BottomNav.module.css';

export default function BottomNav() {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const currentUser = JSON.parse(localStorage.getItem('yap_user') || '{}');

  function handleProfileClick(e) {
    e.preventDefault();
    if (!currentUser.username) {
      navigate('/login');
    } else {
      navigate(`/@${currentUser.username}`);
    }
  }

  const isProfileActive =
    pathname === '/login' ||
    (currentUser.username && pathname === `/@${currentUser.username}`);

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
        to="/explore"
        className={({ isActive }) =>
          `${styles.navItem} ${isActive ? styles.active : ''}`
        }
        aria-label="Explore"
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
        <span className={styles.label}>Explore</span>
      </NavLink>

      <a
        href="#"
        className={`${styles.navItem} ${isProfileActive ? styles.active : ''}`}
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
