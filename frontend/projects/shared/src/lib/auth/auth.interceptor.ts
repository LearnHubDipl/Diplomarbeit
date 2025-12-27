import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { catchError } from 'rxjs/operators';
import { throwError, from, switchMap } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const keycloak = inject(KeycloakService);

  console.log('[AuthInterceptor] Intercepting request to:', req.url);
  console.log('[AuthInterceptor] Method:', req.method);

  /**
  if (req.url.includes('/assets')) {
    console.log('[AuthInterceptor] Skipping - static asset');
    return next(req);
  }
**/
  const keycloakInstance = keycloak.getKeycloakInstance();
  const token = keycloakInstance.token;

  console.log('[AuthInterceptor] Token available:', !!token);

  if (token && typeof token === 'string' && token.length > 0) {
    console.log('[AuthInterceptor] Adding token to', req.method, 'request');

    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
        ...(req.method === 'POST' || req.method === 'PUT' || req.method === 'PATCH' ? {
          'Content-Type': 'application/json'
        } : {})
      }
    });

    console.log('[AuthInterceptor] Headers:', Object.keys(cloned.headers.keys()));

    return next(cloned).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          console.log('[AuthInterceptor] 401 Unauthorized - Session expired');
          console.log('[AuthInterceptor] Forcing re-login...');

          alert('Deine Session ist abgelaufen. Bitte melde dich erneut an.');
          keycloak.login();

          return throwError(() => error);
        }
        return throwError(() => error);
      })
    );
  }

  console.log('[AuthInterceptor] No valid token, redirecting to login');
  keycloak.login();
  return throwError(() => new Error('No authentication token'));
};
