import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { api } from '../api';
import ForumCard from '../components/feed/ForumCard';
import styles from './ExplorePage.module.css';

const SPACE_COLORS = ['#C4973F', '#6B8BAD', '#7A9E7E', '#A07AB5', '#9E7A7A'];
const TABS = ['Posts', 'Spaces', 'Users'];

export default function ExplorePage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const initialQuery = searchParams.get('q') || '';

  const [search, setSearch] = useState(initialQuery);
  const [activeTab, setActiveTab] = useState('Posts');
  const [loading, setLoading] = useState(true);

  // Default state data
  const [trendingTags, setTrendingTags] = useState([]);
  const [hotPosts, setHotPosts] = useState([]);
  const [popularSpaces, setPopularSpaces] = useState([]);

  // Search results
  const [searchResults, setSearchResults] = useState({ posts: [], spaces: [], users: [] });
  const [searchLoading, setSearchLoading] = useState(false);

  const isSearchActive = search.trim().length > 0;

  // Load default state data
  useEffect(() => {
    setLoading(true);
    Promise.all([
      api.getTrendingTags().then(r => r.json()).catch(() => []),
      api.getHotPosts().then(r => r.json()).catch(() => []),
      api.getSpaces().then(r => r.json()).catch(() => []),
    ]).then(([tags, hot, spaces]) => {
      setTrendingTags(Array.isArray(tags) ? tags : []);
      setHotPosts(Array.isArray(hot) ? hot.slice(0, 5) : []);
      // Sort spaces by member count descending
      const sorted = Array.isArray(spaces)
        ? [...spaces].sort((a, b) => (b.memberCount || 0) - (a.memberCount || 0))
        : [];
      setPopularSpaces(sorted);
    }).finally(() => setLoading(false));
  }, []);

  // Perform search when query changes
  const performSearch = useCallback(async (query) => {
    if (!query.trim()) {
      setSearchResults({ posts: [], spaces: [], users: [] });
      return;
    }
    setSearchLoading(true);
    try {
      const [postsRes, spacesRes] = await Promise.all([
        api.searchPosts(query).then(r => r.json()).catch(() => []),
        api.searchSpaces(query).then(r => r.json()).catch(() => []),
      ]);

      // User search may not exist on backend — graceful fallback
      let usersRes = [];
      try {
        const uRes = await api.searchUsers(query);
        if (uRes.ok) usersRes = await uRes.json();
      } catch {
        usersRes = [];
      }

      setSearchResults({
        posts: Array.isArray(postsRes) ? postsRes : [],
        spaces: Array.isArray(spacesRes) ? spacesRes : [],
        users: Array.isArray(usersRes) ? usersRes : [],
      });
    } catch {
      setSearchResults({ posts: [], spaces: [], users: [] });
    } finally {
      setSearchLoading(false);
    }
  }, []);

  // Sync search with URL param and trigger search
  useEffect(() => {
    const q = searchParams.get('q') || '';
    if (q !== search) setSearch(q);
    if (q.trim()) performSearch(q);
  }, [searchParams]); // eslint-disable-line react-hooks/exhaustive-deps

  function handleSearchChange(e) {
    const value = e.target.value;
    setSearch(value);

    // Debounce URL update
    const params = new URLSearchParams(searchParams);
    if (value.trim()) {
      params.set('q', value);
    } else {
      params.delete('q');
    }
    setSearchParams(params, { replace: true });
  }

  function handleSearchSubmit(e) {
    e.preventDefault();
    if (search.trim()) {
      performSearch(search);
    }
  }

  function handleTagClick(tagName) {
    const query = `#${tagName}`;
    setSearch(query);
    setSearchParams({ q: query }, { replace: true });
    performSearch(query);
  }

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={styles.emptyState}>
          <div className={styles.emptyIcon}>✦</div>
          <p>Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      {/* Search Bar */}
      <form className={styles.searchWrap} onSubmit={handleSearchSubmit}>
        <i className={`ti ti-search ${styles.searchIcon}`} aria-hidden="true" />
        <input
          className={styles.searchInput}
          placeholder="Search posts, spaces, users..."
          value={search}
          onChange={handleSearchChange}
        />
        {search && (
          <button
            type="button"
            className={styles.clearBtn}
            onClick={() => {
              setSearch('');
              setSearchParams({}, { replace: true });
              setSearchResults({ posts: [], spaces: [], users: [] });
            }}
            aria-label="Clear search"
          >
            <i className="ti ti-x" aria-hidden="true" />
          </button>
        )}
      </form>

      {isSearchActive ? (
        /* ── Search Results Mode ── */
        <div className={styles.searchResults}>
          {/* Tabs */}
          <div className={styles.tabs}>
            {TABS.map((tab) => (
              <button
                key={tab}
                className={`${styles.tab} ${activeTab === tab ? styles.tabActive : ''}`}
                onClick={() => setActiveTab(tab)}
              >
                {tab}
              </button>
            ))}
          </div>

          {searchLoading ? (
            <div className={styles.emptyState}>
              <div className={styles.emptyIcon}>✦</div>
              <p>Searching...</p>
            </div>
          ) : (
            <div className={styles.resultsList}>
              {activeTab === 'Posts' && (
                searchResults.posts.length === 0 ? (
                  <div className={styles.emptyState}>
                    <div className={styles.emptyIcon}>✦</div>
                    <p>No posts found for &ldquo;{search}&rdquo;</p>
                  </div>
                ) : (
                  searchResults.posts.map((post) => (
                    <ForumCard key={post.postId} post={post} />
                  ))
                )
              )}

              {activeTab === 'Spaces' && (
                searchResults.spaces.length === 0 ? (
                  <div className={styles.emptyState}>
                    <div className={styles.emptyIcon}>✦</div>
                    <p>No spaces found for &ldquo;{search}&rdquo;</p>
                  </div>
                ) : (
                  <div className={styles.grid}>
                    {searchResults.spaces.map((space, i) => (
                      <div
                        key={space.communityId}
                        className={styles.spaceCard}
                        onClick={() => navigate(`/w/${space.name}`)}
                      >
                        <div className={styles.spaceCardTop}>
                          <span
                            className={styles.spaceDot}
                            style={{ background: SPACE_COLORS[i % SPACE_COLORS.length] }}
                          />
                          {space.isMember && (
                            <span className={styles.joinedBadge}>Joined</span>
                          )}
                        </div>
                        <div className={styles.spaceName}>w/{space.name}</div>
                        {space.description && (
                          <p className={styles.spaceDesc}>{space.description}</p>
                        )}
                        <div className={styles.spaceMeta}>
                          {space.memberCount?.toLocaleString()} members
                        </div>
                      </div>
                    ))}
                  </div>
                )
              )}

              {activeTab === 'Users' && (
                searchResults.users.length === 0 ? (
                  <div className={styles.emptyState}>
                    <div className={styles.emptyIcon}>✦</div>
                    <p>No users found for &ldquo;{search}&rdquo;</p>
                  </div>
                ) : (
                  <div className={styles.usersList}>
                    {searchResults.users.map((user) => (
                      <Link
                        key={user.userId}
                        to={`/@${user.username}`}
                        className={styles.userCard}
                      >
                        <div className={styles.userAvatar}>
                          {user.avatarUrl ? (
                            <img src={user.avatarUrl} alt="" className={styles.userAvatarImg} />
                          ) : (
                            <span className={styles.userAvatarFallback}>
                              {user.username?.[0]?.toUpperCase()}
                            </span>
                          )}
                        </div>
                        <div className={styles.userInfo}>
                          <span className={styles.userName}>@{user.username}</span>
                          {user.bio && <span className={styles.userBio}>{user.bio}</span>}
                        </div>
                      </Link>
                    ))}
                  </div>
                )
              )}
            </div>
          )}
        </div>
      ) : (
        /* ── Default State (no query) ── */
        <>
          {/* Trending Topics */}
          {trendingTags.length > 0 && (
            <section>
              <div className={styles.sectionLabel}>Trending Topics</div>
              <div className={styles.tagsScroll}>
                {trendingTags.map((tag) => (
                  <button
                    key={tag.tagName || tag}
                    className={styles.tagPill}
                    onClick={() => handleTagClick(tag.tagName || tag)}
                  >
                    #{tag.tagName || tag}
                    {tag.postCount != null && (
                      <span className={styles.tagCount}>{tag.postCount}</span>
                    )}
                  </button>
                ))}
              </div>
            </section>
          )}

          {/* Hot Discussions */}
          {hotPosts.length > 0 && (
            <section>
              <div className={styles.sectionLabel}>Hot Discussions</div>
              <div className={styles.hotList}>
                {hotPosts.map((post) => (
                  <div
                    key={post.postId}
                    className={styles.hotCard}
                    onClick={() => navigate(`/post/${post.postId}`)}
                    role="button"
                    tabIndex={0}
                    onKeyDown={(e) => e.key === 'Enter' && navigate(`/post/${post.postId}`)}
                  >
                    <h4 className={styles.hotTitle}>{post.title}</h4>
                    <div className={styles.hotMeta}>
                      {post.communityName && (
                        <span className={styles.hotSpace}>w/{post.communityName}</span>
                      )}
                      <span className={styles.hotStats}>
                        <i className="ti ti-heart" aria-hidden="true" /> {post.likeCount}
                        <i className="ti ti-message-circle" aria-hidden="true" /> {post.commentCount}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </section>
          )}

          {/* Popular Spaces */}
          {popularSpaces.length > 0 && (
            <section>
              <div className={styles.sectionLabel}>Popular Spaces</div>
              <div className={styles.grid}>
                {popularSpaces.map((space, i) => (
                  <div
                    key={space.communityId}
                    className={styles.spaceCard}
                    onClick={() => navigate(`/w/${space.name}`)}
                  >
                    <div className={styles.spaceCardTop}>
                      <span
                        className={styles.spaceDot}
                        style={{ background: SPACE_COLORS[i % SPACE_COLORS.length] }}
                      />
                      {space.isMember && (
                        <span className={styles.joinedBadge}>Joined</span>
                      )}
                    </div>
                    <div className={styles.spaceName}>w/{space.name}</div>
                    {space.description && (
                      <p className={styles.spaceDesc}>{space.description}</p>
                    )}
                    <div className={styles.spaceMeta}>
                      {space.memberCount?.toLocaleString()} members
                    </div>
                  </div>
                ))}
              </div>
            </section>
          )}

          {trendingTags.length === 0 && hotPosts.length === 0 && popularSpaces.length === 0 && (
            <div className={styles.emptyState}>
              <div className={styles.emptyIcon}>✦</div>
              <p>Nothing to explore yet. Be the first to start a discussion!</p>
            </div>
          )}
        </>
      )}
    </div>
  );
}
