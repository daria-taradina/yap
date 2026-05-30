import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import SpaceSidebar from '../components/space/SpaceSidebar';
import ForumCard from '../components/feed/ForumCard';
import styles from './SpacePage.module.css';

/**
 * SpacePage  —  /w/:spaceName
 *
 * Forum-only. Blog posts belong to personal /blog/:username pages.
 * Fetches space info + posts from API when backend is ready.
 */
export default function SpacePage() {
  const { spaceName } = useParams();

  const [space, setSpace] = useState(null);
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    // TODO: replace with real API calls
    // GET /api/spaces/{spaceName}
    // GET /api/spaces/{spaceName}/posts
    setLoading(false);
    setSpace(null);
    setPosts([]);
  }, [spaceName]);

  if (loading) return <div className={styles.state}>Loading...</div>;
  if (error)   return <div className={styles.state}>{error}</div>;
  if (!space)  return (
    <div className={styles.layout}>
      <div className={styles.feed}>
        <div className={styles.emptyState}>
          <div className={styles.emptyIcon}>✦</div>
          <p>Space not found</p>
        </div>
      </div>
    </div>
  );

  return (
    <div className={styles.layout}>
      <div className={styles.feed}>
        <div className={styles.spaceHeader}>
          <h1 className={styles.spaceTitle}>{space.name}</h1>
          <div className={styles.spaceHandle}>w/{space.name}</div>
        </div>

        {posts.length === 0 ? (
          <div className={styles.emptyState}>
            <div className={styles.emptyIcon}>✦</div>
            <p>No posts yet. Be the first to start a conversation.</p>
          </div>
        ) : (
          posts.map((post) => <ForumCard key={post.id} post={post} />)
        )}
      </div>

      <div className={styles.sidebar}>
        <SpaceSidebar space={space} />
      </div>
    </div>
  );
}
