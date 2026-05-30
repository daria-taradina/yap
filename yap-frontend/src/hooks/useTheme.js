import { useState, useEffect } from 'react';

/**
 * useTheme
 *
 * Manages dark / light mode by toggling class="light" on <html>.
 * Persists preference to localStorage under "yap_theme".
 *
 * Returns: { theme, toggleTheme }
 *   theme       — "dark" | "light"
 *   toggleTheme — fn to switch between them
 */
export function useTheme() {
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem('yap_theme') || 'dark';
  });

  useEffect(() => {
    const root = document.documentElement;
    if (theme === 'light') {
      root.classList.add('light');
    } else {
      root.classList.remove('light');
    }
    localStorage.setItem('yap_theme', theme);
  }, [theme]);

  function toggleTheme() {
    setTheme((prev) => (prev === 'dark' ? 'light' : 'dark'));
  }

  return { theme, toggleTheme };
}
