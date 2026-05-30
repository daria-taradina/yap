import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import styles from './CreateDiscussionPage.module.css';

/**
 * CreateDiscussionPage  —  /create-post/discussion
 *
 * Forum discussion creation. Space selector is the first and most
 * prominent field — you decide where it lives before you write.
 * No drafts — discussions go live immediately.
 *
 * Supports deep linking: /create-post/discussion?space=WomenInTech
 *
 * TODO: POST /api/posts
 *   { post_type: 'DISCUSSION', title, content, space_id, tags, is_published: true }
 */

// TODO: replace with GET /api/spaces/joined
const MY_SPACES = [
  { id: '1', name: 'WomenInTech' },
  { id: '2', name: 'Friendship'  },
  { id: '3', name: 'SelfCare'    },
];

export default function CreateDiscussionPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  // Pre-select space if passed via query param
  const preselectedSpace = searchParams.get('space') || '';
  const preselectedId = MY_SPACES.find((s) => s.name === preselectedSpace)?.id || '';

  const [spaceId, setSpaceId] = useState(preselectedId);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [tags, setTags] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit() {
    if (!spaceId) { setError('Please choose a space for your discussion.'); return; }
    if (!title.trim()) { setError('Your discussion needs a title.'); return; }

    const tagList = tags
      .split(',')
      .map((t) => t.trim().toLowerCase())
      .filter(Boolean);

    setSubmitting(true);
    setError('');
    try {
      // TODO: POST /api/posts
      // await fetch('/api/posts', {
      //   method: 'POST',
      //   headers: { 'Content-Type': 'application/json', ...authHeaders },
      //   body: JSON.stringify({
      //     post_type: 'DISCUSSION',
      //     title,
      //     content,
      //     space_id: spaceId,
      //     tags: tagList,
      //     is_published: true,
      //   }),
      // });
      const space = MY_SPACES.find((s) => s.id === spaceId);
      navigate(space ? `/w/${space.name}` : '/');
    } catch {
      setError('Something went wrong. Please try again.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        {/* Space — first and most prominent */}
        <div className={styles.spaceField}>
          <label className={styles.spaceLabel}>Post in</label>
          <select
            className={styles.spaceSelect}
            value={spaceId}
            onChange={(e) => { setSpaceId(e.target.value); setError(''); }}
          >
            <option value="">Choose a space...</option>
            {MY_SPACES.map((s) => (
              <option key={s.id} value={s.id}>w/{s.name}</option>
            ))}
          </select>
        </div>

        {/* Title */}
        <div className={styles.field}>
          <label className={styles.label}>Title</label>
          <input
            className={styles.input}
            placeholder="What do you want to discuss?"
            value={title}
            onChange={(e) => { setTitle(e.target.value); setError(''); }}
          />
        </div>

        {/* Body — optional, markdown-lite */}
        <div className={styles.field}>
          <label className={styles.label}>Body <span style={{ fontWeight: 400, textTransform: 'none', letterSpacing: 0 }}>(optional)</span></label>
          <textarea
            className={styles.textarea}
            placeholder="Add more context, a question, or share your thoughts..."
            value={content}
            onChange={(e) => setContent(e.target.value)}
          />
        </div>

        {/* Tags */}
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
          <button className={styles.cancelBtn} onClick={() => navigate(-1)}>
            Cancel
          </button>
          <button
            className={styles.submitBtn}
            onClick={handleSubmit}
            disabled={submitting}
          >
            {submitting ? 'Posting...' : 'Post'}
          </button>
        </div>
      </div>
    </div>
  );
}
