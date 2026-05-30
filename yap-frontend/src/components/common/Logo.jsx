import styles from './Logo.module.css';

/**
 * Logo
 *
 * Props:
 *   theme  {string}  — "dark" | "light"  switches SVG asset
 *   size   {string}  — CSS height (default: "32px")
 */
export default function Logo({ theme = 'dark', size = '32px' }) {
  const src = theme === 'light'
    ? '/assets/light-mode-logo-yap.svg'
    : '/assets/dark-mode-logo-yap.svg';

  return (
    <div className={styles.logoWrapper}>
      <img
        src={src}
        alt="Yap"
        className={styles.logoImg}
        style={{ height: size }}
      />
    </div>
  );
}
