import { Outlet, useLocation, useParams } from 'react-router-dom';
import Navbar from './Navbar';
import RightSidebar from './RightSidebar';
import Avatar from '../common/Avatar';
import styles from './AppLayout.module.css';

/**
 * Derives a short topbar title from the current route.
 * useParams() doesn't work here since AppLayout sits above
 * the matched route, so we parse pathname directly.
 */
function useTopbarTitle(pathname) {
  if (pathname === '/') return 'Home';
  if (pathname.startsWith('/w/')) return 'Forum';
  if (pathname.startsWith('/blog/')) return 'Blog';
  if (pathname.startsWith('/explore')) return 'Explore';
  if (pathname.startsWith('/create-space')) return 'Create Space';
  if (pathname.startsWith('/create-post/blog')) return 'New Blog Post';
  if (pathname.startsWith('/create-post/discussion')) return 'New Discussion';
  if (pathname.startsWith('/post/')) return '';   // post has its own title
  return '';
}

export default function AppLayout() {
  const { pathname } = useLocation();
  const title = useTopbarTitle(pathname);

  // TODO: replace with real auth context / user store
  const currentUser = { displayName: 'Daria', avatarUrl: null };

  return (
    <div className={styles.shell}>
      <Navbar />

      <div className={styles.main}>
        <header className={styles.topbar}>
          {title && <span className={styles.topbarTitle}>{title}</span>}

          <div className={styles.topbarRight}>
            <button className={styles.iconBtn} aria-label="Search">
              <i className="ti ti-search" aria-hidden="true" />
            </button>
            <button className={styles.iconBtn} aria-label="Notifications">
              <i className="ti ti-bell" aria-hidden="true" />
              <span className={styles.notifDot} />
            </button>
            <Avatar name={currentUser.displayName} src={currentUser.avatarUrl} size="sm" />
          </div>
        </header>

        <main className={styles.content}>
          <Outlet />
        </main>
      </div>

      <RightSidebar />
    </div>
  );
}
