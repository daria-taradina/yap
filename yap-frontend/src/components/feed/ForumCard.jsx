import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../../api';
import styles from './ForumCard.module.css';

export default function ForumCard({ post, onDelete }) {
  const navigate = useNavigate();
  const { postId, title, communityName, authorUsername, likeCount, commentCount, tags, canDelete } = post;
  const [showConfirm, setShowConfirm] = useState(false);
  const [deleted, setDeleted] = useState(false);
  const [deleteError, setDeleteError] = useState('');

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
            <Link
              to={`/users/${authorUsername}`}
              className={styles.username}
              onClick={(e) => e.stopPropagation()}
            >
              @{authorUsername}
            </Link>
          )}
        </div>
        <h3 className={styles.title}>{title}</h3>

        {deleteError && (
          <div className={styles.deleteError}>{deleteError}</div>
        )}

        <div className={styles.footer}>
          <div className={styles.tags}>
            {tags && tags.map((tag) => (
              <Link
                key={tag}
                to={`/?tag=${tag}`}
                className={styles.tag}
                onClick={(e) => e.stopPropagation()}
              >
                #{tag}
              </Link>
            ))}
          </div>
          <div className={styles.actions}>
            <span className={styles.actionItem}>
              <i className="ti ti-heart" aria-hidden="true" />{likeCount}
            </span>
            <span className={styles.actionItem}>
              <i className="ti ti-message-circle" aria-hidden="true" />{commentCount}
            </span>
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
    </article>
  );
}