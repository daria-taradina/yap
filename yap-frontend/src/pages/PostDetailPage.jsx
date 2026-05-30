import { useState, useEffect } from 'react';
import { useParams, useSearchParams } from 'react-router-dom';
import styles from './PostDetailPage.module.css';

/**
 * PostDetailPage  —  /post/:postId?type=blog|forum
 *
 * Handles both blog posts and forum posts.
 * Type is passed as a query param so we know which endpoint to hit.
 *
 * TODO: GET /api/posts/blog/{postId}   or   GET /api/posts/forum/{postId}
 * TODO: GET /api/posts/{postId}/comments
 * TODO: POST /api/posts/{postId}/comments
 * TODO: POST /api/posts/{postId}/like
 */
export default function PostDetailPage() {
  const { postId } = useParams();
  const [searchParams] = useSearchParams();
  const postType = searchParams.get('type') || 'forum'; // 'blog' | 'forum'

  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [liked, setLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(0);
  const [commentText, setCommentText] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    // TODO: fetch post + comments from API
    // const endpoint = postType === 'blog'
    //   ? `/api/posts/blog/${postId}`
    //   : `/api/posts/forum/${postId}`;
    // const [postRes, commentsRes] = await Promise.all([
    //   fetch(endpoint, { headers: auth }),
    //   fetch(`/api/posts/${postId}/comments`, { headers: auth }),
    // ]);
    // const postData = await postRes.json();
    // setPost(postData);
    // setLikeCount(postData.likeCount);
    // setComments(await commentsRes.json());
    setLoading(false);
    setPost(null);
  }, [postId, postType]);

  async function handleLike() {
    // TODO: POST /api/posts/{postId}/like
    setLiked((v) => !v);
    setLikeCount((n) => liked ? n - 1 : n + 1);
  }

  async function handleComment() {
    if (!commentText.trim()) return;
    setSubmitting(true);
    try {
      // TODO: POST /api/posts/{postId}/comments
      // const res = await fetch(`/api/posts/${postId}/comments`, {
      //   method: 'POST',
      //   headers: { 'Content-Type': 'application/json', ...auth },
      //   body: JSON.stringify({ content: commentText }),
      // });
      // const newComment = await res.json();
      // setComments((prev) => [newComment, ...prev]);
      setCommentText('');
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <div className={styles.state}>Loading...</div>;
  if (!post)   return <div className={styles.state}>Post not found.</div>;

  return (
    <div className={styles.page}>
      {/* Post */}
      <div className={styles.post}>
        {postType === 'blog' ? (
          <BlogHeader post={post} />
        ) : (
          <ForumHeader post={post} />
        )}

        <h1 className={styles.title}>{post.title}</h1>
        <p className={styles.body}>{post.contentText}</p>

        {post.tags?.length > 0 && (
          <div className={styles.tags}>
            {post.tags.map((tag) => (
              <span key={tag} className={styles.tag}>#{tag}</span>
            ))}
          </div>
        )}

        {/* Actions */}
        <div className={styles.actions}>
          <button
            className={`${styles.actionBtn} ${liked ? styles.liked : ''}`}
            onClick={handleLike}
          >
            <i className={liked ? 'ti ti-heart-filled' : 'ti ti-heart'} />
            {likeCount}
          </button>
          <button className={styles.actionBtn}>
            <i className="ti ti-message-circle" />
            {comments.length}
          </button>
        </div>
      </div>

      {/* Comments */}
      <div className={styles.commentsSection}>
        <div className={styles.commentsLabel}>Replies</div>

        {/* Composer */}
        <div className={styles.composer}>
          <textarea
            className={styles.composerInput}
            placeholder="Share your thoughts..."
            value={commentText}
            onChange={(e) => setCommentText(e.target.value)}
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

        {/* Comment list */}
        {comments.length === 0 ? (
          <div className={styles.emptyComments}>
            No replies yet — start the conversation.
          </div>
        ) : (
          comments.map((c) => (
            <Comment key={c.id} comment={c} />
          ))
        )}
      </div>
    </div>
  );
}

/* ── Sub-components ── */

function BlogHeader({ post }) {
  return (
    <div className={styles.blogAuthor}>
      <span className={styles.displayName}>{post.displayName || post.username}</span>
      <span className={styles.byline}>
        <span className={styles.username}>{post.username}</span>
        {post.readTime && <span>· {post.readTime} read</span>}
      </span>
    </div>
  );
}

function ForumHeader({ post }) {
  return (
    <div className={styles.forumMeta}>
      {post.space && <span className={styles.space}>w/{post.space}</span>}
      {post.username && <span className={styles.forumUsername}>{post.username}</span>}
      {post.createdAt && <span className={styles.timestamp}>· {post.createdAt}</span>}
    </div>
  );
}

function Comment({ comment }) {
  const [liked, setLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(comment.likeCount || 0);

  return (
    <div className={styles.comment}>
      <div className={styles.commentMeta}>
        <span className={styles.commentAuthor}>{comment.username}</span>
        {comment.createdAt && (
          <span className={styles.commentTime}>· {comment.createdAt}</span>
        )}
      </div>
      <p className={styles.commentBody}>{comment.contentText}</p>
      <div
        className={styles.commentLike}
        onClick={() => {
          setLiked((v) => !v);
          setLikeCount((n) => liked ? n - 1 : n + 1);
        }}
      >
        <i className={liked ? 'ti ti-heart-filled' : 'ti ti-heart'} />
        {likeCount > 0 && likeCount}
      </div>
    </div>
  );
}
