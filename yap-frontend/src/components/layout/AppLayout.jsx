import { useState, useEffect, useRef } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import Navbar from './Navbar';
import RightSidebar from './RightSidebar';
import BottomNav from './BottomNav';
import Avatar from '../common/Avatar';
import { api } from '../../api';
import styles from './AppLayout.module.css';

function useTopbarTitle(pathname) {
  if (pathname === '/') return 'Home';
  if (pathname.startsWith('/w/')) return 'Forum';
  if (pathname.startsWith('/blog/')) return 'Profile';
  if (pathname.startsWith('/explore')) return 'Explore';
  if (pathname.startsWith('/forums')) return 'Forums';
  if (pathname.startsWith('/blogs')) return 'Blogs';
  if (pathname.startsWith('/create-space')) return 'Create Space';
  if (pathname.startsWith('/create-post/blog')) return 'New Blog Post';
  if (pathname.startsWith('/create-post/discussion')) return 'New Discussion';
  if (pathname.startsWith('/post/')) return '';
  return '';
}

export default function AppLayout() {
  const { pathname } = useLocation();
  const navigate = useNavigate();
  const title = useTopbarTitle(pathname);
  const currentUser = JSON.parse(localStorage.getItem('yap_user') || '{}');

  const [searchOpen, setSearchOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  const [notifOpen, setNotifOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  const [userMenuOpen, setUserMenuOpen] = useState(false);

  const searchRef = useRef(null);
  const userMenuRef = useRef(null);
  const notifRef = useRef(null);

  useEffect(() => {
    api.getUnreadCount()
      .then(r => r?.json())
      .then(d => setUnreadCount(d?.count || 0))
      .catch(() => {});
  }, [pathname]);

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

  useEffect(() => {
    if (searchOpen && searchRef.current) {
      searchRef.current.focus();
    }
  }, [searchOpen]);

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
    }

    document.addEventListener('mousedown', handleClick);

    return () => {
      document.removeEventListener('mousedown', handleClick);
    };
  }, []);

  function handleSearch(e) {
    if (e.key === 'Enter' && searchQuery.trim()) {
      setSearchOpen(false);
      navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`);
      setSearchQuery('');
    }

    if (e.key === 'Escape') {
      setSearchOpen(false);
      setSearchQuery('');
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
        <header className={styles.topbar}>
          {title && (
            <span className={styles.topbarTitle}>
              {title}
            </span>
          )}

          <div className={styles.topbarRight}>
            {searchOpen ? (
              <input
                ref={searchRef}
                className={styles.searchInput}
                placeholder="Search by tag..."
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                onKeyDown={handleSearch}
                onBlur={() => {
                  if (!searchQuery) {
                    setSearchOpen(false);
                  }
                }}
              />
            ) : (
              <button
                className={styles.iconBtn}
                aria-label="Search"
                onClick={() => setSearchOpen(true)}
              >
                <i
                  className="ti ti-search"
                  aria-hidden="true"
                />
              </button>
            )}

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
                    notifications
                      .slice(0, 15)
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
                      ))
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
                      navigate(`/blog/${currentUser.username}`);
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
      <BottomNav />
    </div>
  );
}