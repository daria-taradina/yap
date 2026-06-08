import { useState, useEffect } from 'react';
import ForumCard from '../components/feed/ForumCard';
import { api } from '../api';
import styles from './FeedPage.module.css';

export default function FeedPage() {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const user = JSON.parse(localStorage.getItem('yap_user') || '{}');
    if (!user.userId) { setLoading(false); return; }

    api.getFeed(user.userId)
      .then((res) => res.json())
      .then((data) => setPosts(Array.isArray(data) ? data : []))
      .catch(() => setPosts([]))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className={styles.state}>Loading feed...</div>;

  return (
    <div className={styles.page}>
      <div className={styles.sectionLabel}>Forums</div>
      {posts.length === 0
        ? <EmptySection message="Join some spaces to see posts here" />
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