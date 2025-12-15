import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const keycloak = inject(KeycloakService);

  console.log('[AuthInterceptor] Intercepting request to:', req.url);
  console.log('[AuthInterceptor] Method:', req.method);

  // Skip if no auth needed
  if (req.url.includes('/assets') || req.url.includes('/public')) {
    console.log('[AuthInterceptor] Skipping - public resource');
    return next(req);
  }

  const keycloakInstance = keycloak.getKeycloakInstance();
  const token = keycloakInstance.token;

  console.log('[AuthInterceptor] Token available:', !!token);

  if (token && typeof token === 'string' && token.length > 0) {
    console.log('[AuthInterceptor] Adding token to', req.method, 'request');

    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
        ...(req.method === 'POST' || req.method === 'PUT' ? {
          'Content-Type': 'application/json'
        } : {})
      }
    });

    console.log('[AuthInterceptor] Headers:', Object.keys(cloned.headers.keys()));
    return next(cloned);
  }

  console.log('[AuthInterceptor] No valid token, proceeding without auth');
  return next(req);
};
