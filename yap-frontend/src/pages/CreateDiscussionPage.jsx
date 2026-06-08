import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { api } from '../api';
import styles from './CreateDiscussionPage.module.css';

export default function CreateDiscussionPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const preselectedSpace = searchParams.get('space') || '';

  const [mySpaces, setMySpaces] = useState([]);
  const [spaceId, setSpaceId] = useState('');
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [tags, setTags] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    api.getMySpaces()
      .then((res) => res.json())
      .then((data) => {
        const spaces = Array.isArray(data) ? data : [];
        setMySpaces(spaces);
        if (preselectedSpace) {
          const found = spaces.find((s) => s.name === preselectedSpace.toLowerCase());
          if (found) setSpaceId(String(found.communityId));
        }
      })
      .catch(() => setMySpaces([]));
  }, []);

  async function handleSubmit() {
    if (!spaceId) { setError('Please choose a space.'); return; }
    if (!title.trim()) { setError('Your discussion needs a title.'); return; }

    const tagList = tags.split(',').map((t) => t.trim().toLowerCase()).filter(Boolean);

    setSubmitting(true);
    setError('');
    try {
      const res = await api.createPost({
        communityId: parseInt(spaceId),
        title,
        contentText: content,
        postType: 'DISCUSSION',
        tags: tagList,
      });

      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        setError(data.error || 'Failed to post.');
        return;
      }

      const data = await res.json();
      navigate(`/post/${data.postId}`);
    } catch {
      setError('Something went wrong. Please try again.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <div className={styles.spaceField}>
          <label className={styles.spaceLabel}>Post in</label>
          <select
            className={styles.spaceSelect}
            value={spaceId}
            onChange={(e) => { setSpaceId(e.target.value); setError(''); }}
          >
            <option value="">Choose a space...</option>
            {mySpaces.map((s) => (
              <option key={s.communityId} value={String(s.communityId)}>w/{s.name}</option>
            ))}
          </select>
        </div>

        <div className={styles.field}>
          <label className={styles.label}>Title</label>
          <input
            className={styles.input}
            placeholder="What do you want to discuss?"
            value={title}
            onChange={(e) => { setTitle(e.target.value); setError(''); }}
          />
        </div>

        <div className={styles.field}>
          <label className={styles.label}>Body <span style={{ fontWeight: 400 }}>(optional)</span></label>
          <textarea
            className={styles.textarea}
            placeholder="Add more context, a question, or share your thoughts..."
            value={content}
            onChange={(e) => setContent(e.target.value)}
          />
        </div>

        <div className={styles.field}>
          <label className={styles.label}>Tags</label>
          <input
            className={styles.input}
            placeholder="career, mindset, relationships"
            value={tags}
            onChange={(e) => setTags(e.target.value)}
          />
          <span className={styles.hint}>Separate with commas</span>
        </div>

        {error && <p className={styles.error}>{error}</p>}

        <div className={styles.footer}>
          <button className={styles.cancelBtn} onClick={() => navigate(-1)}>Cancel</button>
          <button className={styles.submitBtn} onClick={handleSubmit} disabled={submitting}>
            {submitting ? 'Posting...' : 'Post'}
          </button>
        </div>
      </div>
    </div>
  );
}