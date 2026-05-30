import { useState } from 'react';
import styles from './SpaceSidebar.module.css';

/**
 * SpaceSidebar
 *
 * Replaces the global RightSidebar when on a space page.
 *
 * Props:
 *   space {object}:
 *     - name          {string}
 *     - description   {string}
 *     - joinedCount   {number}
 *     - postsCount    {number}
 *     - createdAt     {string}
 *     - guidelines    {string[]}
 */

function formatCount(n) {
  if (n >= 1000) return (n / 1000).toFixed(1).replace(/\.0$/, '') + 'k';
  return String(n);
}

export default function SpaceSidebar({ space }) {
  const [joined, setJoined] = useState(false);

  const {
    name,
    description,
    joinedCount = 0,
    postsCount = 0,
    createdAt,
    guidelines = [],
  } = space;

  return (
    <div className={styles.panel}>
      {/* Space identity */}
      <div className={styles.card}>
        <div className={styles.banner} />

        <div className={styles.cardBody}>
          <div className={styles.spaceName}>{name}</div>
          <div className={styles.spaceHandle}>w/{name}</div>
          <p className={styles.description}>{description}</p>
          {createdAt && (
            <div className={styles.meta}>Created {createdAt}</div>
          )}

          <button
            className={`${styles.joinBtn} ${joined ? styles.joined : ''}`}
            onClick={() => setJoined((v) => !v)}
          >
            {joined ? 'Joined' : 'Join'}
          </button>
        </div>

        {/* Stats */}
        <div className={styles.stats}>
          <div className={styles.stat}>
            <span className={styles.statNum}>{formatCount(joinedCount)}</span>
            <span className={styles.statLabel}>joined</span>
          </div>
          <div className={styles.stat}>
            <span className={styles.statNum}>{formatCount(postsCount)}</span>
            <span className={styles.statLabel}>posts</span>
          </div>
        </div>
      </div>

      {/* Guidelines — always visible while scrolling */}
      {guidelines.length > 0 && (
        <div className={styles.guidelinesCard}>
          <div className={styles.guidelinesTitle}>Space Guidelines</div>
          {guidelines.map((text, i) => (
            <div key={i} className={styles.guideline}>
              <span className={styles.guidelineNum}>{i + 1}.</span>
              <span>{text}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
