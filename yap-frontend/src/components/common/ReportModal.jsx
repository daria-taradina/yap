import { useState } from 'react';
import { api } from '../../api';
import styles from './ReportModal.module.css';

const REPORT_REASONS = [
  { value: 'HARASSMENT', label: 'Harassment' },
  { value: 'HATE_SPEECH', label: 'Hate Speech' },
  { value: 'SPAM', label: 'Spam' },
  { value: 'MISINFORMATION', label: 'Misinformation' },
  { value: 'SELF_HARM', label: 'Self-Harm' },
  { value: 'OTHER', label: 'Other' },
];

export default function ReportModal({ isOpen, onClose, postId, commentId }) {
  const [reason, setReason] = useState('');
  const [details, setDetails] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [success, setSuccess] = useState(false);

  if (!isOpen) return null;

  function handleOverlayClick(e) {
    if (e.target === e.currentTarget) {
      handleClose();
    }
  }

  function handleClose() {
    setReason('');
    setDetails('');
    setError('');
    setSuccess(false);
    onClose();
  }

  async function handleSubmit(e) {
    e.preventDefault();

    if (!reason) {
      setError('Please select a reason for your report.');
      return;
    }

    setError('');
    setSubmitting(true);

    try {
      const body = { reason };
      if (postId) body.postId = postId;
      if (commentId) body.commentId = commentId;
      if (details.trim()) body.details = details.trim();

      const res = await api.reportContent(body);

      if (res.status === 409) {
        setError('You have already reported this content.');
        return;
      }

      if (!res.ok) {
        setError('Something went wrong. Please try again.');
        return;
      }

      setSuccess(true);
      setTimeout(() => {
        handleClose();
      }, 2000);
    } catch {
      setError('Something went wrong. Please try again.');
    } finally {
      setSubmitting(false);
    }
  }

  if (success) {
    return (
      <div className={styles.overlay} onClick={handleOverlayClick}>
        <div className={styles.modal}>
          <div className={styles.success}>
            <div className={styles.successIcon}>
              <i className="ti ti-circle-check" />
            </div>
            <p>Report submitted. Thank you for helping keep Yap safe.</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.overlay} onClick={handleOverlayClick}>
      <div className={styles.modal}>
        <div className={styles.header}>
          <h2 className={styles.title}>Report Content</h2>
          <button className={styles.closeBtn} onClick={handleClose} aria-label="Close">
            <i className="ti ti-x" />
          </button>
        </div>

        {error && <div className={styles.error}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className={styles.reasonGroup}>
            <span className={styles.reasonLabel}>Why are you reporting this?</span>
            {REPORT_REASONS.map((r) => (
              <label key={r.value} className={styles.radioOption}>
                <input
                  type="radio"
                  name="reportReason"
                  value={r.value}
                  checked={reason === r.value}
                  onChange={() => setReason(r.value)}
                />
                <span>{r.label}</span>
              </label>
            ))}
          </div>

          <label className={styles.detailsLabel}>Additional details (optional)</label>
          <textarea
            className={styles.detailsTextarea}
            placeholder="Provide more context if you'd like..."
            value={details}
            onChange={(e) => setDetails(e.target.value)}
            maxLength={500}
          />
          <div className={styles.charCount}>{details.length}/500</div>

          <div className={styles.actions}>
            <button type="button" className={styles.cancelBtn} onClick={handleClose}>
              Cancel
            </button>
            <button
              type="submit"
              className={styles.submitBtn}
              disabled={!reason || submitting}
            >
              {submitting ? 'Submitting...' : 'Submit Report'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
