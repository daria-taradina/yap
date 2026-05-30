import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import AppLayout             from './components/layout/AppLayout';
import FeedPage              from './pages/FeedPage';
import LoginPage             from './pages/LoginPage';
import RegisterPage          from './pages/RegisterPage';
import ProfilePage           from './pages/ProfilePage';
import SpacePage             from './pages/SpacePage';
import ExplorePage           from './pages/ExplorePage';
import PostDetailPage        from './pages/PostDetailPage';
import CreateBlogPostPage    from './pages/CreateBlogPostPage';
import CreateDiscussionPage  from './pages/CreateDiscussionPage';
import CreateSpacePage       from './pages/CreateSpacePage';

/**
 * Route map:
 *
 * Public (no shell):
 *   /login                        → LoginPage
 *   /register                     → RegisterPage
 *
 * Authenticated (inside AppLayout shell):
 *   /                             → FeedPage
 *   /explore                      → ExplorePage
 *   /w/:spaceName                 → SpacePage
 *   /blog/:username               → ProfilePage (personal blog)
 *   /post/:postId?type=blog|discussion → PostDetailPage
 *   /create-post/blog             → CreateBlogPostPage
 *   /create-post/discussion       → CreateDiscussionPage
 *   /create-space                 → CreateSpacePage
 */
export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Standalone — no nav shell */}
        <Route path="/login"    element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Authenticated shell */}
        <Route element={<AppLayout />}>
          <Route path="/"                          element={<FeedPage />} />
          <Route path="/explore"                   element={<ExplorePage />} />
          <Route path="/w/:spaceName"              element={<SpacePage />} />
          <Route path="/blog/:username"            element={<ProfilePage />} />
          <Route path="/post/:postId"              element={<PostDetailPage />} />
          <Route path="/create-post/blog"          element={<CreateBlogPostPage />} />
          <Route path="/create-post/discussion"    element={<CreateDiscussionPage />} />
          <Route path="/create-space"              element={<CreateSpacePage />} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
