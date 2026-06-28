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
  const [hotPosts, setHotPosts] = useState([]);

  useEffect(() => {
    api.getTrendingTags()
      .then((res) => res.json())
      .then((data) => setTrending(Array.isArray(data) ? data.slice(0, 8) : []))
      .catch(() => setTrending([]));

    api.getHotPosts()
      .then((res) => res.json())
      .then((data) => setHotPosts(Array.isArray(data) ? data.slice(0, 3) : []))
      .catch(() => setHotPosts([]));
  }, []);

  return (
    <aside className={styles.sidebar} aria-label="Trending topics and hot discussions">
      {/* Trending Topics */}
      <div className={styles.sectionLabel}>Trending Topics</div>
      {trending.length === 0 ? (
        <div className={styles.emptyState}>No trending topics yet</div>
      ) : (
        <div className={styles.pillsContainer}>
          {trending.map(({ tagName, postCount }) => (
            <button
              key={tagName}
              className={styles.pill}
              onClick={() => navigate(`/explore?q=${encodeURIComponent('#' + tagName)}`)}
              title={`${formatCount(postCount)} posts`}
            >
              #{tagName}
            </button>
          ))}
        </div>
      )}

      {/* Hot Discussions */}
      <div className={styles.sectionLabel}>Hot Discussions</div>
      {hotPosts.length === 0 ? (
        <div className={styles.emptyState}>No hot discussions yet</div>
      ) : (
        <div className={styles.hotList}>
          {hotPosts.map((post) => (
            <div key={post.postId} className={styles.hotItem}>
              <div
                className={styles.hotTitle}
                onClick={() => navigate(`/post/${post.postId}`)}
              >
                {post.title}
              </div>
              <div className={styles.hotMeta}>
                {post.communityName && (
                  <span
                    className={styles.hotSpace}
                    onClick={(e) => {
                      e.stopPropagation();
                      navigate(`/w/${post.communityName}`);
                    }}
                  >
                    w/{post.communityName}
                  </span>
                )}
                <span className={styles.hotStats}>
                  <span title="Likes">♥ {formatCount(post.likeCount)}</span>
                  <span title="Comments">💬 {formatCount(post.commentCount)}</span>
                </span>
              </div>
            </div>
          ))}
        </div>
      )}
    </aside>
  );
}
