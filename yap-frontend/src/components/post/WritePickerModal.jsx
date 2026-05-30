import { useNavigate } from 'react-router-dom';
import styles from './WritePickerModal.module.css';

/**
 * WritePickerModal
 *
 * Shown when user clicks "+ Write" in the navbar.
 * Instagram-style bottom sheet — pick post type, then route.
 *
 * Props:
 *   onClose {fn}
 */
export default function WritePickerModal({ onClose }) {
  const navigate = useNavigate();

  function pick(type) {
    onClose();
    navigate(`/create-post/${type}`);
  }

  return (
    <div className={styles.backdrop} onClick={onClose}>
      <div className={styles.sheet} onClick={(e) => e.stopPropagation()}>
        <div className={styles.handle} />
        <div className={styles.label}>What do you want to create?</div>

        <button className={styles.option} onClick={() => pick('blog')}>
          <div className={`${styles.optionIcon} ${styles.iconBlog}`}>
            <i className="ti ti-notebook" aria-hidden="true" />
          </div>
          <div className={styles.optionText}>
            <span className={styles.optionTitle}>Blog Post</span>
            <span className={styles.optionDesc}>
              Write a piece for your personal column
            </span>
          </div>
        </button>

        <button className={styles.option} onClick={() => pick('discussion')}>
          <div className={`${styles.optionIcon} ${styles.iconDiscussion}`}>
            <i className="ti ti-messages" aria-hidden="true" />
          </div>
          <div className={styles.optionText}>
            <span className={styles.optionTitle}>Discussion</span>
            <span className={styles.optionDesc}>
              Start a conversation in a space
            </span>
          </div>
        </button>
      </div>
    </div>
  );
}
