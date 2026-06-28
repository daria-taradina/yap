import { useState, useEffect, useRef } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import Navbar from './Navbar';
import RightSidebar from './RightSidebar';
import BottomNav from './BottomNav';
import InstallPrompt from '../common/InstallPrompt';
import Logo from '../common/Logo';
import Avatar from '../common/Avatar';
import { api } from '../../api';
import styles from './AppLayout.module.css';

function useTopbarTitle(pathname) {
  if (pathname === '/') return 'Home';
  if (pathname.startsWith('/w/')) return 'Forum';
  if (pathname.startsWith('/@')) return 'Profile';
  if (pathname.startsWith('/explore')) return 'Explore';
  if (pathname.startsWith('/create-space')) return 'Create Space';
  if (pathname.startsWith('/create-post/discussion')) return 'New Discussion';
  if (pathname.startsWith('/post/')) return '';
  return '';
}

export default function AppLayout() {
  const { pathname } = useLocation();
  const navigate = useNavigate();
  const title = useTopbarTitle(pathname);
  const currentUser = JSON.parse(localStorage.getItem('yap_user') || '{}');

  const [searchQuery, setSearchQuery] = useState('');

  const [notifOpen, setNotifOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  const [userMenuOpen, setUserMenuOpen] = useState(false);

  const [hamburgerOpen, setHamburgerOpen] = useState(false);
  const [pendingCount, setPendingCount] = useState(0);

  const searchRef = useRef(null);
  const userMenuRef = useRef(null);
  const notifRef = useRef(null);
  const hamburgerRef = useRef(null);

  const isModOrAdmin = currentUser?.role === 'MOD' || currentUser?.role === 'ADMIN';

  useEffect(() => {
    api.getUnreadCount()
      .then(r => r?.json())
      .then(d => setUnreadCount(d?.count || 0))
      .catch(() => {});
  }, [pathname]);

  useEffect(() => {
    if (isModOrAdmin) {
      api.getModReportCount()
        .then(r => r?.json())
        .then(d => setPendingCount(d?.count || 0))
        .catch(() => {});
    }
  }, [pathname, isModOrAdmin]);

  useEffect(() => {
    if (notifOpen) {
      api.getNotifications()
        .then(r => r?.json())
        .then(d => {
          setNotifications(Array.isArray(d) ? d : []);
          setUnreadCount(0);
          api.markNotificationsRead().catch(() => {});
        })
        .catch(() => {});
    }
  }, [notifOpen]);

  // Close dropdowns on outside click
  useEffect(() => {
    function handleClick(e) {
      if (
        userMenuRef.current &&
        !userMenuRef.current.contains(e.target)
      ) {
        setUserMenuOpen(false);
      }

      if (
        notifRef.current &&
        !notifRef.current.contains(e.target)
      ) {
        setNotifOpen(false);
      }

      if (
        hamburgerRef.current &&
        !hamburgerRef.current.contains(e.target)
      ) {
        setHamburgerOpen(false);
      }
    }

    document.addEventListener('mousedown', handleClick);

    return () => {
      document.removeEventListener('mousedown', handleClick);
    };
  }, []);

  function handleSearch(e) {
    if (e.key === 'Enter' && searchQuery.trim()) {
      navigate(`/explore?q=${encodeURIComponent(searchQuery.trim())}`);
      setSearchQuery('');
      searchRef.current?.blur();
    }

    if (e.key === 'Escape') {
      setSearchQuery('');
      searchRef.current?.blur();
    }
  }

  function handleLogout() {
    localStorage.removeItem('yap_token');
    localStorage.removeItem('yap_user');
    navigate('/login');
  }

  return (
    <div className={styles.shell}>
      <Navbar />

      <div className={styles.main}>
        {/* Mobile topbar — visible only at ≤768px */}
        <header className={styles.mobileTopbar}>
          <button
            className={styles.createBtn}
            aria-label="Create new discussion"
            onClick={() => navigate('/create-post/discussion')}
          >
            <i className="ti ti-plus" aria-hidden="true" />
          </button>

          <Logo theme="dark" size="28px" />

          <button
            className={styles.notifBtn}
            aria-label="Notifications"
            onClick={() => navigate('/inbox')}
          >
            <i className="ti ti-bell" aria-hidden="true" />
            {unreadCount > 0 && (
              <span className={styles.mobileNotifBadge}>
                {unreadCount > 9 ? '9+' : unreadCount}
              </span>
            )}
          </button>
        </header>

        {/* Desktop topbar — hidden at ≤768px */}
        <header className={styles.topbar}>
          {title && (
            <span className={styles.topbarTitle}>
              {title}
            </span>
          )}

          <div className={styles.topbarRight}>
            <input
              ref={searchRef}
              className={styles.searchInput}
              placeholder="Search..."
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              onKeyDown={handleSearch}
            />

            {/* Notifications */}
            <div
              className={styles.notifWrap}
              ref={notifRef}
            >
              <button
                className={styles.iconBtn}
                aria-label="Notifications"
                onClick={() => {
                  setNotifOpen(v => !v);
                  setUserMenuOpen(false);
                }}
              >
                <i
                  className="ti ti-bell"
                  aria-hidden="true"
                />

                {unreadCount > 0 && (
                  <span className={styles.notifBadge}>
                    {unreadCount > 9 ? '9+' : unreadCount}
                  </span>
                )}
              </button>

              {notifOpen && (
                <div className={styles.notifPanel}>
                  <div className={styles.notifHeader}>
                    Notifications
                  </div>

                  {notifications.length === 0 ? (
                    <div className={styles.notifEmpty}>
                      Nothing yet
                    </div>
                  ) : (
                    <>
                      {notifications
                        .slice(0, 10)
                        .map(n => (
                          <div
                            key={n.notificationId}
                            className={`${styles.notifItem} ${
                              !n.read
                                ? styles.notifUnread
                                : ''
                            }`}
                            onClick={() => {
                              setNotifOpen(false);

                              if (n.postId) {
                                navigate(`/post/${n.postId}`);
                              }
                            }}
                          >
                            <span className={styles.notifMsg}>
                              {n.message}
                            </span>

                            <span className={styles.notifTime}>
                              {n.createdAt
                                ? new Date(
                                    n.createdAt
                                  ).toLocaleDateString()
                                : ''}
                            </span>
                          </div>
                        ))}
                      <div
                        className={styles.notifFooter}
                        onClick={() => {
                          setNotifOpen(false);
                          navigate('/inbox');
                        }}
                      >
                        View all
                      </div>
                    </>
                  )}
                </div>
              )}
            </div>

            {/* Hamburger Menu */}
            <div
              className={styles.hamburgerWrap}
              ref={hamburgerRef}
            >
              <button
                className={styles.iconBtn}
                aria-label="Menu"
                onClick={() => {
                  setHamburgerOpen(v => !v);
                  setNotifOpen(false);
                  setUserMenuOpen(false);
                }}
              >
                <i
                  className="ti ti-menu-2"
                  aria-hidden="true"
                />
              </button>

              {hamburgerOpen && (
                <div className={styles.hamburgerMenu}>
                  <button
                    className={styles.hamburgerItem}
                    onClick={() => {
                      setHamburgerOpen(false);
                      navigate('/settings');
                    }}
                  >
                    <i className="ti ti-settings" aria-hidden="true" />
                    Settings
                  </button>

                  {isModOrAdmin && (
                    <button
                      className={styles.hamburgerItem}
                      onClick={() => {
                        setHamburgerOpen(false);
                        navigate('/mod-queue');
                      }}
                    >
                      <i className="ti ti-flag" aria-hidden="true" />
                      Moderation
                      {pendingCount > 0 && (
                        <span className={styles.modBadge}>
                          {pendingCount > 99 ? '99+' : pendingCount}
                        </span>
                      )}
                    </button>
                  )}
                </div>
              )}
            </div>

            {/* User menu */}
            <div
              className={styles.userMenuWrap}
              ref={userMenuRef}
            >
              <div
                className={styles.avatarBtn}
                onClick={() => {
                  setUserMenuOpen(v => !v);
                  setNotifOpen(false);
                }}
                aria-label="User menu"
              >
                <Avatar
                  name={currentUser.username || '?'}
                  src={currentUser.avatarUrl}
                  size="sm"
                />
              </div>

              {userMenuOpen && (
                <div className={styles.userMenu}>
                  <div className={styles.userMenuHeader}>
                    <div className={styles.userMenuName}>
                      @{currentUser.username}
                    </div>
                  </div>

                  <button
                    className={styles.userMenuItem}
                    onClick={() => {
                      setUserMenuOpen(false);
                      navigate(`/@${currentUser.username}`);
                    }}
                  >
                    <i
                      className="ti ti-user"
                      aria-hidden="true"
                    />
                    View profile
                  </button>

                  <button
                    className={styles.userMenuItem}
                    onClick={() => {
                      setUserMenuOpen(false);
                      navigate('/settings');
                    }}
                  >
                    <i
                      className="ti ti-settings"
                      aria-hidden="true"
                    />
                    Edit profile
                  </button>

                  <div className={styles.userMenuDivider} />

                  <button
                    className={`${styles.userMenuItem} ${styles.userMenuDanger}`}
                    onClick={handleLogout}
                  >
                    <i
                      className="ti ti-logout"
                      aria-hidden="true"
                    />
                    Sign out
                  </button>
                </div>
              )}
            </div>
          </div>
        </header>

        <main className={styles.content}>
          <Outlet />
        </main>
      </div>

      <RightSidebar />
      <InstallPrompt />
      <BottomNav />
    </div>
  );
}