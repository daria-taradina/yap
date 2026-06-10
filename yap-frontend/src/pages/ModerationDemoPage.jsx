import { useState } from 'react';
import { api } from '../api';
import styles from './ModerationDemoPage.module.css';

const EXAMPLE_POSTS = [
  { label: 'Support Seeking', text: 'My ex-boyfriend abused me and I finally left. I need support from other women who have been through this.' },
  { label: 'Harassment', text: 'Women belong in the kitchen. They should stop pretending to be engineers.' },
  { label: 'Threat', text: 'I want to hurt someone who wronged me. They deserve to suffer.' },
  { label: 'Health Discussion', text: 'Has anyone tried therapy for postpartum depression? I want to hear your experiences.' },
  { label: 'Misinformation', text: 'Vaccines cause autism. Big pharma is hiding the truth from all of us.' },
  { label: 'Normal Discussion', text: 'What are your favorite books by female authors? Looking for recommendations!' },
];

export default function ModerationDemoPage() {
  const [content, setContent] = useState('');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [history, setHistory] = useState([]);

  async function handleClassify() {
    if (!content.trim()) return;
    setLoading(true);
    setResult(null);
    try {
      const res = await api.classifyContent(content);
      const data = await res.json();
      setResult(data);
      setHistory((prev) => [{ content, ...data, timestamp: new Date() }, ...prev]);
    } catch (err) {
      setResult({ error: 'Classification failed — check if AWS Bedrock is configured.' });
    } finally {
      setLoading(false);
    }
  }

  function loadExample(text) {
    setContent(text);
    setResult(null);
  }

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h2 className={styles.title}>AI Content Moderation</h2>
        <p className={styles.subtitle}>
          Context-aware classification powered by Amazon Bedrock. Unlike keyword filters,
          this system understands <em>intent</em>.
        </p>
      </div>

      <div className={styles.demoArea}>
        <div className={styles.inputSection}>
          <label className={styles.label}>Content to classify</label>
          <textarea
            className={styles.textarea}
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="Type or paste content to analyze..."
            rows={4}
          />
          <button
            className={styles.classifyBtn}
            onClick={handleClassify}
            disabled={loading || !content.trim()}
          >
            {loading ? 'Analyzing...' : 'Classify with AI'}
          </button>
        </div>

        {result && !result.error && (
          <div className={styles.resultCard}>
            <div className={styles.resultHeader}>AI Decision</div>
            <div className={styles.resultGrid}>
              <div className={styles.resultItem}>
                <span className={styles.resultLabel}>Category</span>
                <span className={`${styles.badge} ${styles[`cat_${result.category}`]}`}>
                  {result.category?.replace('_', ' ')}
                </span>
              </div>
              <div className={styles.resultItem}>
                <span className={styles.resultLabel}>Risk Level</span>
                <span className={`${styles.badge} ${styles[`risk_${result.risk}`]}`}>
                  {result.risk}
                </span>
              </div>
              <div className={styles.resultItem}>
                <span className={styles.resultLabel}>Action</span>
                <span className={`${styles.badge} ${styles[`action_${result.action}`]}`}>
                  {result.action}
                </span>
              </div>
            </div>
            <div className={styles.reasoning}>
              <span className={styles.resultLabel}>Reasoning</span>
              <p>{result.reasoning}</p>
            </div>
          </div>
        )}

        {result?.error && (
          <div className={styles.errorCard}>{result.error}</div>
        )}
      </div>

      <div className={styles.examples}>
        <div className={styles.label}>Try these examples</div>
        <div className={styles.exampleGrid}>
          {EXAMPLE_POSTS.map((ex, i) => (
            <button
              key={i}
              className={styles.exampleBtn}
              onClick={() => loadExample(ex.text)}
            >
              {ex.label}
            </button>
          ))}
        </div>
      </div>

      {history.length > 0 && (
        <div className={styles.historySection}>
          <div className={styles.label}>Classification History</div>
          {history.map((item, i) => (
            <div key={i} className={styles.historyItem}>
              <p className={styles.historyContent}>"{item.content.slice(0, 80)}..."</p>
              <div className={styles.historyBadges}>
                <span className={`${styles.badgeSm} ${styles[`action_${item.action}`]}`}>{item.action}</span>
                <span className={`${styles.badgeSm} ${styles[`risk_${item.risk}`]}`}>{item.risk}</span>
                <span className={styles.badgeSm}>{item.category?.replace('_', ' ')}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
