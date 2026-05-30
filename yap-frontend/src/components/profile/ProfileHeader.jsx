import { useState } from 'react';
import styles from './ProfileHeader.module.css';

/**
 * ProfileHeader
 *
 * Props:
 *   profile {object}:
 *     - displayName   {string}
 *     - username      {string}   e.g. "@noelle.writes"
 *     - bio           {string}
 *     - bannerUrl     {string}   optional
 *     - avatarUrl     {string}   optional
 *     - writesCount   {number}
 *     - readersCount  {number}
 *     - readingCount  {number}
 *     - spacesCount   {number}
 *     - isOwnProfile  {boolean}  hides subscribe button
 *
 *   activeTab  {string}
 *   onTabChange {fn}
 */

const TABS = ['Writes', 'Forum Posts', 'Liked'];

function formatCount(n) {
  if (n >= 1000) return (n / 1000).toFixed(1).replace(/\.0$/, '') + 'k';
  return String(n);
}

export default function ProfileHeader({ profile, activeTab, onTabChange }) {
  const [subscribed, setSubscribed] = useState(false);

  const {
    displayName,
    username,
    bio,
    bannerUrl,
    avatarUrl,
    writesCount = 0,
    readersCount = 0,
    readingCount = 0,
    spacesCount = 0,
    isOwnProfile = false,
  } = profile;

  const initials = displayName
    .split(' ')
    .map((n) => n[0])
    .join('')
    .slice(0, 2)
    .toUpperCase();

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
            ? <img src={avatarUrl} alt={displayName} />
            : initials}
        </div>
      </div>

      {/* Name + subscribe */}
      <div className={styles.bodyRow}>
        <div className={styles.identityBlock}>
          <span className={styles.displayName}>{displayName}</span>
          <span className={styles.username}>{username}</span>
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

      {/* Stats: 24 writes · 1.2k readers · reading 18 · 6 spaces */}
      <div className={styles.stats}>
        <div className={styles.stat}>
          <span className={styles.statNum}>{formatCount(writesCount)}</span>
          <span className={styles.statLabel}>writes</span>
        </div>
        <span className={styles.dot}>·</span>
        <div className={styles.stat}>
          <span className={styles.statNum}>{formatCount(readersCount)}</span>
          <span className={styles.statLabel}>readers</span>
        </div>
        <span className={styles.dot}>·</span>
        <div className={styles.stat}>
          <span className={styles.statNum}>{formatCount(readingCount)}</span>
          <span className={styles.statLabel}>reading</span>
        </div>
        <span className={styles.dot}>·</span>
        <div className={styles.stat}>
          <span className={styles.statNum}>{spacesCount}</span>
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
