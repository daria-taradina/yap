import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import styles from './ExplorePage.module.css';

const SPACE_COLORS = ['#C4973F', '#6B8BAD', '#7A9E7E', '#A07AB5', '#9E7A7A'];

export default function ExplorePage() {
  const navigate = useNavigate();
  const [search, setSearch] = useState('');
  const [spaces, setSpaces] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getSpaces()
      .then((res) => res.json())
      .then((data) => setSpaces(Array.isArray(data) ? data : []))
      .catch(() => setSpaces([]))
      .finally(() => setLoading(false));
  }, []);

  const filtered = spaces.filter((s) =>
    !search ||
    s.name.toLowerCase().includes(search.toLowerCase()) ||
    s.description?.toLowerCase().includes(search.toLowerCase())
  );

  if (loading) return <div className={styles.page}><div className={styles.emptyState}><div className={styles.emptyIcon}>✦</div><p>Loading spaces...</p></div></div>;

  return (
    <div className={styles.page}>
      <div className={styles.searchWrap}>
        <i className={`ti ti-search ${styles.searchIcon}`} aria-hidden="true" />
        <input
          className={styles.searchInput}
          placeholder="Search spaces..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      <div className={styles.sectionLabel}>All Spaces</div>

      {filtered.length === 0 ? (
        <div className={styles.emptyState}>
          <div className={styles.emptyIcon}>✦</div>
          <p>
            {search
              ? `No spaces found for "${search}"`
              : 'No spaces yet — be the first to create one.'}
          </p>
        </div>
      ) : (
        <div className={styles.grid}>
          {filtered.map((space, i) => (
            <div
              key={space.communityId}
              className={styles.spaceCard}
              onClick={() => navigate(`/w/${space.name}`)}
            >
              <div className={styles.spaceCardTop}>
                <span
                  className={styles.spaceDot}
                  style={{ background: SPACE_COLORS[i % SPACE_COLORS.length] }}
                />
                {space.member && (
                  <span className={styles.joinedBadge}>Joined</span>
                )}
              </div>
              <div className={styles.spaceName}>w/{space.name}</div>
              {space.description && (
                <p className={styles.spaceDesc}>{space.description}</p>
              )}
              <div className={styles.spaceMeta}>
                {space.memberCount?.toLocaleString()} members
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}