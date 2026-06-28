import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../api';
import Avatar from '../components/common/Avatar';
import ReportModal from '../components/common/ReportModal';
import styles from './PostDetailPage.module.css';

const FLAIR_COLORS = {
  DISCUSSION: '#C4973F',
  SUPPORT: '#1D9E75',
  RANT: '#C0392B',
  RESOURCE: '#2E86C1',
  QUESTION: '#7D3C98',
  SENSITIVE: '#D35400',
};

export default function PostDetailPage() {
  const { postId } = useParams();
  const navigate = useNavigate();

  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [liked, setLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(0);
  const [commentText, setCommentText] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleteError, setDeleteError] = useState('');
  const [showPostOverflow, setShowPostOverflow] = useState(false);
  const [showPostReport, setShowPostReport] = useState(false);
  const [showCopied, setShowCopied] = useState(false);
  const [bookmarked, setBookmarked] = useState(false);
  const postOverflowRef = useRef(null);

  const token = localStorage.getItem('yap_token');
  const currentUser = token ? JSON.parse(localStorage.getItem('yap_user') || '{}') : null;

  useEffect(() => {
    Promise.all([
      api.getPost(postId).then(r => r.json()),
      api.getComments(postId).then(r => r.json()),
    ]).then(([postData, commentsData]) => {
      setPost(postData);
      setLiked(postData.likedByCurrentUser);
      setLikeCount(postData.likeCount);
      setBookmarked(!!postData.bookmarked);
      setComments(Array.isArray(commentsData) ? commentsData : []);
    }).catch(() => setPost(null))
      .finally(() => setLoading(false));
  }, [postId]);

  useEffect(() => {
    function handleClickOutside(e) {
      if (postOverflowRef.current && !postOverflowRef.current.contains(e.target)) {
        setShowPostOverflow(false);
      }
    }
    if (showPostOverflow) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [showPostOverflow]);

  async function handleLike() {
    try {
      if (liked) {
        await api.unlikePost(postId);
        setLiked(false); setLikeCount(n => Math.max(0, n - 1));
      } else {
        await api.likePost(postId);
        setLiked(true); setLikeCount(n => n + 1);
      }
    } catch {}
  }

  async function handleDelete() {
    setDeleteError('');
    try {
      const res = await api.deletePost(postId);
      if (!res.ok) throw new Error('Failed to delete');
      // Navigate back to the space or home after successful delete
      if (post.communityName) {
        navigate(`/w/${post.communityName}`);
      } else {
        navigate('/');
      }
    } catch {
      setDeleteError('Could not delete post. Please try again.');
      setShowDeleteConfirm(false);
    }
  }

  function handleShareClick() {
    const url = `${window.location.origin}/post/${postId}`;
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(url).then(() => {
        setShowCopied(true);
        setTimeout(() => setShowCopied(false), 1500);
      });
    } else {
      const input = document.createElement('input');
      input.style.position = 'fixed';
      input.style.opacity = '0';
      input.value = url;
      document.body.appendChild(input);
      input.select();
      document.execCommand('copy');
      document.body.removeChild(input);
      setShowCopied(true);
      setTimeout(() => setShowCopied(false), 1500);
    }
  }

  async function handleBookmark() {
    if (!token) {
      navigate('/login');
      return;
    }
    setBookmarked(prev => !prev);
    try {
      await api.toggleBookmark(postId);
    } catch {
      setBookmarked(prev => !prev);
    }
  }

  async function handleComment() {
    if (!commentText.trim()) return;
    setSubmitting(true);
    try {
      const res = await api.createComment({ postId: parseInt(postId), contentText: commentText });
      const newComment = await res.json();
      setComments(prev => [newComment, ...prev]);
      setCommentText('');
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <div className={styles.state}>Loading...</div>;
  if (!post)   return <div className={styles.state}>Post not found.</div>;

  return (
    <div className={styles.page}>
      <div className={styles.post}>
        <div className={styles.forumMeta}>
          {post.communityName && (
            <span className={styles.space} onClick={() => navigate(`/w/${post.communityName}`)}>
              w/{post.communityName}
            </span>
          )}
          <span className={styles.forumUsername}>@{post.authorUsername}</span>
          {post.createdAt && (
            <span className={styles.timestamp}>· {new Date(post.createdAt).toLocaleDateString()}</span>
          )}
        </div>

        <h1 className={styles.title}>{post.title}</h1>
        <p className={styles.body}>{post.contentText}</p>

        {post.flair && (
          <span
            className={styles.flair}
            style={{ backgroundColor: FLAIR_COLORS[post.flair] || '#888' }}
          >
            {post.flair === 'SENSITIVE' && <span aria-label="Warning">⚠️</span>}
            {post.flair}
          </span>
        )}

        {deleteError && (
          <div className={styles.deleteError}>{deleteError}</div>
        )}

        <div className={styles.actions}>
          <button className={`${styles.actionBtn} ${liked ? styles.liked : ''}`} onClick={handleLike}>
            <i className={liked ? 'ti ti-heart-filled' : 'ti ti-heart'} /> {likeCount}
          </button>
          <button className={styles.actionBtn}>
            <i className="ti ti-message-circle" /> {comments.length}
          </button>
          <span className={styles.shareBtnWrapper}>
            <button
              className={styles.actionBtn}
              onClick={handleShareClick}
              aria-label="Copy link to post"
              title="Copy link"
            >
              <i className="ti ti-link" />
            </button>
            {showCopied && (
              <span className={styles.copiedTooltip}>Link copied!</span>
            )}
          </span>
          <button
            className={`${styles.actionBtn} ${bookmarked ? styles.bookmarkActive : ''}`}
            onClick={handleBookmark}
            aria-label={bookmarked ? 'Remove bookmark' : 'Bookmark post'}
            title={bookmarked ? 'Remove bookmark' : 'Bookmark'}
          >
            <i className={bookmarked ? 'ti ti-bookmark-filled' : 'ti ti-bookmark'} />
          </button>
          {post.canDelete && (
            <button
              className={`${styles.actionBtn} ${styles.deleteBtn}`}
              onClick={() => setShowDeleteConfirm(true)}
              aria-label="Delete post"
            >
              <i className="ti ti-trash" /> Delete
            </button>
          )}
          {currentUser && post.authorUsername !== currentUser.username && (
            <div className={styles.overflowWrapper} ref={postOverflowRef}>
              <button
                className={styles.overflowBtn}
                onClick={() => setShowPostOverflow(v => !v)}
                aria-label="More options"
              >
                <i className="ti ti-dots" />
              </button>
              {showPostOverflow && (
                <div className={styles.overflowMenu}>
                  <button
                    className={styles.overflowItem}
                    onClick={() => { setShowPostOverflow(false); setShowPostReport(true); }}
                  >
                    <i className="ti ti-flag" /> Report
                  </button>
                </div>
              )}
            </div>
          )}
        </div>

        {showDeleteConfirm && (
          <div className={styles.confirmDialog}>
            <p className={styles.confirmText}>Delete this post? This action cannot be undone.</p>
            <div className={styles.confirmActions}>
              <button className={styles.confirmCancelBtn} onClick={() => setShowDeleteConfirm(false)}>Cancel</button>
              <button className={styles.confirmDeleteBtn} onClick={handleDelete}>Delete</button>
            </div>
          </div>
        )}

        <ReportModal
          isOpen={showPostReport}
          onClose={() => setShowPostReport(false)}
          postId={parseInt(postId)}
        />
      </div>

      <div className={styles.commentsSection}>
        <div className={styles.commentsLabel}>Replies</div>

        <div className={styles.composer}>
          <textarea
            className={styles.composerInput}
            placeholder="Share your thoughts..."
            value={commentText}
            onChange={e => setCommentText(e.target.value)}
          />
          <div className={styles.composerFooter}>
            <button
              className={styles.submitBtn}
              onClick={handleComment}
              disabled={!commentText.trim() || submitting}
            >
              {submitting ? 'Posting...' : 'Reply'}
            </button>
          </div>
        </div>

        {comments.length === 0
          ? <div className={styles.emptyComments}>No replies yet — start the conversation.</div>
          : comments.map(c => <CommentItem key={c.commentId} comment={c} postId={postId} />)
        }
      </div>
    </div>
  );
}

function CommentItem({ comment, postId }) {
  const [liked, setLiked] = useState(comment.likedByCurrentUser);
  const [likeCount, setLikeCount] = useState(comment.likeCount || 0);
  const [replyText, setReplyText] = useState('');
  const [showReply, setShowReply] = useState(false);
  const [replies, setReplies] = useState(comment.replies || []);
  const [submitting, setSubmitting] = useState(false);
  const [showOverflow, setShowOverflow] = useState(false);
  const [showReportModal, setShowReportModal] = useState(false);
  const overflowRef = useRef(null);

  const token = localStorage.getItem('yap_token');
  const currentUser = token ? JSON.parse(localStorage.getItem('yap_user') || '{}') : null;
  const canReport = currentUser && comment.authorUsername !== currentUser.username;

  useEffect(() => {
    function handleClickOutside(e) {
      if (overflowRef.current && !overflowRef.current.contains(e.target)) {
        setShowOverflow(false);
      }
    }
    if (showOverflow) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [showOverflow]);

  async function handleLike() {
    try {
      if (liked) {
        await api.unlikeComment(comment.commentId);
        setLiked(false); setLikeCount(n => Math.max(0, n - 1));
      } else {
        await api.likeComment(comment.commentId);
        setLiked(true); setLikeCount(n => n + 1);
      }
    } catch {}
  }

  async function handleReply() {
    if (!replyText.trim()) return;
    setSubmitting(true);
    try {
      const res = await api.createComment({
        postId: parseInt(postId),
        parentCommentId: comment.commentId,
        contentText: replyText
      });
      const newReply = await res.json();
      setReplies(prev => [...prev, newReply]);
      setReplyText('');
      setShowReply(false);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className={styles.comment}>
      <div className={styles.commentMeta}>
        <Avatar src={comment.authorAvatarUrl} name={comment.authorUsername} size="sm" />
        <span className={styles.commentAuthor}>@{comment.authorUsername}</span>
        {comment.createdAt && (
          <span className={styles.commentTime}>· {new Date(comment.createdAt).toLocaleDateString()}</span>
        )}
      </div>
      <p className={styles.commentBody}>{comment.contentText}</p>
      <div className={styles.commentActions}>
        <span className={styles.commentLike} onClick={handleLike}>
          <i className={liked ? 'ti ti-heart-filled' : 'ti ti-heart'} />
          {likeCount > 0 && likeCount}
        </span>
        <button className={styles.replyBtn} onClick={() => setShowReply(v => !v)}>
          Reply
        </button>
        {canReport && (
          <div className={styles.overflowWrapper} ref={overflowRef}>
            <button
              className={styles.overflowBtn}
              onClick={() => setShowOverflow(v => !v)}
              aria-label="More options"
            >
              <i className="ti ti-dots" />
            </button>
            {showOverflow && (
              <div className={styles.overflowMenu}>
                <button
                  className={styles.overflowItem}
                  onClick={() => { setShowOverflow(false); setShowReportModal(true); }}
                >
                  <i className="ti ti-flag" /> Report
                </button>
              </div>
            )}
          </div>
        )}
      </div>

      {showReply && (
        <div className={styles.replyComposer}>
          <textarea
            className={styles.composerInput}
            placeholder="Write a reply..."
            value={replyText}
            onChange={e => setReplyText(e.target.value)}
          />
          <button className={styles.submitBtn} onClick={handleReply} disabled={submitting}>
            {submitting ? '...' : 'Reply'}
          </button>
        </div>
      )}

      {replies.length > 0 && (
        <div className={styles.replies}>
          {replies.map(r => (
            <div key={r.commentId} className={styles.reply}>
              <span className={styles.commentAuthor}>@{r.authorUsername}</span>
              <p className={styles.commentBody}>{r.contentText}</p>
            </div>
          ))}
        </div>
      )}

      <ReportModal
        isOpen={showReportModal}
        onClose={() => setShowReportModal(false)}
        commentId={comment.commentId}
      />
    </div>
  );
}