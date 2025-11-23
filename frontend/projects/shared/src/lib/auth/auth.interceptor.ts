import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const keycloak = inject(KeycloakService);

  console.log('[AuthInterceptor] Intercepting request to:', req.url);

  // Skip if no auth needed or external URL
  if (req.url.includes('/assets') || req.url.includes('/public')) {
    console.log('[AuthInterceptor] Skipping - public resource');
    return next(req);
  }

  const keycloakInstance = keycloak.getKeycloakInstance();
  const token = keycloakInstance.token;

  console.log('[AuthInterceptor] Token type:', typeof token);
  console.log('[AuthInterceptor] Token available:', !!token);
  console.log('[AuthInterceptor] Token length:', token?.length || 0);

  if (token && typeof token === 'string' && token.length > 0) {
    console.log('[AuthInterceptor] Adding token to request');
    console.log('[AuthInterceptor] Token preview:', token.substring(0, 50) + '...');

    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });

    console.log('[AuthInterceptor] Request headers:', cloned.headers.keys());
    console.log('[AuthInterceptor] Authorization header value:', cloned.headers.get('Authorization')?.substring(0, 60) + '...');
    return next(cloned);
  }

  console.log('[AuthInterceptor] No valid token, proceeding without auth');
  return next(req);
};
