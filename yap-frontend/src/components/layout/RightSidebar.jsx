import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../../api';
import styles from './RightSidebar.module.css';

function formatCount(n) {
  if (n >= 1000) return (n / 1000).toFixed(1).replace(/\.0$/, '') + 'k';
  return String(n);
}

export default function RightSidebar() {
  const navigate = useNavigate();
  const [trending, setTrending] = useState([]);

  useEffect(() => {
    api.getTrendingTags()
      .then((res) => res.json())
      .then((data) => setTrending(Array.isArray(data) ? data.slice(0, 6) : []))
      .catch(() => setTrending([]));
  }, []);

  return (
    <aside className={styles.sidebar} aria-label="Trending topics">
      <div className={styles.sectionLabel}>Trending</div>
      {trending.length === 0 ? (
        <div className={styles.trendItem} style={{ opacity: 0.5 }}>No trending topics yet</div>
      ) : (
        trending.map(({ tagName, postCount }) => (
          <div
            key={tagName}
            className={styles.trendItem}
            onClick={() => navigate(`/?tag=${tagName}`)}
            style={{ cursor: 'pointer' }}
          >
            <span>#{tagName}</span>
            <span className={styles.trendCount}>{formatCount(postCount)}</span>
          </div>
        ))
      )}
    </aside>
  );
}
