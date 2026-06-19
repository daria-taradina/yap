import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import SpaceSidebar from '../components/space/SpaceSidebar';
import ForumCard from '../components/feed/ForumCard';
import { api } from '../api';
import styles from './SpacePage.module.css';

export default function SpacePage() {
  const { spaceName } = useParams();
  const navigate = useNavigate();

  const [space, setSpace] = useState(null);
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [joining, setJoining] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  const currentUser = JSON.parse(localStorage.getItem('yap_user') || '{}');
  const isAuthenticated = !!localStorage.getItem('yap_token');

  useEffect(() => {
    setLoading(true);
    api.getSpace(spaceName)
      .then((res) => {
        if (!res.ok) throw new Error('Space not found');
        return res.json();
      })
      .then(async (spaceData) => {
        setSpace(spaceData);
        const postsRes = await api.getSpacePosts(spaceData.communityId);
        const postsData = await postsRes.json();
        setPosts(Array.isArray(postsData) ? postsData : []);
      })
      .catch(() => setError('Space not found'))
      .finally(() => setLoading(false));
  }, [spaceName]);

  function handleNewDiscussion() {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    navigate(`/create-post/discussion?space=${space.name}`);
  }

  async function handleDeletePost(postId) {
    setDeleteError('');
    // Optimistic removal
    const previousPosts = [...posts];
    setPosts((prev) => prev.filter((p) => p.postId !== postId));
    try {
      const res = await api.deletePost(postId);
      if (!res.ok) {
        throw new Error('Failed to delete post');
      }
    } catch {
      // Rollback on failure
      setPosts(previousPosts);
      setDeleteError('Could not delete post. Please try again.');
    }
  }

  async function handleJoin() {
    if (!space) return;
    setJoining(true);
    try {
      const res = await api.joinSpace(space.communityId);
      if (res.ok) {
        setSpace((prev) => ({ ...prev, member: true, memberCount: prev.memberCount + 1 }));
      }
    } finally {
      setJoining(false);
    }
  }

  async function handleLeave() {
    if (!space) return;
    setJoining(true);
    try {
      const res = await api.leaveSpace(space.communityId);
      if (res.ok) {
        setSpace((prev) => ({ ...prev, member: false, memberCount: Math.max(0, prev.memberCount - 1) }));
      }
    } finally {
      setJoining(false);
    }
  }

  if (loading) return <div className={styles.state}>Loading...</div>;
  if (error) return <div className={styles.state}>{error}</div>;
  if (!space) return (
    <div className={styles.layout}>
      <div className={styles.feed}>
        <div className={styles.emptyState}>
          <div className={styles.emptyIcon}>✦</div>
          <p>Space not found</p>
        </div>
      </div>
    </div>
  );

  const isOwner = space.ownerId === currentUser.userId;

  return (
    <div className={styles.layout}>
      <div className={styles.feed}>
        <div className={styles.spaceHeader}>
          <div className={styles.spaceHeaderRow}>
            <h1 className={styles.spaceTitle}>w/{space.name}</h1>
            <button
              className={styles.newPostBtn}
              onClick={handleNewDiscussion}
            >
              <i className="ti ti-plus" /> New Discussion
            </button>
          </div>
          <p className={styles.spaceDesc}>{space.description}</p>
        </div>

        {deleteError && (
          <div className={styles.errorBanner}>{deleteError}</div>
        )}

        {posts.length === 0 ? (
          <div className={styles.emptyState}>
            <div className={styles.emptyIcon}>✦</div>
            <p>No posts yet. Be the first to start a conversation.</p>
          </div>
        ) : (
          posts.map((post) => (
            <ForumCard
              key={post.postId}
              post={post}
              onDelete={handleDeletePost}
            />
          ))
        )}
      </div>

      <div className={styles.sidebar}>
        <SpaceSidebar
          space={space}
          isOwner={isOwner}
          joining={joining}
          onJoin={handleJoin}
          onLeave={handleLeave}
        />
      </div>
    </div>
  );
}