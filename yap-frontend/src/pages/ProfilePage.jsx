import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import ProfileHeader from '../components/profile/ProfileHeader';
import ForumCard from '../components/feed/ForumCard';
import { api } from '../api';
import styles from './ProfilePage.module.css';

export default function ProfilePage() {
  const { username } = useParams();
  const [activeTab, setActiveTab] = useState('Posts');
  const [profile, setProfile] = useState(null);
  const [posts, setPosts] = useState([]);
  const [liked, setLiked] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getUserProfile(username)
      .then(r => r.json())
      .then(async (profileData) => {
        setProfile(profileData);
        const [postsRes, likedRes] = await Promise.all([
          api.getUserPosts(profileData.userId).then(r => r.json()),
          api.getUserLiked(profileData.userId).then(r => r.json()),
        ]);
        setPosts(Array.isArray(postsRes) ? postsRes : []);
        setLiked(Array.isArray(likedRes) ? likedRes : []);
      })
      .catch(() => setProfile(null))
      .finally(() => setLoading(false));
  }, [username]);

  if (loading) return <div className={styles.state}>Loading...</div>;
  if (!profile) return (
    <div className={styles.page}>
      <div className={styles.emptyState}><div className={styles.emptyIcon}>✦</div><p>User not found</p></div>
    </div>
  );

  const currentPosts = activeTab === 'Posts' ? posts : liked;

  return (
    <div className={styles.page}>
      <ProfileHeader profile={profile} activeTab={activeTab} onTabChange={setActiveTab} />
      {currentPosts.length === 0
        ? <div className={styles.emptyState}><div className={styles.emptyIcon}>✦</div><p>Nothing here yet</p></div>
        : currentPosts.map(post => <ForumCard key={post.postId} post={post} />)
      }
    </div>
  );
}