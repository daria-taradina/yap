import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import styles from './CreateSpacePage.module.css';

const CATEGORIES = [
  { label: 'Career & Money',              value: 'CAREER_AND_MONEY' },
  { label: 'Health & Wellness',           value: 'HEALTH_AND_WELLNESS' },
  { label: 'Relationships & Family',      value: 'RELATIONSHIPS_AND_FAMILY' },
  { label: 'Identity & Life Experiences', value: 'IDENTITY_AND_EXPERIENCES' },
  { label: 'Knowledge & Learning',        value: 'KNOWLEDGE_AND_LEARNING' },
  { label: 'Creativity & Expression',     value: 'CREATIVITY_AND_EXPRESSION' },
  { label: 'Hobbies & Interests',         value: 'HOBBIES_AND_INTERESTS' },
  { label: 'Lifestyle & Travel',          value: 'LIFESTYLE_AND_TRAVEL' },
  { label: 'Entertainment & Culture',     value: 'ENTERTAINMENT_AND_CULTURE' },
  { label: 'Technology & Gaming',         value: 'TECHNOLOGY_AND_GAMING' },
  { label: 'Q&As & Stories',             value: 'QA_AND_STORIES' },
  { label: 'Society & Current Issues',    value: 'SOCIETY_AND_CURRENT_ISSUES' },
  { label: 'Support & Sensitive Topics',  value: 'SUPPORT_AND_SENSITIVE_TOPICS' },
  { label: 'Other',                       value: 'OTHER' },
];

export default function CreateSpacePage() {
  const navigate = useNavigate();

  const [form, setForm] = useState({ name: '', description: '', category: '' });
  const [guidelines, setGuidelines] = useState(['']);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    setError('');
  }

  function handleGuidelineChange(index, value) {
    setGuidelines((prev) => prev.map((g, i) => i === index ? value : g));
  }

  function addGuideline() {
    if (guidelines.length >= 8) return;
    setGuidelines((prev) => [...prev, '']);
  }

  function removeGuideline(index) {
    setGuidelines((prev) => prev.filter((_, i) => i !== index));
  }

  async function handleSubmit() {
    if (!form.name.trim()) { setError('Space name is required.'); return; }
    if (!form.description.trim()) { setError('Description is required.'); return; }
    if (!form.category) { setError('Please select a category.'); return; }

    const cleanGuidelines = guidelines.map((g) => g.trim()).filter(Boolean);

    setSubmitting(true);
    try {
      const res = await api.createSpace({
        name: form.name.toLowerCase(),
        description: form.description,
        category: form.category,
        guidelines: cleanGuidelines,
      });

      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        setError(data.error || 'Failed to create space.');
        return;
      }

      const data = await res.json();
      navigate(`/w/${data.name}`);
    } catch {
      setError('Something went wrong. Please try again.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <p className={styles.intro}>
          Spaces are where conversations live. Give yours a clear name,
          a short description, and a few guidelines so your community
          knows what to expect.
        </p>

        <div className={styles.field}>
          <label className={styles.label}>Space Name</label>
          <div className={styles.inputWrap}>
            <span className={styles.prefix}>w/</span>
            <input
              name="name"
              className={`${styles.input} ${styles.inputWithPrefix}`}
              placeholder="womenintech"
              value={form.name}
              onChange={handleChange}
            />
          </div>
          <span className={styles.hint}>Lowercase letters, numbers, and underscores only</span>
        </div>

        <div className={styles.field}>
          <label className={styles.label}>Category</label>
          <select
            name="category"
            className={styles.select}
            value={form.category}
            onChange={handleChange}
          >
            <option value="">Select a category...</option>
            {CATEGORIES.map((c) => (
              <option key={c.value} value={c.value}>{c.label}</option>
            ))}
          </select>
        </div>

        <div className={styles.field}>
          <label className={styles.label}>Description</label>
          <textarea
            name="description"
            className={styles.textarea}
            placeholder="What is this space about? Who is it for?"
            value={form.description}
            onChange={handleChange}
          />
        </div>

        <div className={styles.field}>
          <label className={styles.label}>Guidelines (optional)</label>
          {guidelines.map((g, i) => (
            <div key={i} className={styles.guidelineRow}>
              <span className={styles.guidelineNum}>{i + 1}.</span>
              <input
                className={styles.input}
                placeholder="e.g. Be kind and assume good intent"
                value={g}
                onChange={(e) => handleGuidelineChange(i, e.target.value)}
              />
              {guidelines.length > 1 && (
                <button
                  className={styles.removeBtn}
                  onClick={() => removeGuideline(i)}
                  aria-label="Remove guideline"
                >
                  <i className="ti ti-x" />
                </button>
              )}
            </div>
          ))}
          {guidelines.length < 8 && (
            <button className={styles.addGuidelineBtn} onClick={addGuideline}>
              <i className="ti ti-plus" /> Add guideline
            </button>
          )}
        </div>

        {error && <p className={styles.error}>{error}</p>}

        <div className={styles.footer}>
          <button className={styles.cancelBtn} onClick={() => navigate(-1)}>Cancel</button>
          <button className={styles.submitBtn} onClick={handleSubmit} disabled={submitting}>
            {submitting ? 'Creating...' : 'Create Space'}
          </button>
        </div>
      </div>
    </div>
  );
}