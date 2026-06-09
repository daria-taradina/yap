import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import ForumCard from '../components/feed/ForumCard';
import { api } from '../api';
import styles from './FeedPage.module.css';

export default function ForumsPage() {
  const navigate = useNavigate();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const user = JSON.parse(localStorage.getItem('yap_user') || '{}');
    if (!user.userId) { setLoading(false); return; }

    api.getForumsFeed(user.userId)
      .then((res) => res.json())
      .then((data) => setPosts(Array.isArray(data) ? data : []))
      .catch(() => setPosts([]))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className={styles.state}>Loading...</div>;

  return (
    <div className={styles.page}>
      <div className={styles.sectionLabel}>Discussions</div>
      {posts.length === 0 ? (
        <div className={styles.emptySection}>
          <span className={styles.emptyIcon}>✦</span>
          <span>No discussions yet — join a space and start one.</span>
          <button
            style={{ marginTop: 12, fontSize: 12, color: 'var(--accent)', background: 'none', cursor: 'pointer' }}
            onClick={() => navigate('/explore')}
          >
            Explore spaces →
          </button>
        </div>
      ) : (
        posts.map((post) => <ForumCard key={post.postId} post={post} />)
      )}
    </div>
  );
}
