import styles from './Avatar.module.css';

/**
 * Avatar
 *
 * Props:
 *   src      {string}  — image URL (optional; shows initials if absent)
 *   name     {string}  — used to derive initials and alt text
 *   size     {string}  — "sm" | "md" | "lg"  (default: "md")
 *   onClick  {fn}      — optional click handler
 */
export default function Avatar({ src, name = '?', size = 'md', onClick }) {
  const initials = name
    .split(' ')
    .map((n) => n[0])
    .join('')
    .slice(0, 2)
    .toUpperCase();

  return (
    <div
      className={`${styles.avatar} ${styles[size]}`}
      onClick={onClick}
      role={onClick ? 'button' : undefined}
      title={name}
    >
      {src ? <img src={src} alt={name} /> : initials}
    </div>
  );
}