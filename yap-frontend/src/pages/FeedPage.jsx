import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import ForumCard from '../components/feed/ForumCard';
import { api } from '../api';
import styles from './FeedPage.module.css';

export default function FeedPage() {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchParams, setSearchParams] = useSearchParams();
  const activeTag = searchParams.get('tag') || '';

  useEffect(() => {
    setLoading(true);

    if (activeTag) {
      // Tag endpoint is public GET — no auth needed
      api.getPostsByTag(activeTag)
        .then((res) => res.json())
        .then((data) => setPosts(Array.isArray(data) ? data : []))
        .catch(() => setPosts([]))
        .finally(() => setLoading(false));
    } else {
      const user = JSON.parse(localStorage.getItem('yap_user') || '{}');
      if (!user.userId) { setLoading(false); return; }

      api.getFeed(user.userId)
        .then((res) => res.json())
        .then((data) => setPosts(Array.isArray(data) ? data : []))
        .catch(() => setPosts([]))
        .finally(() => setLoading(false));
    }
  }, [activeTag]);

  function clearTag() {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      next.delete('tag');
      return next;
    });
  }

  if (loading) return <div className={styles.state}>Loading feed...</div>;

  return (
    <div className={styles.page}>
      {activeTag && (
        <div className={styles.tagPill}>
          <span className={styles.tagPillLabel}>#{activeTag}</span>
          <button
            className={styles.tagPillClear}
            onClick={clearTag}
            aria-label={`Clear tag filter ${activeTag}`}
          >
            ✕
          </button>
        </div>
      )}
      <div className={styles.sectionLabel}>Forums</div>
      {posts.length === 0
        ? <EmptySection message={activeTag ? `No posts found for #${activeTag}` : 'Join some spaces to see posts here'} />
        : posts.map((post) => <ForumCard key={post.postId} post={post} />)
      }
    </div>
  );
}

function EmptySection({ message }) {
  return (
    <div className={styles.emptySection}>
      <span className={styles.emptyIcon}>✦</span>
      <span>{message}</span>
    </div>
  );
}
