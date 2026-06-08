import { useNavigate } from 'react-router-dom';
import styles from './FeaturedCard.module.css';

export default function FeaturedCard({ post }) {
  const navigate = useNavigate();
  const { postId, title, contentText, authorUsername, likeCount, commentCount, tags } = post;

  return (
    <article className={styles.card} onClick={() => navigate(`/post/${postId}`)} role="button">
      <div className={styles.coverStrip}><i className="ti ti-feather" aria-hidden="true" /></div>
      <div className={styles.body}>
        <div className={styles.authorBlock}>
          <span className={styles.username}>@{authorUsername}</span>
        </div>
        <h2 className={styles.title}>{title}</h2>
        {contentText && <p className={styles.excerpt}>{contentText.slice(0, 120)}{contentText.length > 120 ? '...' : ''}</p>}
        <div className={styles.footer}>
          <div className={styles.tags}>
            {tags && tags.map(tag => <span key={tag} className={styles.tag}>#{tag}</span>)}
          </div>
          <div className={styles.actions}>
            <span className={styles.actionItem}><i className="ti ti-heart" />{likeCount}</span>
            <span className={styles.actionItem}><i className="ti ti-message-circle" />{commentCount}</span>
          </div>
        </div>
      </div>
    </article>
  );
}