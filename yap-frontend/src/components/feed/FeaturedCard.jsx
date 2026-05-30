import { useNavigate } from 'react-router-dom';
import styles from './FeaturedCard.module.css';

export default function FeaturedCard({ post }) {
  const navigate = useNavigate();
  const { id, title, excerpt, displayName, username, readTime, tags, likes, comments } = post;

  return (
    <article
      className={styles.card}
      onClick={() => navigate(`/post/${id}?type=blog`)}
      role="button"
    >
      <div className={styles.coverStrip}>
        <i className="ti ti-feather" aria-hidden="true" />
      </div>
      <div className={styles.body}>
        <div className={styles.authorBlock}>
          <span className={styles.displayName}>{displayName}</span>
          <span className={styles.authorMeta}>
            <span className={styles.username}>{username}</span>
            {readTime && <span>· {readTime} read</span>}
          </span>
        </div>
        <h2 className={styles.title}>{title}</h2>
        {excerpt && <p className={styles.excerpt}>{excerpt}</p>}
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
