const CACHE_NAME = 'delivo-cache-v3';
const DYNAMIC_CACHE_NAME = 'delivo-dynamic-cache-v3';
const ASSETS = [
    'index.html',
    'styles.css',
    'app.js',
    'manifest.json',
    'offline.html'
];

self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME).then(cache => {
            return cache.addAll(ASSETS).catch(err => {
                console.warn('Pre-caching assets failed:', err);
            });
        })
    );
});

self.addEventListener('activate', event => {
    event.waitUntil(
        caches.keys().then(keys => {
            return Promise.all(
                keys.map(key => {
                    if (key !== CACHE_NAME && key !== DYNAMIC_CACHE_NAME) {
                        return caches.delete(key);
                    }
                })
            );
        })
    );
});

self.addEventListener('fetch', event => {
    const url = event.request.url;
    // Cache CDN resources dynamically (Google Fonts, FontAwesome, Leaflet, etc.)
    if (url.includes('fonts.googleapis.com') || 
        url.includes('fonts.gstatic.com') || 
        url.includes('cdnjs.cloudflare.com') || 
        url.includes('unpkg.com') || 
        url.includes('cdn.jsdelivr.net')) {
        event.respondWith(
            caches.match(event.request).then(cachedResponse => {
                if (cachedResponse) {
                    return cachedResponse;
                }
                return fetch(event.request).then(networkResponse => {
                    if (!networkResponse || networkResponse.status !== 200) {
                        return networkResponse;
                    }
                    return caches.open(DYNAMIC_CACHE_NAME).then(cache => {
                        cache.put(event.request, networkResponse.clone());
                        return networkResponse;
                    });
                }).catch(() => {
                    return caches.match(event.request);
                });
            })
        );
    } else {
        event.respondWith(
            caches.match(event.request).then(cachedResponse => {
                return cachedResponse || fetch(event.request).catch(() => {
                    if (event.request.mode === 'navigate') {
                        return caches.match('offline.html');
                    }
                });
            })
        );
    }
});
