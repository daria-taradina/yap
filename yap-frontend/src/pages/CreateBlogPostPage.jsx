import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import BlogEditor from '../components/post/BlogEditor';
import styles from './CreateBlogPostPage.module.css';

/**
 * CreateBlogPostPage  —  /create-post/blog
 *
 * Blog post creation with TipTap rich editor.
 * Supports draft saving and publishing.
 *
 * TODO: POST /api/posts
 *   { post_type: 'BLOG', title, content (TipTap JSON), tags, is_published }
 */
export default function CreateBlogPostPage() {
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState(null); // TipTap JSON
  const [tags, setTags] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(isPublished) {
    if (!title.trim()) { setError('Your post needs a title.'); return; }
    if (!content) { setError('Write something before publishing.'); return; }

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
      //     post_type: 'BLOG',
      //     title,
      //     content: JSON.stringify(content),
      //     tags: tagList,
      //     is_published: isPublished,
      //   }),
      // });
      navigate('/');
    } catch {
      setError('Something went wrong. Please try again.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className={styles.page}>
      {/* Title — editorial, minimal */}
      <input
        className={styles.titleInput}
        placeholder="Title"
        value={title}
        onChange={(e) => { setTitle(e.target.value); setError(''); }}
      />

      {/* Rich editor */}
      <BlogEditor onChange={setContent} />

      {/* Tags */}
      <div className={styles.field}>
        <label className={styles.label}>Tags</label>
        <input
          className={styles.input}
          placeholder="selfcare, career, mindset"
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
          className={styles.draftBtn}
          onClick={() => handleSubmit(false)}
          disabled={submitting}
        >
          Save Draft
        </button>
        <button
          className={styles.publishBtn}
          onClick={() => handleSubmit(true)}
          disabled={submitting}
        >
          {submitting ? 'Publishing...' : 'Publish'}
        </button>
      </div>
    </div>
  );
}
