import { useState, useEffect } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import Logo from '../common/Logo';
import { useTheme } from '../../hooks/useTheme';
import { api } from '../../api';
import styles from './Navbar.module.css';

const EXPLORE_LINKS = [
  { to: '/',        label: 'Home',    icon: 'ti ti-home'      },
  { to: '/explore', label: 'Explore', icon: 'ti ti-sparkles'  },
];

const SPACE_COLORS = ['#C4973F', '#6B8BAD', '#7A9E7E', '#A07AB5', '#9E7A7A'];
const SPACES_VISIBLE_DEFAULT = 3;

export default function Navbar() {
  const navigate = useNavigate();
  const [showAll, setShowAll] = useState(false);
  const { theme, toggleTheme } = useTheme();
  const [mySpaces, setMySpaces] = useState([]);
  const currentUser = JSON.parse(localStorage.getItem('yap_user') || '{}');

  useEffect(() => {
    api.getMySpaces()
      .then((res) => res.json())
      .then((data) => setMySpaces(Array.isArray(data) ? data : []))
      .catch(() => setMySpaces([]));
  }, []);

  const visibleSpaces = showAll ? mySpaces : mySpaces.slice(0, SPACES_VISIBLE_DEFAULT);
  const hiddenCount = mySpaces.length - SPACES_VISIBLE_DEFAULT;

  function handleLogout() {
    localStorage.removeItem('yap_token');
    localStorage.removeItem('yap_user');
    navigate('/login');
  }

  return (
    <nav className={styles.navbar} aria-label="Main navigation">
      <div className={styles.logoArea}>
        <Logo theme={theme} />
        <button
          className={styles.themeToggle}
          onClick={toggleTheme}
          aria-label={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
        >
          <i className={theme === 'dark' ? 'ti ti-sun' : 'ti ti-moon'} aria-hidden="true" />
        </button>
      </div>

      <div className={styles.navSection}>
        <div className={styles.sectionLabel}>Explore</div>
        {EXPLORE_LINKS.map(({ to, label, icon }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) => `${styles.navItem} ${isActive ? styles.active : ''}`}
          >
            <i className={`${icon} ${styles.navIcon}`} aria-hidden="true" />
            {label}
          </NavLink>
        ))}

        <div className={styles.sectionLabel}>My Spaces</div>
        {visibleSpaces.map((space, i) => (
          <div
            key={space.communityId}
            className={styles.communityItem}
            onClick={() => navigate(`/w/${space.name}`)}
          >
            <span className={styles.communityDot} style={{ background: SPACE_COLORS[i % SPACE_COLORS.length] }} />
            <span className={styles.communityName}>w/{space.name}</span>
          </div>
        ))}

        {mySpaces.length > SPACES_VISIBLE_DEFAULT && (
          <button className={styles.loadMoreBtn} onClick={() => setShowAll((v) => !v)}>
            {showAll ? 'Show less' : `+${hiddenCount} more`}
          </button>
        )}

        <button className={styles.createSpaceBtn} onClick={() => navigate('/create-space')}>
          <i className="ti ti-plus" aria-hidden="true" />
          Create Space
        </button>
      </div>

      <div className={styles.writeArea}>
        <button
          className={styles.writeBtn}
          onClick={() => navigate('/create-post/discussion')}
        >
          + New Discussion
        </button>
      </div>

      
    </nav>
  );
}