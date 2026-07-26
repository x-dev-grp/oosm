# ZitFlow PWA Complete Kit

This package uses the selected ZitFlow ZF/oil-drop logo and is configured for an Angular PWA.

## Included

- Standard install icons from 48×48 through 1024×1024
- Transparent icon variants
- Maskable Android icons
- Monochrome launcher icons
- Apple touch icons
- Multi-resolution favicon
- Safari pinned-tab SVG
- Microsoft tile icon and `browserconfig.xml`
- Apple startup screens in portrait and landscape
- `manifest.webmanifest`
- Angular `ngsw-config.json`
- Angular service-worker provider snippet
- `<head>` integration snippet
- Offline fallback page
- 2048×2048 and 4096×4096 branding exports
- SHA-256 integrity checksums

## Brand colors

- Dark olive: `#3D4F2F`
- Light olive: `#8A9A6A`
- Oil gold: `#D4A84B`
- Canvas cream: `#F7F6F2`

## Angular integration

1. Copy the contents of `public/` into the Angular project's `public/` directory.
2. Copy `ngsw-config.json` to the Angular project root.
3. Install Angular PWA support:

   ```bash
   npm install @angular/service-worker
   ```

4. Add the provider from `integration/app-config-service-worker.snippet.ts` to `src/app/app.config.ts`.
5. Add the service-worker build option from `integration/angular-json.snippet.json` to the project's production build configuration when it is not already present.
6. Merge `integration/index-head.snippet.html` into `src/index.html`.
7. Build for the existing `/osm/` deployment path:

   ```bash
   ng build --configuration production --base-href /osm/ --deploy-url /osm/
   ```

8. Serve the generated application over HTTPS. Service workers do not install on ordinary HTTP origins except localhost.

## Path behavior

The manifest uses relative `id`, `start_url`, `scope`, and asset URLs. It therefore works under `/osm/` when `manifest.webmanifest` is served from `/osm/manifest.webmanifest`.

## Cache policy

`ngsw-config.json` caches application files and branding assets. `/api/**` uses a freshness strategy with a one-hour cache limit. Review this rule before caching sensitive or user-specific API responses.

## Quality

PNG files use lossless encoding. The 4096×4096 files are high-resolution raster exports. They remain raster images and do not provide infinite vector scaling.
