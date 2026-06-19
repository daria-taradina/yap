import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import AppLayout            from './components/layout/AppLayout';
import FeedPage             from './pages/FeedPage';
import LoginPage            from './pages/LoginPage';
import RegisterPage         from './pages/RegisterPage';
import ProfilePage          from './pages/ProfilePage';
import SpacePage            from './pages/SpacePage';
import ExplorePage          from './pages/ExplorePage';
import ForumsPage           from './pages/ForumsPage';
import PostDetailPage       from './pages/PostDetailPage';
import CreateDiscussionPage from './pages/CreateDiscussionPage';
import CreateSpacePage      from './pages/CreateSpacePage';
import SearchPage           from './pages/SearchPage';

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
          <Route path="/w/:spaceName"           element={<SpacePage />} />
          <Route path="/post/:postId"           element={<PostDetailPage />} />
          <Route path="/create-post/discussion" element={<CreateDiscussionPage />} />
          <Route path="/create-space"           element={<CreateSpacePage />} />
          <Route path="/search"                 element={<SearchPage />} />
        </Route>

        {/* Redirects for removed blog routes */}
        <Route path="/blogs"              element={<Navigate to="/" replace />} />
        <Route path="/create-post/blog"   element={<Navigate to="/" replace />} />
        <Route path="/blog/:username"     element={<Navigate to="/" replace />} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
