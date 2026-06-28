import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../../api';
import Avatar from '../common/Avatar';
import ReportModal from '../common/ReportModal';
import styles from './ForumCard.module.css';

const FLAIR_COLORS = {
  DISCUSSION: '#C4973F',
  SUPPORT: '#1D9E75',
  RANT: '#C0392B',
  RESOURCE: '#2E86C1',
  QUESTION: '#7D3C98',
  SENSITIVE: '#D35400',
};

export default function ForumCard({ post, onDelete }) {
  const navigate = useNavigate();
  const { postId, title, communityName, authorUsername, likeCount, commentCount, canDelete } = post;
  const [showConfirm, setShowConfirm] = useState(false);
  const [deleted, setDeleted] = useState(false);
  const [deleteError, setDeleteError] = useState('');
  const [showReportModal, setShowReportModal] = useState(false);
  const [showCopied, setShowCopied] = useState(false);
  const [bookmarked, setBookmarked] = useState(!!post.bookmarked);

  const token = localStorage.getItem('yap_token');
  const currentUser = token ? JSON.parse(localStorage.getItem('yap_user') || '{}') : null;
  const canReport = currentUser && authorUsername !== currentUser.username;

  function handleShareClick(e) {
    e.stopPropagation();
    const url = `${window.location.origin}/post/${postId}`;
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(url).then(() => {
        setShowCopied(true);
        setTimeout(() => setShowCopied(false), 1500);
      });
    } else {
      // Fallback for browsers without clipboard API
      const input = document.createElement('input');
      input.style.position = 'fixed';
      input.style.opacity = '0';
      input.value = url;
      document.body.appendChild(input);
      input.select();
      document.execCommand('copy');
      document.body.removeChild(input);
      setShowCopied(true);
      setTimeout(() => setShowCopied(false), 1500);
    }
  }

  async function handleBookmarkClick(e) {
    e.stopPropagation();
    if (!token) {
      navigate('/login');
      return;
    }
    setBookmarked(prev => !prev);
    try {
      await api.toggleBookmark(postId);
    } catch {
      setBookmarked(prev => !prev);
    }
  }

  function handleDeleteClick(e) {
    e.stopPropagation();
    setShowConfirm(true);
  }

  async function handleConfirmDelete(e) {
    e.stopPropagation();
    setShowConfirm(false);
    setDeleteError('');

    if (onDelete) {
      // Parent handles optimistic removal + rollback
      onDelete(postId);
    } else {
      // Self-managed delete
      try {
        const res = await api.deletePost(postId);
        if (!res.ok) throw new Error('Failed to delete');
        setDeleted(true);
      } catch {
        setDeleteError('Could not delete post. Please try again.');
      }
    }
  }

  function handleCancelDelete(e) {
    e.stopPropagation();
    setShowConfirm(false);
  }

  if (deleted) return null;

  return (
    <article
      className={styles.card}
      onClick={() => navigate(`/post/${postId}`)}
      role="button"
    >
      <div className={styles.body}>
        <div className={styles.topMeta}>
          {communityName && (
            <Link
              to={`/w/${communityName}`}
              className={styles.space}
              onClick={(e) => e.stopPropagation()}
            >
              w/{communityName}
            </Link>
          )}
          {authorUsername && (
            <>
              <Avatar src={post.authorAvatarUrl} name={authorUsername} size="sm" />
              <Link
                to={`/@${authorUsername}`}
                className={styles.username}
                onClick={(e) => e.stopPropagation()}
              >
                @{authorUsername}
              </Link>
            </>
          )}
          {post.flair && (
            <span
              className={styles.flair}
              style={{ backgroundColor: FLAIR_COLORS[post.flair] || '#888' }}
            >
              {post.flair === 'SENSITIVE' && <span aria-label="Warning">⚠️</span>}
              {post.flair}
            </span>
          )}
        </div>
        <h3 className={styles.title}>{title}</h3>

        {deleteError && (
          <div className={styles.deleteError}>{deleteError}</div>
        )}

        <div className={styles.footer}>
          <div className={styles.actions}>
            <span className={styles.actionItem}>
              <i className="ti ti-heart" aria-hidden="true" />{likeCount}
            </span>
            <span className={styles.actionItem}>
              <i className="ti ti-message-circle" aria-hidden="true" />{commentCount}
            </span>
            <span className={styles.shareBtnWrapper}>
              <button
                className={styles.shareBtn}
                onClick={handleShareClick}
                aria-label="Copy link to post"
                title="Copy link"
              >
                <i className="ti ti-link" aria-hidden="true" />
              </button>
              {showCopied && (
                <span className={styles.copiedTooltip}>Link copied!</span>
              )}
            </span>
            <button
              className={`${styles.bookmarkBtn} ${bookmarked ? styles.bookmarkBtnActive : ''}`}
              onClick={handleBookmarkClick}
              aria-label={bookmarked ? 'Remove bookmark' : 'Bookmark post'}
              title={bookmarked ? 'Remove bookmark' : 'Bookmark'}
            >
              <i className={bookmarked ? 'ti ti-bookmark-filled' : 'ti ti-bookmark'} aria-hidden="true" />
            </button>
            {canDelete && (
              <button
                className={styles.deleteBtn}
                onClick={handleDeleteClick}
                aria-label="Delete post"
                title="Delete post"
              >
                <i className="ti ti-trash" aria-hidden="true" />
              </button>
            )}
            {canReport && (
              <button
                className={styles.reportBtn}
                onClick={(e) => { e.stopPropagation(); setShowReportModal(true); }}
                aria-label="Report post"
                title="Report post"
              >
                <i className="ti ti-flag" aria-hidden="true" />
              </button>
            )}
          </div>
        </div>
      </div>

      {showConfirm && (
        <div className={styles.confirmOverlay} onClick={(e) => e.stopPropagation()}>
          <div className={styles.confirmDialog}>
            <p className={styles.confirmText}>Delete this post? This action cannot be undone.</p>
            <div className={styles.confirmActions}>
              <button className={styles.confirmCancelBtn} onClick={handleCancelDelete}>Cancel</button>
              <button className={styles.confirmDeleteBtn} onClick={handleConfirmDelete}>Delete</button>
            </div>
          </div>
        </div>
      )}

      <ReportModal
        isOpen={showReportModal}
        onClose={() => setShowReportModal(false)}
        postId={postId}
      />
    </article>
  );
}