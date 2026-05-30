import Avatar from '../common/Avatar';
import styles from './RightSidebar.module.css';

// TODO: replace with real API data
const TRENDING = [
  { tag: '#SoftLife',      count: '4.2k' },
  { tag: '#WomenInTech',   count: '2.8k' },
  { tag: '#BoundaryWork',  count: '1.9k' },
  { tag: '#QuietQuitting', count: '1.4k' },
];

const SUGGESTED = [
  { name: 'noelle.writes', handle: '@noelle.writes', topic: 'Wellness' },
  { name: 'tasha.voices',  handle: '@tasha.voices',  topic: 'Career'   },
  { name: 'Mira Osei',     handle: '@mira.osei',     topic: 'Mindset'  },
];

export default function RightSidebar() {
  return (
    <aside className={styles.sidebar} aria-label="Trending and suggestions">
      {/* Trending */}
      <div className={styles.sectionLabel}>Trending</div>
      {TRENDING.map(({ tag, count }) => (
        <div key={tag} className={styles.trendItem}>
          <span>{tag}</span>
          <span className={styles.trendCount}>{count}</span>
        </div>
      ))}

      {/* Suggested */}
      <div className={styles.sectionLabel}>Suggested</div>
      {SUGGESTED.map(({ name, handle, topic }) => (
        <div key={handle} className={styles.suggestedItem}>
          <Avatar name={name} size="sm" />
          <div className={styles.suggestedInfo}>
            <span className={styles.suggestedName}>{handle}</span>
            <span className={styles.suggestedSub}>{topic}</span>
          </div>
        </div>
      ))}
    </aside>
  );
}
