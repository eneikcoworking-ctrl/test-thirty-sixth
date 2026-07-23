/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{svelte,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        "on-secondary-fixed-variant": "#005236",
        "on-secondary-container": "#00714d",
        "background": "#f8f9ff",
        "tertiary": "#000000",
        "inverse-on-surface": "#eaf1ff",
        "on-primary": "#ffffff",
        "on-primary-fixed": "#131b2e",
        "tertiary-fixed-dim": "#ffb95f",
        "on-tertiary-fixed-variant": "#653e00",
        "on-primary-fixed-variant": "#3f465c",
        "tertiary-fixed": "#ffddb8",
        "surface-container-low": "#eff4ff",
        "secondary": "#006c49",
        "on-secondary": "#ffffff",
        "surface-bright": "#f8f9ff",
        "secondary-fixed-dim": "#4edea3",
        "outline-variant": "#c6c6cd",
        "primary-fixed": "#dae2fd",
        "on-tertiary-container": "#b87500",
        "on-secondary-fixed": "#002113",
        "secondary-container": "#6cf8bb",
        "error": "#ba1a1a",
        "on-primary-container": "#7c839b",
        "on-background": "#0b1c30",
        "primary-container": "#131b2e",
        "inverse-primary": "#bec6e0",
        "outline": "#76777d",
        "on-surface-variant": "#45464d",
        "surface-variant": "#d3e4fe",
        "on-surface": "#0b1c30",
        "primary": "#000000",
        "on-tertiary": "#ffffff",
        "on-error-container": "#93000a",
        "inverse-surface": "#213145",
        "primary-fixed-dim": "#bec6e0",
        "surface-container-lowest": "#ffffff",
        "on-error": "#ffffff",
        "surface-container-highest": "#d3e4fe",
        "surface": "#f8f9ff",
        "tertiary-container": "#2a1700",
        "surface-container": "#e5eeff",
        "error-container": "#ffdad6",
        "surface-container-high": "#dce9ff",
        "on-tertiary-fixed": "#2a1700",
        "surface-tint": "#565e74",
        "surface-dim": "#cbdbf5",
        "secondary-fixed": "#6ffbbe"
      },
      borderRadius: {
        "DEFAULT": "0.125rem",
        "lg": "0.25rem",
        "xl": "0.5rem",
        "full": "0.75rem"
      },
      spacing: {
        "inline-gap": "8px",
        "container-margin": "16px",
        "list-item-padding": "12px 16px",
        "chip-padding": "4px 8px",
        "stack-gap": "12px"
      },
      fontSize: {
        "headline-md": ["1.25rem", {"lineHeight": "1.75rem", "letterSpacing": "-0.01em", "fontWeight": "600"}],
        "timestamp": ["0.6875rem", {"lineHeight": "0.875rem", "fontWeight": "400"}],
        "body-sm": ["0.8125rem", {"lineHeight": "1.125rem", "fontWeight": "400"}],
        "headline-lg": ["1.5rem", {"lineHeight": "2rem", "letterSpacing": "-0.02em", "fontWeight": "700"}],
        "label-md": ["0.75rem", {"lineHeight": "1rem", "letterSpacing": "0.05em", "fontWeight": "600"}],
        "body-md": ["0.875rem", {"lineHeight": "1.25rem", "fontWeight": "400"}],
        "body-lg": ["1rem", {"lineHeight": "1.5rem", "fontWeight": "400"}],
        "label-sm": ["0.6875rem", {"lineHeight": "0.875rem", "fontWeight": "500"}]
      }
    }
  },
  plugins: [],
}
