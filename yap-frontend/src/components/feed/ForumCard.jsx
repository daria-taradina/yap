import { useNavigate } from 'react-router-dom';
import styles from './ForumCard.module.css';

export default function ForumCard({ post }) {
  const navigate = useNavigate();
  const { id, title, space, username, likes, comments, tags } = post;

  return (
    <article
      className={styles.card}
      onClick={() => navigate(`/post/${id}?type=discussion`)}
      role="button"
    >
      <div className={styles.body}>
        <div className={styles.topMeta}>
          {space && (
            <span
              className={styles.space}
              onClick={(e) => { e.stopPropagation(); navigate(`/w/${space}`); }}
            >
              w/{space}
            </span>
          )}
          {username && <span className={styles.username}>{username}</span>}
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
              <i className="ti ti-heart" aria-hidden="true" />{likes}
            </span>
            <span className={styles.actionItem}>
              <i className="ti ti-message-circle" aria-hidden="true" />{comments}
            </span>
          </div>
        </div>
      </div>
    </article>
  );
}
