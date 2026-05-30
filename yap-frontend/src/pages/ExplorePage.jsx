import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './ExplorePage.module.css';

/**
 * ExplorePage  —  /explore
 *
 * Discover spaces by search.
 * TODO: GET /api/spaces?search=
 */

const SPACE_COLORS = ['#C4973F', '#6B8BAD', '#7A9E7E', '#A07AB5', '#9E7A7A'];

export default function ExplorePage() {
  const navigate = useNavigate();
  const [search, setSearch] = useState('');
  const [spaces] = useState([]); // TODO: fetch from GET /api/spaces

  const filtered = spaces.filter((s) =>
    !search ||
    s.name.toLowerCase().includes(search.toLowerCase()) ||
    s.description?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className={styles.page}>
      {/* Search */}
      <div className={styles.searchWrap}>
        <i className={`ti ti-search ${styles.searchIcon}`} aria-hidden="true" />
        <input
          className={styles.searchInput}
          placeholder="Search spaces..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {/* Category chips removed — search is enough for now */}

      {/* Space grid */}
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
              key={space.id}
              className={styles.spaceCard}
              onClick={() => navigate(`/w/${space.name}`)}
            >
              <div className={styles.spaceCardTop}>
                <span
                  className={styles.spaceDot}
                  style={{ background: SPACE_COLORS[i % SPACE_COLORS.length] }}
                />
                {space.isJoined && (
                  <span className={styles.joinedBadge}>Joined</span>
                )}
              </div>
              <div className={styles.spaceName}>{space.name}</div>
              <div className={styles.spaceHandle}>w/{space.name}</div>
              {space.description && (
                <p className={styles.spaceDesc}>{space.description}</p>
              )}
              <div className={styles.spaceMeta}>
                {space.joinedCount?.toLocaleString()} joined
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
