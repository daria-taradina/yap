import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import AppLayout            from './components/layout/AppLayout';
import FeedPage             from './pages/FeedPage';
import LoginPage            from './pages/LoginPage';
import RegisterPage         from './pages/RegisterPage';
import ProfilePage          from './pages/ProfilePage';
import SpacePage            from './pages/SpacePage';
import ExplorePage          from './pages/ExplorePage';
import ForumsPage           from './pages/ForumsPage';
import BlogsPage            from './pages/BlogsPage';
import PostDetailPage       from './pages/PostDetailPage';
import CreateBlogPostPage   from './pages/CreateBlogPostPage';
import CreateDiscussionPage from './pages/CreateDiscussionPage';
import CreateSpacePage      from './pages/CreateSpacePage';
import SearchPage           from './pages/SearchPage';
import ModerationDemoPage   from './pages/ModerationDemoPage';
import ModQueuePage         from './pages/ModQueuePage';

function RequireAuth({ children }) {
  const token = localStorage.getItem('yap_token');
  return token ? children : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login"    element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        <Route element={
          <RequireAuth><AppLayout /></RequireAuth>
        }>
          <Route path="/"                       element={<FeedPage />} />
          <Route path="/explore"                element={<ExplorePage />} />
          <Route path="/forums"                 element={<ForumsPage />} />
          <Route path="/blogs"                  element={<BlogsPage />} />
          <Route path="/w/:spaceName"           element={<SpacePage />} />
          <Route path="/blog/:username"         element={<ProfilePage />} />
          <Route path="/post/:postId"           element={<PostDetailPage />} />
          <Route path="/create-post/blog"       element={<CreateBlogPostPage />} />
          <Route path="/create-post/discussion" element={<CreateDiscussionPage />} />
          <Route path="/create-space"           element={<CreateSpacePage />} />
          <Route path="/search"                 element={<SearchPage />} />
          <Route path="/moderation"             element={<ModerationDemoPage />} />
          <Route path="/mod-queue"              element={<ModQueuePage />} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}