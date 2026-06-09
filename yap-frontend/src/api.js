const BASE = '/api';

function getToken() {
  return localStorage.getItem('yap_token');
}

function authHeaders() {
  return {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${getToken()}`,
  };
}

export async function apiFetch(path, options = {}) {
  const res = await fetch(`${BASE}${path}`, {
    ...options,
    headers: { ...authHeaders(), ...options.headers },
  });

  if (res.status === 401) {
    localStorage.removeItem('yap_token');
    localStorage.removeItem('yap_user');
    window.location.href = '/login';
    throw new Error('Unauthorized');  // throw instead of return undefined
  }

  return res;
}

export const api = {
  // Auth
  login: (body) =>
    fetch(`${BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    }),

  register: (body) =>
    fetch(`${BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    }),

  // Spaces
  getSpaces: () => apiFetch('/spaces'),
  getSpace: (name) => apiFetch(`/spaces/${name}`),
  createSpace: (body) => apiFetch('/spaces', { method: 'POST', body: JSON.stringify(body) }),
  joinSpace: (id) => apiFetch(`/spaces/${id}/join`, { method: 'POST' }),
  leaveSpace: (id) => apiFetch(`/spaces/${id}/leave`, { method: 'DELETE' }),
  getMySpaces: () => apiFetch('/spaces/my'),

  // Posts
  getFeed: (userId) => apiFetch(`/posts/feed/${userId}`),
  getForumsFeed: (userId) => apiFetch(`/posts/forums/${userId}`),
  getBlogsFeed: () => apiFetch('/posts/blogs'),
  getSpacePosts: (communityId) => apiFetch(`/posts/community/${communityId}`),
  createPost: (body) => apiFetch('/posts', { method: 'POST', body: JSON.stringify(body) }),
  likePost: (id) => apiFetch(`/posts/${id}/like`, { method: 'POST' }),
  unlikePost: (id) => apiFetch(`/posts/${id}/like`, { method: 'DELETE' }),

  // Comments
  getComments: (postId) => apiFetch(`/posts/${postId}/comments`),
  createComment: (body) => apiFetch('/posts/comments', { method: 'POST', body: JSON.stringify(body) }),

  // add these to the api object:
  getPost: (id) => apiFetch(`/posts/${id}`),
  likeComment: (id) => apiFetch(`/posts/comments/${id}/like`, { method: 'POST' }),
  unlikeComment: (id) => apiFetch(`/posts/comments/${id}/like`, { method: 'DELETE' }),
  getTrendingTags: () => apiFetch('/posts/trending'),
  getUserProfile: (username) => apiFetch(`/users/${username}`),
  getUserBlogPosts: (userId) => apiFetch(`/posts/profile/${userId}`),
  getUserPosts: (userId) => apiFetch(`/posts/user/${userId}/community`),
  getUserLiked: (userId) => apiFetch(`/posts/liked-by/${userId}`),

  // Notifications
  getNotifications: () => apiFetch('/notifications'),
  getUnreadCount: () => apiFetch('/notifications/unread-count'),
  markNotificationsRead: () => apiFetch('/notifications/mark-all-read', { method: 'POST' }),

  // Search
  searchPosts: (q) => apiFetch(`/posts/search?q=${encodeURIComponent(q)}`),
  searchSpaces: (q) => apiFetch(`/spaces?search=${encodeURIComponent(q)}`),
  
  // Profile edit
  updateBio: (bio) => apiFetch('/users/me/bio', { method: 'PATCH', body: JSON.stringify({ bio }) }),
  updateAvatar: (url) => apiFetch('/users/me/avatar', { method: 'PATCH', body: JSON.stringify({ url }) }),
};