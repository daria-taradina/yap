import styles from './ProfileHeader.module.css';

function formatCount(n) {
  if (n >= 1000) return (n / 1000).toFixed(1).replace(/\.0$/, '') + 'k';
  return String(n ?? 0);
}

export default function ProfileHeader({
  profile,
  isOwnProfile,
  editMode,
  bioInput,
  onBioChange,
  onEditToggle,
  onSaveBio,
  saving,
  onAvatarClick,
  uploadingAvatar,
  activeTab,
  onTabChange,
  postCount = 0,
}) {
  const {
    username,
    bio,
    avatarUrl,
    followerCount = 0,
    followingCount = 0,
  } = profile;

  const initials = username ? username.slice(0, 2).toUpperCase() : '?';
  const tabs = isOwnProfile ? ['Posts', 'Liked', 'Saved'] : ['Posts', 'Liked'];

  return (
    <div className={styles.header}>
      {/* Avatar */}
      <div className={styles.avatarWrap}>
        <div
          className={`${styles.avatar} ${editMode ? styles.avatarEditable : ''}`}
          onClick={onAvatarClick}
          title={editMode ? 'Click to change photo' : undefined}
        >
          {uploadingAvatar ? (
            <span className={styles.uploading}>...</span>
          ) : avatarUrl ? (
            <img src={avatarUrl} alt={username} />
          ) : initials}
          {editMode && (
            <div className={styles.avatarOverlay}>
              <i className="ti ti-camera" />
            </div>
          )}
        </div>
      </div>

      {/* Name row */}
      <div className={styles.bodyRow}>
        <div className={styles.identityBlock}>
          <span className={styles.displayName}>{username}</span>
          <span className={styles.username}>@{username}</span>
        </div>

        <div className={styles.ownerActions}>
          {isOwnProfile ? (
            editMode ? (
              <div className={styles.editActions}>
                <button className={styles.cancelBtn} onClick={onEditToggle}>Cancel</button>
                <button className={styles.saveBtn} onClick={onSaveBio} disabled={saving}>
                  {saving ? 'Saving...' : 'Save'}
                </button>
              </div>
            ) : (
              <>
                <button className={styles.editBtn} onClick={onEditToggle}>
                  <i className="ti ti-edit" /> Edit profile
                </button>
                <button
                  className={styles.gearBtn}
                  onClick={() => alert('Settings coming soon!')}
                  title="Settings"
                >
                  <i className="ti ti-settings" />
                </button>
              </>
            )
          ) : (
            <button className={styles.subscribeBtn}>Follow</button>
          )}
        </div>
      </div>

      {/* Bio */}
      {editMode ? (
        <textarea
          className={styles.bioInput}
          placeholder="Write a short bio..."
          value={bioInput}
          onChange={e => onBioChange(e.target.value)}
          maxLength={150}
        />
      ) : (
        bio && <p className={styles.bio}>{bio}</p>
      )}

      {/* Stats */}
      <div className={styles.stats}>
        <div className={styles.stat}>
          <span className={styles.statNum}>{formatCount(postCount)}</span>
          <span className={styles.statLabel}>posts</span>
        </div>
        <span className={styles.dot}>·</span>
        <div className={styles.stat}>
          <span className={styles.statNum}>{formatCount(followerCount)}</span>
          <span className={styles.statLabel}>followers</span>
        </div>
        <span className={styles.dot}>·</span>
        <div className={styles.stat}>
          <span className={styles.statNum}>{formatCount(followingCount)}</span>
          <span className={styles.statLabel}>following</span>
        </div>
      </div>

      {/* Tabs */}
      <div className={styles.tabs}>
        {tabs.map((tab) => (
          <button
            key={tab}
            className={`${styles.tab} ${activeTab === tab ? styles.active : ''}`}
            onClick={() => onTabChange(tab)}
          >
            {tab}
          </button>
        ))}
      </div>
    </div>
  );
}