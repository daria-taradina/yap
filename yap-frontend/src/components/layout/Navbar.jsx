import { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import Logo from '../common/Logo';
import { useTheme } from '../../hooks/useTheme';
import styles from './Navbar.module.css';

const DISCOVER_LINKS = [
  { to: '/',        label: 'Home',    icon: 'ti ti-home'      },
  { to: '/explore', label: 'Explore', icon: 'ti ti-sparkles'  },
  { to: '/forums',  label: 'Forums',  icon: 'ti ti-message-2' },
  { to: '/blogs',   label: 'Blogs',   icon: 'ti ti-notebook'  },
];

// TODO: pull from user context / API
const MY_SPACES = [
  { name: 'WomenInTech', color: '#C4973F' },
  { name: 'Friendship',  color: '#6B8BAD' },
  { name: 'SelfCare',    color: '#7A9E7E' },
  { name: 'BookClub',    color: '#A07AB5' },
  { name: 'CareerTalk',  color: '#7A9E7E' },
  { name: 'MindfulLiving', color: '#6B8BAD' },
];

const SPACES_VISIBLE_DEFAULT = 3;

export default function Navbar() {
  const navigate = useNavigate();
  const [showAll, setShowAll] = useState(false);
  const [expanded, setExpanded] = useState(false);
  const { theme, toggleTheme } = useTheme();

  const visibleSpaces = showAll ? MY_SPACES : MY_SPACES.slice(0, SPACES_VISIBLE_DEFAULT);
  const hiddenCount = MY_SPACES.length - SPACES_VISIBLE_DEFAULT;

  function pick(type) {
    setExpanded(false);
    navigate(`/create-post/${type}`);
  }

  return (
    <nav className={styles.navbar} aria-label="Main navigation">
      <div className={styles.logoArea}>
        <Logo theme={theme} />
        <button
          className={styles.themeToggle}
          onClick={toggleTheme}
          aria-label={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
          title={theme === 'dark' ? 'Light mode' : 'Dark mode'}
        >
          <i className={theme === 'dark' ? 'ti ti-sun' : 'ti ti-moon'} aria-hidden="true" />
        </button>
      </div>

      <div className={styles.navSection}>
        <div className={styles.sectionLabel}>Discover</div>

        {DISCOVER_LINKS.map(({ to, label, icon }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) =>
              `${styles.navItem} ${isActive ? styles.active : ''}`
            }
          >
            <i className={`${icon} ${styles.navIcon}`} aria-hidden="true" />
            {label}
          </NavLink>
        ))}

        <div className={styles.sectionLabel}>My Spaces</div>

        {visibleSpaces.map(({ name, color }) => (
          <div
            key={name}
            className={styles.communityItem}
            onClick={() => navigate(`/w/${name}`)}
          >
            <span className={styles.communityDot} style={{ background: color }} />
            <span className={styles.communityName}>{name}</span>
          </div>
        ))}

        {MY_SPACES.length > SPACES_VISIBLE_DEFAULT && (
          <button
            className={styles.loadMoreBtn}
            onClick={() => setShowAll((v) => !v)}
          >
            {showAll ? 'Show less' : `+${hiddenCount} more`}
          </button>
        )}

        <button
          className={styles.createSpaceBtn}
          onClick={() => navigate('/create-space')}
        >
          <i className="ti ti-plus" aria-hidden="true" />
          Create Space
        </button>
      </div>

      {/* Write area — expands inline */}
      <div className={styles.writeArea}>
        {expanded && (
          <div className={styles.writeOptions}>
            <button
              className={styles.writeOption}
              onClick={() => pick('blog')}
            >
              <i className="ti ti-notebook" aria-hidden="true" />
              Blog Post
            </button>
            <button
              className={styles.writeOption}
              onClick={() => pick('discussion')}
            >
              <i className="ti ti-messages" aria-hidden="true" />
              Discussion
            </button>
          </div>
        )}

        <button
          className={`${styles.writeBtn} ${expanded ? styles.writeBtnActive : ''}`}
          onClick={() => setExpanded((v) => !v)}
        >
          {expanded ? '✕ Cancel' : '+ Write'}
        </button>
      </div>
    </nav>
  );
}
