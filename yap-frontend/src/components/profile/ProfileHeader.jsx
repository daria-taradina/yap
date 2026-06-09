import { useState } from 'react';
import styles from './ProfileHeader.module.css';

/**
 * ProfileHeader
 *
 * Props:
 *   profile {object}  — shape from ProfileData DTO:
 *     - userId        {number}
 *     - username      {string}
 *     - bio           {string}
 *     - bannerUrl     {string}   optional
 *     - avatarUrl     {string}   optional
 *     - followerCount {number}
 *     - followingCount {number}
 *     - communityCount {number}
 *     - isOwnProfile  {boolean}  hides subscribe button
 *
 *   activeTab  {string}
 *   onTabChange {fn}
 */

const TABS = ['Blog Posts', 'Forum Posts', 'Liked'];

function formatCount(n) {
  if (n >= 1000) return (n / 1000).toFixed(1).replace(/\.0$/, '') + 'k';
  return String(n);
}

export default function ProfileHeader({ profile, activeTab, onTabChange }) {
  const [subscribed, setSubscribed] = useState(false);

  const {
    username,
    bio,
    bannerUrl,
    avatarUrl,
    followerCount = 0,
    followingCount = 0,
    communityCount = 0,
    isOwnProfile = false,
  } = profile;

  const initials = username
    ? username.slice(0, 2).toUpperCase()
    : '?';

  return (
    <div className={styles.header}>
      {/* Banner */}
      <div className={styles.banner}>
        {bannerUrl
          ? <img src={bannerUrl} alt="" />
          : <div className={styles.bannerFallback} />}
      </div>

      {/* Avatar — overlaps banner */}
      <div className={styles.avatarWrap}>
        <div className={styles.avatar}>
          {avatarUrl
            ? <img src={avatarUrl} alt={username} />
            : initials}
        </div>
      </div>

      {/* Name + subscribe */}
      <div className={styles.bodyRow}>
        <div className={styles.identityBlock}>
          <span className={styles.displayName}>{username}</span>
          <span className={styles.username}>@{username}</span>
        </div>

        {!isOwnProfile && (
          <button
            className={`${styles.subscribeBtn} ${subscribed ? styles.subscribed : ''}`}
            onClick={() => setSubscribed((v) => !v)}
          >
            {subscribed ? 'Subscribed' : 'Subscribe'}
          </button>
        )}
      </div>

      {/* Bio */}
      {bio && <p className={styles.bio}>{bio}</p>}

      {/* Stats: followers · following · spaces */}
      <div className={styles.stats}>
        <div className={styles.stat}>
          <span className={styles.statNum}>{formatCount(followerCount)}</span>
          <span className={styles.statLabel}>followers</span>
        </div>
        <span className={styles.dot}>·</span>
        <div className={styles.stat}>
          <span className={styles.statNum}>{formatCount(followingCount)}</span>
          <span className={styles.statLabel}>following</span>
        </div>
        <span className={styles.dot}>·</span>
        <div className={styles.stat}>
          <span className={styles.statNum}>{communityCount}</span>
          <span className={styles.statLabel}>spaces</span>
        </div>
      </div>

      {/* Tab bar */}
      <div className={styles.tabs}>
        {TABS.map((tab) => (
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
