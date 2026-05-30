import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import ProfileHeader from '../components/profile/ProfileHeader';
import FeaturedCard from '../components/feed/FeaturedCard';
import ForumCard from '../components/feed/ForumCard';
import styles from './ProfilePage.module.css';

/**
 * ProfilePage  —  /blog/:username
 *
 * Personal blog/column page. Shows a user's writes, forum posts, and liked posts.
 * Fetches data from API when backend is ready.
 */
export default function ProfilePage() {
  const { username } = useParams();
  const [activeTab, setActiveTab] = useState('Writes');

  const [profile, setProfile] = useState(null);
  const [writes, setWrites] = useState([]);
  const [forumPosts, setForumPosts] = useState([]);
  const [liked, setLiked] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    // TODO: replace with real API calls
    // GET /api/users/{username}
    // GET /api/users/{username}/posts/blog
    // GET /api/users/{username}/posts/forum
    // GET /api/users/{username}/liked
    setLoading(false);
    setProfile(null);
  }, [username]);

  if (loading) return <div className={styles.state}>Loading...</div>;

  if (!profile) return (
    <div className={styles.page}>
      <div className={styles.emptyState}>
        <div className={styles.emptyIcon}>✦</div>
        <p>User not found</p>
      </div>
    </div>
  );

  return (
    <div className={styles.page}>
      <ProfileHeader
        profile={profile}
        activeTab={activeTab}
        onTabChange={setActiveTab}
      />

      {activeTab === 'Writes' && (
        writes.length === 0
          ? <EmptyTab message="No writes yet" />
          : writes.map((post) => <FeaturedCard key={post.id} post={post} />)
      )}

      {activeTab === 'Forum Posts' && (
        forumPosts.length === 0
          ? <EmptyTab message="No forum posts yet" />
          : forumPosts.map((post) => <ForumCard key={post.id} post={post} />)
      )}

      {activeTab === 'Liked' && (
        liked.length === 0
          ? <EmptyTab message="No liked posts yet" icon="♡" />
          : liked.map((post) => <FeaturedCard key={post.id} post={post} />)
      )}
    </div>
  );
}

function EmptyTab({ message, icon = '✦' }) {
  return (
    <div className={styles.emptyState}>
      <div className={styles.emptyIcon}>{icon}</div>
      <p>{message}</p>
    </div>
  );
}
