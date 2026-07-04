/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        bg: '#0a0a0a',
        surface: '#1a1a1a',
        'surface-2': '#262626',
        border: '#2a2a2a',
        'text-primary': '#f5f5f5',
        'text-secondary': '#a3a3a3',
        accent: '#8B1E3F',
        'accent-gold': '#c9a961',
        success: '#4ade80',
        warning: '#fbbf24',
        error: '#f87171',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      borderRadius: {
        card: '12px',
        btn: '8px',
      },
    },
  },
  plugins: [],
}
