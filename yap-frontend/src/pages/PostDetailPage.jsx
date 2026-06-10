import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../api';
import styles from './PostDetailPage.module.css';

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
  const [showReport, setShowReport] = useState(false);
  const [reportReason, setReportReason] = useState('HARASSMENT');
  const [reportDetails, setReportDetails] = useState('');

  useEffect(() => {
    Promise.all([
      api.getPost(postId).then(r => r.json()),
      api.getComments(postId).then(r => r.json()),
    ]).then(([postData, commentsData]) => {
      setPost(postData);
      setLiked(postData.likedByCurrentUser);
      setLikeCount(postData.likeCount);
      setComments(Array.isArray(commentsData) ? commentsData : []);
    }).catch(() => setPost(null))
      .finally(() => setLoading(false));
  }, [postId]);

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

  async function handleDelete() {
    if (!window.confirm('Delete this post?')) return;
    try {
      await api.deletePost(postId);
      navigate('/');
    } catch {}
  }

  async function handleReport() {
    try {
      await api.reportContent({ postId: parseInt(postId), reason: reportReason, details: reportDetails });
      setShowReport(false);
      setReportDetails('');
      alert('Report submitted. Thank you.');
    } catch {}
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

        {post.tags?.length > 0 && (
          <div className={styles.tags}>
            {post.tags.map(tag => <span key={tag} className={styles.tag}>#{tag}</span>)}
          </div>
        )}

        <div className={styles.actions}>
          <button className={`${styles.actionBtn} ${liked ? styles.liked : ''}`} onClick={handleLike}>
            <i className={liked ? 'ti ti-heart-filled' : 'ti ti-heart'} /> {likeCount}
          </button>
          <button className={styles.actionBtn}>
            <i className="ti ti-message-circle" /> {comments.length}
          </button>
          <button className={styles.actionBtn} onClick={() => setShowReport(v => !v)}>
            <i className="ti ti-flag" /> Report
          </button>
          {post.canDelete && (
            <button className={`${styles.actionBtn} ${styles.deleteBtn}`} onClick={handleDelete}>
              <i className="ti ti-trash" /> Delete
            </button>
          )}
        </div>

        {showReport && (
          <div className={styles.reportForm}>
            <select value={reportReason} onChange={e => setReportReason(e.target.value)} className={styles.reportSelect}>
              <option value="HARASSMENT">Harassment</option>
              <option value="HATE_SPEECH">Hate Speech</option>
              <option value="SPAM">Spam</option>
              <option value="MISINFORMATION">Misinformation</option>
              <option value="SELF_HARM">Self Harm</option>
              <option value="OTHER">Other</option>
            </select>
            <textarea
              className={styles.composerInput}
              placeholder="Optional details..."
              value={reportDetails}
              onChange={e => setReportDetails(e.target.value)}
              rows={2}
            />
            <button className={styles.submitBtn} onClick={handleReport}>Submit Report</button>
          </div>
        )}
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
    </div>
  );
}