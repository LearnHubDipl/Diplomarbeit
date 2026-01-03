import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

let reloginInProgress = false;

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const keycloak = inject(KeycloakService);

  console.log('[AuthInterceptor] Intercepting request to:', req.url);
  console.log('[AuthInterceptor] Method:', req.method);

  const keycloakInstance = keycloak.getKeycloakInstance();
  const token = keycloakInstance.token;

  console.log('[AuthInterceptor] Token available:', !!token);

  if (token && typeof token === 'string' && token.length > 0) {
    const isFormData = req.body instanceof FormData;

    console.log('[AuthInterceptor] Adding token to', req.method, 'request');
    console.log('[AuthInterceptor] Body is FormData:', isFormData);

    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
        ...(isFormData
          ? {}
          : (req.method === 'POST' || req.method === 'PUT' || req.method === 'PATCH'
            ? { 'Content-Type': 'application/json' }
            : {}))
      }
    });

    console.log('[AuthInterceptor] Headers:', cloned.headers.keys());

    return next(cloned).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          console.log('[AuthInterceptor] 401 Unauthorized - Session expired');

          if (!reloginInProgress) {
            reloginInProgress = true;
            alert('Deine Session ist abgelaufen. Bitte melde dich erneut an.');
            keycloak.login();
          }

          return throwError(() => error);
        }
        return throwError(() => error);
      })
    );
  }

  console.log('[AuthInterceptor] No valid token, redirecting to login');
  if (!reloginInProgress) {
    reloginInProgress = true;
    keycloak.login();
  }
  return throwError(() => new Error('No authentication token'));
};
