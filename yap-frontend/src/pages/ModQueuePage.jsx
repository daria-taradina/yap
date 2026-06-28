import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import styles from './ModQueuePage.module.css';

function formatReason(reason) {
  if (!reason) return 'Unknown';
  return reason
    .replace(/_/g, ' ')
    .replace(/\b\w/g, c => c.toUpperCase())
    .replace(/\B[A-Z]/g, c => c.toLowerCase())
    .replace(/^./, c => c.toUpperCase());
}

function formatDate(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
}

function truncate(text, max = 200) {
  if (!text) return '';
  return text.length > max ? text.slice(0, max) + '…' : text;
}

export default function ModQueuePage() {
  const navigate = useNavigate();

  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [actionErrors, setActionErrors] = useState({});
  const [pendingActions, setPendingActions] = useState({});
  const [confirmBan, setConfirmBan] = useState(null);

  // Role gate: redirect non-ADMIN/MOD users
  useEffect(() => {
    const user = JSON.parse(localStorage.getItem('yap_user') || '{}');
    if (!user.role || (user.role !== 'ADMIN' && user.role !== 'MOD')) {
      navigate('/', { replace: true });
    }
  }, [navigate]);

  // Fetch reports
  useEffect(() => {
    setLoading(true);
    api.getModReports(0, 20)
      .then(r => r.json())
      .then(data => {
        setReports(data.content || []);
        setHasMore((data.page + 1) * data.size < data.totalElements);
        setPage(0);
      })
      .catch(() => setReports([]))
      .finally(() => setLoading(false));
  }, []);

  async function loadMore() {
    setLoadingMore(true);
    try {
      const nextPage = page + 1;
      const res = await api.getModReports(nextPage, 20);
      const data = await res.json();
      setReports(prev => [...prev, ...(data.content || [])]);
      setHasMore((nextPage + 1) * data.size < data.totalElements);
      setPage(nextPage);
    } catch {
      // silently fail
    } finally {
      setLoadingMore(false);
    }
  }

  async function handleAction(reportId, action) {
    // Clear any previous error for this report
    setActionErrors(prev => ({ ...prev, [reportId]: null }));
    setPendingActions(prev => ({ ...prev, [reportId]: action }));

    // Optimistic: remove from list
    const removedIndex = reports.findIndex(r => r.reportId === reportId);
    const removedReport = reports[removedIndex];
    setReports(prev => prev.filter(r => r.reportId !== reportId));

    try {
      let apiCall;
      if (action === 'dismiss') apiCall = api.dismissReport(reportId);
      else if (action === 'remove') apiCall = api.removeReport(reportId);
      else if (action === 'ban') apiCall = api.banReport(reportId);

      const res = await apiCall;
      if (!res.ok) throw new Error('Action failed');
    } catch {
      // Rollback: re-insert at original position
      setReports(prev => {
        const updated = [...prev];
        updated.splice(removedIndex, 0, removedReport);
        return updated;
      });
      setActionErrors(prev => ({
        ...prev,
        [reportId]: `Could not ${action} this report. Please try again.`,
      }));
    } finally {
      setPendingActions(prev => ({ ...prev, [reportId]: null }));
      setConfirmBan(null);
    }
  }

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.state}>Loading reports…</div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1>Mod Queue</h1>
        <p>Review reported content and take action</p>
      </div>

      {reports.length === 0 ? (
        <div className={styles.state}>
          <div className={styles.emptyIcon}>✓</div>
          No pending reports. The community is safe!
        </div>
      ) : (
        <>
          {reports.map(report => {
            const isPost = !!report.post;
            const content = isPost ? report.post : report.comment;
            const title = isPost ? report.post.title : null;
            const contentText = content?.contentText || '';
            const authorUsername = content?.authorUsername || 'Unknown';
            const createdAt = content?.createdAt || '';
            const communityName = isPost ? report.post.communityName : '';

            return (
              <div
                key={report.reportId}
                className={`${styles.card} ${pendingActions[report.reportId] ? styles.removing : ''}`}
              >
                <div className={styles.cardMeta}>
                  {communityName && (
                    <span className={styles.spaceBadge}>w/{communityName}</span>
                  )}
                  <span>by {authorUsername}</span>
                  <span>•</span>
                  <span>{formatDate(createdAt)}</span>
                </div>

                {title && <h3 className={styles.cardTitle}>{title}</h3>}
                <p className={styles.cardContent}>{truncate(contentText)}</p>

                <div className={styles.reportInfo}>
                  <span>
                    Reason: <span className={styles.reportReason}>{formatReason(report.reason)}</span>
                  </span>
                  <span>Reported by: {report.reporterUsername}</span>
                  <span>Reported: {formatDate(report.createdAt)}</span>
                </div>

                <div className={styles.actions}>
                  <button
                    className={styles.btnDismiss}
                    disabled={!!pendingActions[report.reportId]}
                    onClick={() => handleAction(report.reportId, 'dismiss')}
                  >
                    Dismiss
                  </button>
                  <button
                    className={styles.btnRemove}
                    disabled={!!pendingActions[report.reportId]}
                    onClick={() => handleAction(report.reportId, 'remove')}
                  >
                    Remove
                  </button>
                  <button
                    className={styles.btnBan}
                    disabled={!!pendingActions[report.reportId]}
                    onClick={() => setConfirmBan(report.reportId)}
                  >
                    Ban
                  </button>
                </div>

                {confirmBan === report.reportId && (
                  <div className={styles.confirmOverlay}>
                    <span>Ban this user? This action is permanent.</span>
                    <button
                      className={styles.confirmYes}
                      onClick={() => handleAction(report.reportId, 'ban')}
                    >
                      Confirm Ban
                    </button>
                    <button
                      className={styles.confirmNo}
                      onClick={() => setConfirmBan(null)}
                    >
                      Cancel
                    </button>
                  </div>
                )}

                {actionErrors[report.reportId] && (
                  <div className={styles.errorMsg}>
                    {actionErrors[report.reportId]}
                  </div>
                )}
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
                {loadingMore ? 'Loading…' : 'Load more reports'}
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
