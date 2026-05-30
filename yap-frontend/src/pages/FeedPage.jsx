import { useState, useEffect } from 'react';
import FeaturedCard from '../components/feed/FeaturedCard';
import ForumCard from '../components/feed/ForumCard';
import styles from './FeedPage.module.css';

/**
 * FeedPage  —  /
 *
 * Main feed. Fetches featured blog posts and forum posts from API.
 * TODO: GET /api/feed/featured
 * TODO: GET /api/feed/forum
 */
export default function FeedPage() {
  const [featured, setFeatured] = useState([]);
  const [forum, setForum] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // TODO: replace with real API calls
    // const fetchFeed = async () => {
    //   const [featuredRes, forumRes] = await Promise.all([
    //     fetch('/api/feed/featured', { headers: { Authorization: `Bearer ${token}` } }),
    //     fetch('/api/feed/forum',    { headers: { Authorization: `Bearer ${token}` } }),
    //   ]);
    //   setFeatured(await featuredRes.json());
    //   setForum(await forumRes.json());
    //   setLoading(false);
    // };
    // fetchFeed();
    setLoading(false);
  }, []);

  if (loading) return <div className={styles.state}>Loading feed...</div>;

  return (
    <div className={styles.page}>
      <div className={styles.sectionLabel}>Featured</div>
      {featured.length === 0
        ? <EmptySection message="No featured posts yet" />
        : featured.map((post) => <FeaturedCard key={post.id} post={post} />)
      }

      <div className={styles.sectionLabel}>Forums</div>
      {forum.length === 0
        ? <EmptySection message="No forum posts yet" />
        : forum.map((post) => <ForumCard key={post.id} post={post} />)
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
