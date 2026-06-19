import { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import ProfileHeader from '../components/profile/ProfileHeader';
import ForumCard from '../components/feed/ForumCard';
import { api } from '../api';
import styles from './ProfilePage.module.css';

export default function ProfilePage() {
  const { username } = useParams();
  const currentUser = JSON.parse(localStorage.getItem('yap_user') || '{}');
  const isOwnProfile = currentUser.username === username;

  const [activeTab, setActiveTab] = useState('Discussions');
  const [profile, setProfile] = useState(null);
  const [forumPosts, setForumPosts] = useState([]);
  const [liked, setLiked] = useState([]);
  const [loading, setLoading] = useState(true);

  // edit mode state
  const [editMode, setEditMode] = useState(false);
  const [bioInput, setBioInput] = useState('');
  const [saving, setSaving] = useState(false);
  const [uploadingAvatar, setUploadingAvatar] = useState(false);
  const avatarInputRef = useRef(null);

  useEffect(() => {
    setLoading(true);
    api.getUserProfile(username)
      .then(r => r.json())
      .then(async (profileData) => {
        setProfile(profileData);
        setBioInput(profileData.bio || '');
        const [forumRes, likedRes] = await Promise.all([
          api.getUserPosts(profileData.userId).then(r => r.json()),
          api.getUserLiked(profileData.userId).then(r => r.json()),
        ]);
        setForumPosts(Array.isArray(forumRes) ? forumRes : []);
        setLiked(Array.isArray(likedRes) ? likedRes : []);
      })
      .catch(() => setProfile(null))
      .finally(() => setLoading(false));
  }, [username]);

  async function handleSaveBio() {
    setSaving(true);
    try {
      await api.updateBio(bioInput);
      setProfile(prev => ({ ...prev, bio: bioInput }));
      setEditMode(false);
    } catch {
    } finally {
      setSaving(false);
    }
  }

  async function handleAvatarUpload(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploadingAvatar(true);
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('upload_preset', 'yap_unsigned'); // set this in Cloudinary
      const res = await fetch(`https://api.cloudinary.com/v1_1/${import.meta.env.VITE_CLOUDINARY_CLOUD_NAME}/image/upload`, {
        method: 'POST',
        body: formData,
      });
      const data = await res.json();
      if (data.secure_url) {
        await api.updateAvatar(data.secure_url);
        setProfile(prev => ({ ...prev, avatarUrl: data.secure_url }));
        // update localStorage too
        const user = JSON.parse(localStorage.getItem('yap_user') || '{}');
        localStorage.setItem('yap_user', JSON.stringify({ ...user, avatarUrl: data.secure_url }));
      }
    } catch {
    } finally {
      setUploadingAvatar(false);
    }
  }

  if (loading) return <div className={styles.state}>Loading...</div>;
  if (!profile) return (
    <div className={styles.page}>
      <div className={styles.emptyState}><div className={styles.emptyIcon}>✦</div><p>User not found</p></div>
    </div>
  );

  const currentPosts = activeTab === 'Discussions' ? forumPosts : liked;

  return (
    <div className={styles.page}>
      <ProfileHeader
        profile={profile}
        isOwnProfile={isOwnProfile}
        editMode={editMode}
        bioInput={bioInput}
        onBioChange={setBioInput}
        onEditToggle={() => setEditMode(v => !v)}
        onSaveBio={handleSaveBio}
        saving={saving}
        onAvatarClick={() => isOwnProfile && editMode && avatarInputRef.current?.click()}
        uploadingAvatar={uploadingAvatar}
        activeTab={activeTab}
        onTabChange={setActiveTab}
      />

      {/* hidden file inputs */}
      {isOwnProfile && (
        <input
          ref={avatarInputRef}
          type="file"
          accept="image/*"
          style={{ display: 'none' }}
          onChange={handleAvatarUpload}
        />
      )}

      {currentPosts.length === 0
        ? <div className={styles.emptyState}><div className={styles.emptyIcon}>✦</div><p>Nothing here yet</p></div>
        : currentPosts.map(post =>
            <ForumCard key={post.postId} post={post} />
          )
      }
    </div>
  );
}
