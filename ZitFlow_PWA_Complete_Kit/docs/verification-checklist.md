# Verification checklist

- `manifest.webmanifest` parses as valid JSON.
- Every manifest icon path exists in `public/`.
- Standard icons are square PNG files.
- Maskable icon artwork remains inside the central safe region.
- `favicon.ico` contains multiple resolutions.
- Apple touch icons have an opaque cream background.
- Startup images are provided in portrait and landscape.
- Service-worker registration is disabled in Angular development mode.
- Production deployment must use HTTPS.
- Run Chrome DevTools > Application > Manifest to inspect installation eligibility.
- Run Lighthouse PWA checks against the deployed production URL.
