import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { api } from '../api';
import Avatar from '../components/common/Avatar';
import styles from './InboxPage.module.css';

const PAGE_SIZE = 20;

/**
 * Format a date string as relative time (e.g. "2 hours ago", "3 days ago").
 */
function formatRelativeTime(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diffMs = now - date;
  const diffSec = Math.floor(diffMs / 1000);
  const diffMin = Math.floor(diffSec / 60);
  const diffHr = Math.floor(diffMin / 60);
  const diffDay = Math.floor(diffHr / 24);

  if (diffSec < 60) return 'just now';
  if (diffMin < 60) return `${diffMin}m ago`;
  if (diffHr < 24) return `${diffHr}h ago`;
  if (diffDay < 30) return `${diffDay}d ago`;
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

/**
 * Determine the link target for a notification.
 */
function getNotificationLink(notification) {
  if (notification.postId) {
    return `/post/${notification.postId}`;
  }
  return null;
}

export default function InboxPage() {
  const navigate = useNavigate();

  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);

  // Auth gate
  useEffect(() => {
    const token = localStorage.getItem('yap_token');
    if (!token) {
      navigate('/login', { replace: true });
    }
  }, [navigate]);

  // Fetch first page & mark all as read
  useEffect(() => {
    async function init() {
      setLoading(true);
      try {
        const res = await api.getNotifications(0, PAGE_SIZE);
        const data = await res.json();
        setNotifications(data);
        setHasMore(data.length === PAGE_SIZE);
        setPage(0);

        // Mark all as read on page open
        api.markNotificationsRead().catch(() => {});
      } catch {
        setNotifications([]);
      } finally {
        setLoading(false);
      }
    }
    init();
  }, []);

  async function loadMore() {
    setLoadingMore(true);
    try {
      const nextPage = page + 1;
      const res = await api.getNotifications(nextPage, PAGE_SIZE);
      const data = await res.json();
      setNotifications(prev => [...prev, ...data]);
      setHasMore(data.length === PAGE_SIZE);
      setPage(nextPage);
    } catch {
      // silently fail
    } finally {
      setLoadingMore(false);
    }
  }

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.state}>Loading notifications…</div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1>Inbox</h1>
        <p>Your notifications</p>
      </div>

      {notifications.length === 0 ? (
        <div className={styles.state}>
          <div className={styles.emptyIcon}>✓</div>
          You're all caught up ✓
        </div>
      ) : (
        <>
          {notifications.map(n => {
            const link = getNotificationLink(n);
            const content = (
              <>
                <div className={styles.avatarWrap}>
                  <Avatar
                    src={n.actorAvatarUrl}
                    name={n.actorUsername || 'System'}
                    size="sm"
                  />
                </div>
                <div className={styles.body}>
                  <p className={styles.message}>{n.message}</p>
                  <span className={styles.timestamp}>{formatRelativeTime(n.createdAt)}</span>
                </div>
              </>
            );

            const className = `${styles.item}${!n.read ? ` ${styles.unread}` : ''}`;

            if (link) {
              return (
                <Link key={n.notificationId} to={link} className={className}>
                  {content}
                </Link>
              );
            }

            return (
              <div key={n.notificationId} className={className} style={{ cursor: 'default' }}>
                {content}
              </div>
            );
          })}

          {hasMore && (
            <div className={styles.loadMore}>
              <button
                className={styles.loadMoreBtn}
                onClick={loadMore}
                disabled={loadingMore}
              >
                {loadingMore ? 'Loading…' : 'Load more'}
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
