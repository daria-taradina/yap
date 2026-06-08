import styles from './SpaceSidebar.module.css';

function formatCount(n) {
  if (n >= 1000) return (n / 1000).toFixed(1).replace(/\.0$/, '') + 'k';
  return String(n ?? 0);
}

export default function SpaceSidebar({ space, onJoin, onLeave }) {
  const guidelines = space.guidelines
    ? space.guidelines.split(',').map(g => g.trim()).filter(Boolean)
    : [];

  return (
    <div className={styles.panel}>
      <div className={styles.card}>
        <div className={styles.banner} />
        <div className={styles.cardBody}>
          <div className={styles.spaceName}>{space.name}</div>
          <div className={styles.spaceHandle}>w/{space.name}</div>
          <p className={styles.description}>{space.description}</p>
          {space.createdAt && (
            <div className={styles.meta}>
              Created {new Date(space.createdAt).toLocaleDateString()}
            </div>
          )}
          {space.member
            ? <button className={`${styles.joinBtn} ${styles.joined}`} onClick={onLeave}>Leave</button>
            : <button className={styles.joinBtn} onClick={onJoin}>Join</button>
          }
        </div>
        <div className={styles.stats}>
          <div className={styles.stat}>
            <span className={styles.statNum}>{formatCount(space.memberCount)}</span>
            <span className={styles.statLabel}>members</span>
          </div>
        </div>
      </div>

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