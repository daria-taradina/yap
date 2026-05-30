import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

// ─── Global styles (order matters) ───────────────
import './styles/tokens.css';   // CSS custom properties / design tokens
import './styles/global.css';   // reset + base body styles
// ─────────────────────────────────────────────────

import App from './App';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>
);
