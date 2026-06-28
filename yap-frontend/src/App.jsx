import { BrowserRouter, Routes, Route, Navigate, useParams } from 'react-router-dom';

import AppLayout            from './components/layout/AppLayout';
import FeedPage             from './pages/FeedPage';
import LoginPage            from './pages/LoginPage';
import RegisterPage         from './pages/RegisterPage';
import ProfilePage          from './pages/ProfilePage';
import SpacePage            from './pages/SpacePage';
import ExplorePage          from './pages/ExplorePage';
import PostDetailPage       from './pages/PostDetailPage';
import CreateDiscussionPage from './pages/CreateDiscussionPage';
import CreateSpacePage      from './pages/CreateSpacePage';
import ModQueuePage         from './pages/ModQueuePage';
import InboxPage            from './pages/InboxPage';

function RequireAuth({ children }) {
  const token = localStorage.getItem('yap_token');
  return token ? children : <Navigate to="/login" replace />;
}

/** Redirect /users/:username → /@:username */
function RedirectToProfile() {
  const { username } = useParams();
  return <Navigate to={`/@${username}`} replace />;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login"    element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Main layout — public read access (no RequireAuth wrapper) */}
        <Route element={<AppLayout />}>
          <Route path="/"                       element={<FeedPage />} />
          <Route path="/explore"                element={<ExplorePage />} />
          <Route path="/w/:spaceName"           element={<SpacePage />} />
          <Route path="/post/:postId"           element={<PostDetailPage />} />
          <Route path="/@:username"             element={<ProfilePage />} />
          <Route path="/mod-queue"              element={<ModQueuePage />} />

          {/* Write routes — protected at component level */}
          <Route path="/create-post/discussion" element={<RequireAuth><CreateDiscussionPage /></RequireAuth>} />
          <Route path="/create-space"           element={<RequireAuth><CreateSpacePage /></RequireAuth>} />
          <Route path="/inbox"                  element={<RequireAuth><InboxPage /></RequireAuth>} />
        </Route>

        {/* Redirects: removed pages → /explore */}
        <Route path="/forums"             element={<Navigate to="/explore" replace />} />
        <Route path="/search"             element={<Navigate to="/explore" replace />} />
        <Route path="/discover"           element={<Navigate to="/explore" replace />} />

        {/* Redirects: old profile routes → /@username */}
        <Route path="/users/:username"    element={<RedirectToProfile />} />
        <Route path="/blog/:username"     element={<RedirectToProfile />} />

        {/* Redirects for removed blog routes */}
        <Route path="/blogs"              element={<Navigate to="/" replace />} />
        <Route path="/create-post/blog"   element={<Navigate to="/" replace />} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
