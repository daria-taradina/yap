import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import ProfileHeader from '../components/profile/ProfileHeader';
import ForumCard from '../components/feed/ForumCard';
import { api } from '../api';
import styles from './ProfilePage.module.css';

export default function ProfilePage() {
  const { username } = useParams();
  const [activeTab, setActiveTab] = useState('Blog Posts');
  const [profile, setProfile] = useState(null);
  const [blogPosts, setBlogPosts] = useState([]);
  const [forumPosts, setForumPosts] = useState([]);
  const [liked, setLiked] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getUserProfile(username)
      .then(r => r.json())
      .then(async (profileData) => {
        setProfile(profileData);
        const [blogRes, forumRes, likedRes] = await Promise.all([
          api.getUserBlogPosts(profileData.userId).then(r => r.json()),
          api.getUserPosts(profileData.userId).then(r => r.json()),
          api.getUserLiked(profileData.userId).then(r => r.json()),
        ]);
        setBlogPosts(Array.isArray(blogRes) ? blogRes : []);
        setForumPosts(Array.isArray(forumRes) ? forumRes : []);
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

  const currentPosts =
    activeTab === 'Blog Posts'   ? blogPosts  :
    activeTab === 'Forum Posts'  ? forumPosts :
    liked;

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
