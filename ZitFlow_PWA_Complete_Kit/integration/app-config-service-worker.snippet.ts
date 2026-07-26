import { isDevMode } from '@angular/core';
import { provideServiceWorker } from '@angular/service-worker';

// Add this provider to the providers array in src/app/app.config.ts:
provideServiceWorker('ngsw-worker.js', {
  enabled: !isDevMode(),
  registrationStrategy: 'registerWhenStable:30000',
});
