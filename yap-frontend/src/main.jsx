import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

// ─── Global styles (order matters) ───────────────
import './styles/tokens.css';   // CSS custom properties / design tokens
import './styles/global.css';   // reset + base body styles
import './styles/mobile.css';   // mobile responsive overrides (≤768px)
// ─────────────────────────────────────────────────

import App from './App';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>
);
