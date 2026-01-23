import { APP_INITIALIZER, Provider, Injector } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { UserInitializationService } from '../services/user-initialization.service';
import { MatSnackBar } from '@angular/material/snack-bar';

const TOKEN_REFRESH_INTERVAL = 30000;
const TOKEN_MIN_VALIDITY = 70;
const MAX_SESSION_DURATION = 2 * 60 * 60 * 1000;

export function initializeKeycloak(keycloak: KeycloakService, injector: Injector) {
  return async () => {
    try {
      console.log('[KEYCLOAK] Starting initialization...');

      const authenticated = await keycloak.init({
        config: {
          url: 'https://auth.htl-leonding.ac.at',
          realm: '2526_5bhitm',
          clientId: 'frontend',
        },
        initOptions: {
          onLoad: 'login-required',
          checkLoginIframe: false,
          pkceMethod: 'S256',
        },
        enableBearerInterceptor: false,
        bearerExcludedUrls: ['/assets'],
      });

      console.log('[KEYCLOAK] Authentication status:', authenticated);

      if (authenticated) {
        await keycloak.updateToken(30);

        const token = await keycloak.getToken();
        console.log('[KEYCLOAK] Token obtained, length:', token?.length || 0);

        setupTokenRefreshWithTimeout(keycloak, injector);

        console.log('[KEYCLOAK] Initializing user in backend...');
        const userInitService = injector.get(UserInitializationService);

        try {
          const user = await userInitService.initializeUser();

          if (user) {
            console.log('[KEYCLOAK]  User initialized in backend');
            console.log('[KEYCLOAK] User ID:', user.id);
            console.log('[KEYCLOAK] Keycloak Sub:', user.keycloakSub);
          } else {
            console.warn('[KEYCLOAK]  User initialization returned null');
          }
        } catch (error) {
          console.error('[KEYCLOAK] ✗ Error initializing user:', error);
        }
      }

      return authenticated;
    } catch (error) {
      console.error('[KEYCLOAK] ✗ Initialization Error:', error);
      return false;
    }
  };
}

function showSnackbar(injector: Injector, message: string, action: string = 'OK', panelClass: string[] = []) {
  const snackBar = injector.get(MatSnackBar);
  snackBar.open(message, action, {
    duration: 5000,
    horizontalPosition: 'right',
    verticalPosition: 'top',
    panelClass: panelClass
  });
}

/**
 * Setup automatic token refresh with maximum session duration
 * - Refreshes token every 30 seconds if needed
 * - Forces re-login after MAX_SESSION_DURATION
 */
function setupTokenRefreshWithTimeout(keycloak: KeycloakService, injector: Injector): void {
  const sessionStartTime = Date.now();
  let refreshIntervalId: any;

  console.log(`[KEYCLOAK] Session started. Max duration: ${MAX_SESSION_DURATION / (60 * 60 * 1000)} hours`);

  refreshIntervalId = setInterval(async () => {
    console.log('[KEYCLOAK] Checking token...'); // ← NEU

    const sessionDuration = Date.now() - sessionStartTime;

    if (sessionDuration >= MAX_SESSION_DURATION) {
      console.log('[KEYCLOAK] Maximum session duration reached. Logging out...');
      clearInterval(refreshIntervalId);

      showSnackbar(injector, 'Deine Session ist abgelaufen. Bitte melde dich erneut an.', 'OK', ['error-snackbar']);
      await keycloak.logout();
      return;
    }

    try {
      console.log('[KEYCLOAK] Attempting token refresh...'); // ← NEU
      const updated = await keycloak.updateToken(TOKEN_MIN_VALIDITY);

      if (updated) {
        const remainingTime = MAX_SESSION_DURATION - sessionDuration;
        const remainingMinutes = Math.floor(remainingTime / (60 * 1000));
        console.log(`[KEYCLOAK] Token refreshed. Session expires in ${remainingMinutes} minutes`);
      } else {
        console.log('[KEYCLOAK] Token still valid, no refresh needed'); // ← NEU
      }
    } catch (error) {
      console.error('[KEYCLOAK] ✗ Token refresh failed:', error);
      console.log('[KEYCLOAK] Session invalid. Redirecting to login...');
      clearInterval(refreshIntervalId);
      showSnackbar(injector, 'Deine Session ist abgelaufen. Bitte melde dich erneut an.', 'OK', ['error-snackbar']);
      await keycloak.login();
    }
  }, TOKEN_REFRESH_INTERVAL);

  const warningMinutes = 10;
  if (MAX_SESSION_DURATION > warningMinutes * 60 * 1000) {
    const warningTime = MAX_SESSION_DURATION - (warningMinutes * 60 * 1000);
    setTimeout(() => {
      console.log(`[KEYCLOAK] Session expires in ${warningMinutes} minutes`);
      showSnackbar(injector, `Deine Session läuft in ${warningMinutes} Minuten ab.`, 'OK', ['warning-snackbar']);
    }, warningTime);
  }
}

export function provideKeycloakConfig(): Provider[] {
  return [
    {
      provide: APP_INITIALIZER,
      multi: true,
      deps: [KeycloakService, Injector],
      useFactory: initializeKeycloak
    }
  ];
}
