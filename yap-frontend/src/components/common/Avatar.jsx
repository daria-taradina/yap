import styles from './Avatar.module.css';

/**
 * Generate a deterministic HSL color from a string (username/name).
 * Uses a simple hash to pick a hue, with fixed saturation/lightness for readability.
 */
function getDeterministicColor(str) {
  if (!str) return undefined;
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }
  const hue = ((hash % 360) + 360) % 360;
  return `hsl(${hue}, 55%, 45%)`;
}

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

  const bgColor = !src ? getDeterministicColor(name) : undefined;

  return (
    <div
      className={`${styles.avatar} ${styles[size]}`}
      onClick={onClick}
      role={onClick ? 'button' : undefined}
      title={name}
      style={bgColor ? { background: bgColor, color: '#fff' } : undefined}
    >
      {src ? <img src={src} alt={name} /> : initials}
    </div>
  );
}
