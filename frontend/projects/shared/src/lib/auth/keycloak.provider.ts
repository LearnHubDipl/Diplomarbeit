import { APP_INITIALIZER, Provider, Injector } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { UserInitializationService } from '../services/user-initialization.service';

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
        }
      });

      console.log('[KEYCLOAK] Authentication status:', authenticated);

      if (authenticated) {
        await keycloak.updateToken(30);

        const token = await keycloak.getToken();
        console.log('[KEYCLOAK] Token obtained, length:', token?.length || 0);

        console.log('[KEYCLOAK] Initializing user in backend...');
        const userInitService = injector.get(UserInitializationService);

        try {
          const user = await userInitService.initializeUser();

          if (user) {
            console.log('[KEYCLOAK] ✓ User initialized in backend');
            console.log('[KEYCLOAK] User ID:', user.id);
            console.log('[KEYCLOAK] Keycloak Sub:', user.keycloakSub);
          } else {
            console.warn('[KEYCLOAK] ⚠ User initialization returned null');
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
