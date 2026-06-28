import { useState, useEffect, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { api } from '../api';
import styles from './CreateDiscussionPage.module.css';

const FLAIR_OPTIONS = [
  { value: 'DISCUSSION', label: 'Discussion', color: '#C4973F' },
  { value: 'SUPPORT', label: 'Support', color: '#1D9E75' },
  { value: 'RANT', label: 'Rant', color: '#C0392B' },
  { value: 'RESOURCE', label: 'Resource', color: '#2E86C1' },
  { value: 'QUESTION', label: 'Question', color: '#7D3C98' },
  { value: 'SENSITIVE', label: 'Sensitive', color: '#D35400' },
];

export default function CreateDiscussionPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const preselectedSpace = searchParams.get('space') || '';

  const [allSpaces, setAllSpaces] = useState([]);
  const [selectedSpace, setSelectedSpace] = useState(null);
  const [spaceSearch, setSpaceSearch] = useState('');
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [guidelines, setGuidelines] = useState('');
  const [guidelinesOpen, setGuidelinesOpen] = useState(false);

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [flair, setFlair] = useState(null);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const dropdownRef = useRef(null);
  const inputRef = useRef(null);

  // Fetch all spaces on mount
  useEffect(() => {
    api.getSpaces()
      .then((res) => res.json())
      .then((data) => {
        const spaces = Array.isArray(data) ? data : [];
        setAllSpaces(spaces);

        // Pre-select from ?space= query param
        if (preselectedSpace) {
          const found = spaces.find(
            (s) => s.name.toLowerCase() === preselectedSpace.toLowerCase()
          );
          if (found) {
            setSelectedSpace(found);
            fetchGuidelines(found.name);
          }
        }
      })
      .catch(() => setAllSpaces([]));
  }, []);

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(e) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setDropdownOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  function fetchGuidelines(spaceName) {
    api.getSpace(spaceName)
      .then((res) => res.json())
      .then((data) => {
        setGuidelines(data.guidelines || '');
      })
      .catch(() => setGuidelines(''));
  }

  function handleSelectSpace(space) {
    setSelectedSpace(space);
    setSpaceSearch('');
    setDropdownOpen(false);
    setError('');
    fetchGuidelines(space.name);
  }

  function handleClearSpace() {
    setSelectedSpace(null);
    setGuidelines('');
    setSpaceSearch('');
    setError('');
    // Re-focus the search input after clearing
    setTimeout(() => inputRef.current?.focus(), 0);
  }

  function handleSearchChange(e) {
    setSpaceSearch(e.target.value);
    if (!dropdownOpen) setDropdownOpen(true);
  }

  function handleInputFocus() {
    if (!selectedSpace) setDropdownOpen(true);
  }

  const filteredSpaces = allSpaces.filter((s) => {
    if (!spaceSearch.trim()) return true;
    const q = spaceSearch.toLowerCase();
    return (
      s.name.toLowerCase().includes(q) ||
      (s.description && s.description.toLowerCase().includes(q))
    );
  });

  async function handleSubmit() {
    if (!selectedSpace) { setError('Please choose a space.'); return; }
    if (!title.trim()) { setError('Your discussion needs a title.'); return; }

    setSubmitting(true);
    setError('');
    try {
      const res = await api.createPost({
        communityId: selectedSpace.communityId,
        title,
        contentText: content,
        postType: 'DISCUSSION',
        flair: flair || null,
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
        {/* Space selector */}
        <div className={styles.spaceField}>
          <label className={styles.spaceLabel}>Post in</label>
          <div className={styles.spaceSelector} ref={dropdownRef}>
            <div
              className={styles.spaceInputWrapper}
              onClick={() => !selectedSpace && inputRef.current?.focus()}
            >
              {selectedSpace ? (
                <span className={styles.selectedChip}>
                  w/{selectedSpace.name}
                  <button
                    type="button"
                    className={styles.chipClear}
                    onClick={handleClearSpace}
                    aria-label="Clear space selection"
                  >
                    ×
                  </button>
                </span>
              ) : (
                <input
                  ref={inputRef}
                  type="text"
                  className={styles.spaceSearchInput}
                  placeholder="Search for a space..."
                  value={spaceSearch}
                  onChange={handleSearchChange}
                  onFocus={handleInputFocus}
                  aria-label="Search spaces"
                />
              )}
            </div>

            {dropdownOpen && !selectedSpace && (
              <div className={styles.spaceDropdown} role="listbox">
                {filteredSpaces.length === 0 ? (
                  <div className={styles.spaceNoResults}>No spaces found</div>
                ) : (
                  filteredSpaces.map((space) => (
                    <div
                      key={space.communityId}
                      className={styles.spaceOption}
                      role="option"
                      onClick={() => handleSelectSpace(space)}
                    >
                      <span className={styles.spaceOptionName}>w/{space.name}</span>
                      <span className={styles.spaceOptionMeta}>
                        {space.description && (
                          <span className={styles.spaceOptionDesc}>
                            {space.description.length > 60
                              ? space.description.slice(0, 60) + '…'
                              : space.description}
                          </span>
                        )}
                        <span>{space.memberCount?.toLocaleString() || 0} members</span>
                      </span>
                    </div>
                  ))
                )}
              </div>
            )}
          </div>
        </div>

        {/* Space guidelines */}
        {guidelines && (
          <div className={styles.guidelinesSection}>
            {/* Mobile toggle */}
            <button
              type="button"
              className={styles.guidelinesToggle}
              onClick={() => setGuidelinesOpen(!guidelinesOpen)}
              aria-expanded={guidelinesOpen}
            >
              Space Guidelines
              <span
                className={`${styles.guidelinesArrow} ${guidelinesOpen ? styles.guidelinesArrowOpen : ''}`}
              >
                ▾
              </span>
            </button>
            {/* Desktop: always visible header */}
            <div className={styles.guidelinesHeader}>Space Guidelines</div>
            {/* Content: on desktop always shown, on mobile toggled */}
            <div
              className={`${styles.guidelinesBody} ${guidelinesOpen ? styles.guidelinesBodyOpen : ''}`}
            >
              <p className={styles.guidelinesContent}>{guidelines}</p>
            </div>
          </div>
        )}

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

        {/* Body */}
        <div className={styles.field}>
          <label className={styles.label}>Body <span style={{ fontWeight: 400 }}>(optional)</span></label>
          <textarea
            className={styles.textarea}
            placeholder="Add more context, a question, or share your thoughts..."
            value={content}
            onChange={(e) => setContent(e.target.value)}
          />
        </div>

        {/* Flair */}
        <div className={styles.field}>
          <label className={styles.label}>Flair <span style={{ fontWeight: 400 }}>(optional)</span></label>
          <div className={styles.flairPicker}>
            {FLAIR_OPTIONS.map((f) => (
              <button
                key={f.value}
                type="button"
                className={`${styles.flairPill} ${flair === f.value ? styles.flairPillActive : ''}`}
                style={{
                  '--flair-color': f.color,
                }}
                onClick={() => setFlair(flair === f.value ? null : f.value)}
              >
                {f.label}
              </button>
            ))}
          </div>
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
