import { useState, useEffect } from 'react';
import { api } from '../api';
import styles from './ModQueuePage.module.css';

export default function ModQueuePage() {
  const [queue, setQueue] = useState([]);
  const [allDecisions, setAllDecisions] = useState([]);
  const [tab, setTab] = useState('queue'); // 'queue' | 'all'
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    try {
      const [qRes, dRes] = await Promise.all([
        api.getModerationQueue(),
        api.getAllDecisions(),
      ]);
      const qData = await qRes.json();
      const dData = await dRes.json();
      setQueue(Array.isArray(qData) ? qData : []);
      setAllDecisions(Array.isArray(dData) ? dData : []);
    } catch (err) {
      console.error('Failed to load moderation data', err);
    } finally {
      setLoading(false);
    }
  }

  async function handleOverride(decisionId, action) {
    try {
      await api.overrideDecision(decisionId, action);
      loadData(); // refresh
    } catch (err) {
      console.error('Override failed', err);
    }
  }

  const items = tab === 'queue' ? queue : allDecisions;

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h2 className={styles.title}>Moderation Dashboard</h2>
        <p className={styles.subtitle}>
          AI-flagged content for human review. Moderators can override AI decisions.
        </p>
      </div>

      <div className={styles.tabs}>
        <button
          className={`${styles.tab} ${tab === 'queue' ? styles.activeTab : ''}`}
          onClick={() => setTab('queue')}
        >
          Review Queue ({queue.length})
        </button>
        <button
          className={`${styles.tab} ${tab === 'all' ? styles.activeTab : ''}`}
          onClick={() => setTab('all')}
        >
          All Decisions ({allDecisions.length})
        </button>
      </div>

      {loading && <div className={styles.state}>Loading...</div>}

      {!loading && items.length === 0 && (
        <div className={styles.empty}>
          {tab === 'queue' ? 'No content flagged for review' : 'No moderation decisions yet'}
        </div>
      )}

      {!loading && items.map((item) => (
        <div key={item.decisionId} className={styles.card}>
          <div className={styles.cardHeader}>
            <span className={styles.author}>@{item.authorUsername}</span>
            <span className={styles.timestamp}>
              {item.createdAt ? new Date(item.createdAt).toLocaleString() : ''}
            </span>
          </div>

          <p className={styles.content}>{item.contentText}</p>

          <div className={styles.badges}>
            <span className={`${styles.badge} ${styles[`action_${item.action}`]}`}>
              {item.action}
            </span>
            <span className={`${styles.badge} ${styles[`risk_${item.risk}`]}`}>
              {item.risk} risk
            </span>
            <span className={styles.badge}>
              {item.category?.replace('_', ' ')}
            </span>
          </div>

          {item.reasoning && (
            <p className={styles.reasoning}>AI: {item.reasoning}</p>
          )}

          {tab === 'queue' && (
            <div className={styles.actions}>
              <button
                className={`${styles.actionBtn} ${styles.allowBtn}`}
                onClick={() => handleOverride(item.decisionId, 'ALLOW')}
              >
                ✓ Approve
              </button>
              <button
                className={`${styles.actionBtn} ${styles.hideBtn}`}
                onClick={() => handleOverride(item.decisionId, 'HIDE')}
              >
                ✕ Remove
              </button>
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
