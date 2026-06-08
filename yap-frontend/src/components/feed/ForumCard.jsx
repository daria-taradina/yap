import { useNavigate } from 'react-router-dom';
import styles from './ForumCard.module.css';

export default function ForumCard({ post }) {
  const navigate = useNavigate();
  const { postId, title, communityName, authorUsername, likeCount, commentCount, tags } = post;

  return (
    <article
      className={styles.card}
      onClick={() => navigate(`/post/${postId}`)}
      role="button"
    >
      <div className={styles.body}>
        <div className={styles.topMeta}>
          {communityName && (
            <span
              className={styles.space}
              onClick={(e) => { e.stopPropagation(); navigate(`/w/${communityName}`); }}
            >
              w/{communityName}
            </span>
          )}
          {authorUsername && <span className={styles.username}>@{authorUsername}</span>}
        </div>
        <h3 className={styles.title}>{title}</h3>
        <div className={styles.footer}>
          <div className={styles.tags}>
            {tags && tags.map((tag) => (
              <span key={tag} className={styles.tag}>#{tag}</span>
            ))}
          </div>
          <div className={styles.actions}>
            <span className={styles.actionItem}>
              <i className="ti ti-heart" aria-hidden="true" />{likeCount}
            </span>
            <span className={styles.actionItem}>
              <i className="ti ti-message-circle" aria-hidden="true" />{commentCount}
            </span>
          </div>
        </div>
      </div>
    </article>
  );
}