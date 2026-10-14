// DSS Workstation Offline Service Worker
const CACHE_NAME = 'dss-workstation-v1';

const STATIC_ASSETS = [
  '/',
  '/index.html',
  '/style.css',
  '/app.js',
  '/manifest.json',
  '/favicon.png',
  '/download',
  '/download.html',
  '/assets/icon-192.png',
  '/assets/icon-512.png',
  '/assets/qr-code.png',
  '/assets/step1.svg',
  '/assets/step2.svg',
  '/assets/step3.svg',
  '/assets/step4.svg',
  '/assets/step5.svg'
];

// Install: Cache core application assets
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      console.log('[DSS Service Worker] Pre-caching offline workstation shell');
      return cache.addAll(STATIC_ASSETS).catch((err) => {
        console.warn('[DSS Service Worker] Pre-cache non-fatal error:', err);
      });
    }).then(() => self.skipWaiting())
  );
});

// Activate: Clean up older cache versions
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames
          .filter((name) => name !== CACHE_NAME)
          .map((name) => {
            console.log('[DSS Service Worker] Deleting obsolete cache:', name);
            return caches.delete(name);
          })
      );
    }).then(() => self.clients.claim())
  );
});

// Fetch: Network-first for dynamic, Stale-while-revalidate / cache-first for static assets
self.addEventListener('fetch', (event) => {
  // Do not intercept non-GET or cross-origin API calls like redirects
  if (event.request.method !== 'GET') return;
  const url = new URL(event.request.url);

  // If request is for github APK releases or external analytics, bypass cache
  if (url.origin !== self.location.origin) {
    return;
  }

  event.respondWith(
    caches.match(event.request).then((cachedResponse) => {
      // Return cached version if found, while also refreshing cache in background
      const fetchPromise = fetch(event.request).then((networkResponse) => {
        if (networkResponse && networkResponse.status === 200) {
          const responseToCache = networkResponse.clone();
          caches.open(CACHE_NAME).then((cache) => {
            cache.put(event.request, responseToCache);
          });
        }
        return networkResponse;
      }).catch(() => {
        // Network failed (offline); if cached response exists, it will be returned
      });

      return cachedResponse || fetchPromise;
    })
  );
});
