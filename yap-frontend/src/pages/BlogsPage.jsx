import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import ForumCard from '../components/feed/ForumCard';
import { api } from '../api';
import styles from './FeedPage.module.css';

export default function BlogsPage() {
  const navigate = useNavigate();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getBlogsFeed()
      .then((res) => res.json())
      .then((data) => setPosts(Array.isArray(data) ? data : []))
      .catch(() => setPosts([]))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className={styles.state}>Loading...</div>;

  return (
    <div className={styles.page}>
      <div className={styles.sectionLabel}>Blog Posts</div>
      {posts.length === 0 ? (
        <div className={styles.emptySection}>
          <span className={styles.emptyIcon}>✦</span>
          <span>No blog posts yet — be the first to publish one.</span>
          <button
            style={{ marginTop: 12, fontSize: 12, color: 'var(--accent)', background: 'none', cursor: 'pointer' }}
            onClick={() => navigate('/create-post/blog')}
          >
            Write a blog post →
          </button>
        </div>
      ) : (
        posts.map((post) => <ForumCard key={post.postId} post={post} />)
      )}
    </div>
  );
}
