import { useState, useEffect, useCallback } from 'react';
import styles from './InstallPrompt.module.css';

const DISMISS_KEY = 'yap_install_dismissed';
const SESSION_SHOWN_KEY = 'yap_install_shown_session';

/**
 * Detects iOS Safari (not in standalone mode).
 */
function isIOSSafari() {
  const ua = navigator.userAgent;
  const isIOS = /iPad|iPhone|iPod/.test(ua) || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1);
  const isSafari = /Safari/.test(ua) && !/CriOS|FxiOS|Chrome/.test(ua);
  const isStandalone = window.navigator.standalone === true;
  return isIOS && isSafari && !isStandalone;
}

/**
 * Checks if the user is on a mobile viewport (≤768px).
 */
function isMobileViewport() {
  return window.matchMedia('(max-width: 768px)').matches;
}

/**
 * Checks if the app is already installed (running in standalone mode).
 */
function isAppInstalled() {
  return window.matchMedia('(display-mode: standalone)').matches || window.navigator.standalone === true;
}

export default function InstallPrompt() {
  const [deferredPrompt, setDeferredPrompt] = useState(null);
  const [showBanner, setShowBanner] = useState(false);
  const [isIOS, setIsIOS] = useState(false);

  const currentUser = JSON.parse(localStorage.getItem('yap_user') || 'null');
  const isLoggedIn = !!currentUser && !!localStorage.getItem('yap_token');

  useEffect(() => {
    // Don't show if: not logged in, not mobile, already installed, already dismissed this session
    if (!isLoggedIn || !isMobileViewport() || isAppInstalled()) return;

    const dismissed = localStorage.getItem(DISMISS_KEY);
    const shownThisSession = sessionStorage.getItem(SESSION_SHOWN_KEY);

    if (dismissed || shownThisSession) return;

    // iOS Safari path
    if (isIOSSafari()) {
      setIsIOS(true);
      setShowBanner(true);
      sessionStorage.setItem(SESSION_SHOWN_KEY, 'true');
      return;
    }

    // Android/Chrome path — listen for beforeinstallprompt
    function handleBeforeInstall(e) {
      e.preventDefault();
      setDeferredPrompt(e);
      setShowBanner(true);
      sessionStorage.setItem(SESSION_SHOWN_KEY, 'true');
    }

    window.addEventListener('beforeinstallprompt', handleBeforeInstall);

    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstall);
    };
  }, [isLoggedIn]);

  const handleInstall = useCallback(async () => {
    if (!deferredPrompt) return;
    deferredPrompt.prompt();
    const { outcome } = await deferredPrompt.userChoice;
    if (outcome === 'accepted') {
      setShowBanner(false);
    }
    setDeferredPrompt(null);
  }, [deferredPrompt]);

  const handleDismiss = useCallback(() => {
    setShowBanner(false);
    localStorage.setItem(DISMISS_KEY, 'true');
  }, []);

  if (!showBanner) return null;

  return (
    <div className={styles.banner} role="alert" aria-live="polite">
      <div className={styles.bannerContent}>
        <div className={styles.bannerIcon}>
          <i className="ti ti-download" aria-hidden="true" />
        </div>

        <div className={styles.bannerText}>
          {isIOS ? (
            <>
              <strong>Add Yap to Home Screen</strong>
              <span className={styles.bannerDesc}>
                Tap <i className="ti ti-share" aria-hidden="true" /> Share, then "Add to Home Screen"
              </span>
            </>
          ) : (
            <>
              <strong>Install Yap</strong>
              <span className={styles.bannerDesc}>
                Get quick access from your home screen
              </span>
            </>
          )}
        </div>

        <div className={styles.bannerActions}>
          {!isIOS && (
            <button
              className={styles.installBtn}
              onClick={handleInstall}
              aria-label="Install Yap app"
            >
              Install
            </button>
          )}
          <button
            className={styles.dismissBtn}
            onClick={handleDismiss}
            aria-label="Dismiss install prompt"
          >
            <i className="ti ti-x" aria-hidden="true" />
          </button>
        </div>
      </div>
    </div>
  );
}
