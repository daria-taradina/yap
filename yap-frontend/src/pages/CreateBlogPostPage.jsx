import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import BlogEditor from '../components/post/BlogEditor';
import { api } from '../api';
import styles from './CreateBlogPostPage.module.css';

export default function CreateBlogPostPage() {
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [content, setContent] = useState(null); // TipTap JSON
  const [tags, setTags] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit() {
    if (!title.trim()) { setError('Your post needs a title.'); return; }
    if (!content) { setError('Write something before publishing.'); return; }

    const tagList = tags
      .split(',')
      .map((t) => t.trim().toLowerCase())
      .filter(Boolean);

    setSubmitting(true);
    setError('');
    try {
      const res = await api.createPost({
        postType: 'BLOG',
        title,
        contentText: JSON.stringify(content),
        tags: tagList,
      });

      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        setError(data.error || 'Failed to publish.');
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
      <input
        className={styles.titleInput}
        placeholder="Title"
        value={title}
        onChange={(e) => { setTitle(e.target.value); setError(''); }}
      />

      <BlogEditor onChange={setContent} />

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
          className={styles.publishBtn}
          onClick={handleSubmit}
          disabled={submitting}
        >
          {submitting ? 'Publishing...' : 'Publish'}
        </button>
      </div>
    </div>
  );
}
