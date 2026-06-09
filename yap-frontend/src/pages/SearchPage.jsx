import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import ForumCard from '../components/feed/ForumCard';
import { api } from '../api';
import styles from './FeedPage.module.css';

export default function SearchPage() {
  const [searchParams] = useSearchParams();
  const q = searchParams.get('q') || '';
  const navigate = useNavigate();
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!q.trim()) return;
    setLoading(true);
    api.searchPosts(q)
      .then(r => r.json())
      .then(d => setResults(Array.isArray(d) ? d : []))
      .catch(() => setResults([]))
      .finally(() => setLoading(false));
  }, [q]);

  return (
    <div className={styles.page}>
      <div className={styles.sectionLabel}>
        {q ? `Results for "${q}"` : 'Search'}
      </div>
      {loading && <div className={styles.state}>Searching...</div>}
      {!loading && results.length === 0 && q && (
        <div className={styles.emptySection}>
          <span className={styles.emptyIcon}>✦</span>
          <span>No posts found for "{q}"</span>
        </div>
      )}
      {results.map(post => <ForumCard key={post.postId} post={post} />)}
    </div>
  );
}