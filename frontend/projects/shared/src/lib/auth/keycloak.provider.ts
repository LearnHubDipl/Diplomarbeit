import { APP_INITIALIZER, Provider, Injector } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { UserInitializationService } from '../services/user-initialization.service';

export function initializeKeycloak(keycloak: KeycloakService, injector: Injector) {
  return async () => {
    try {
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
      console.log('[KEYCLOAK] initialized:', authenticated);

      // Initialize user in backend after successful Keycloak authentication
      if (authenticated) {
        // Wait for token to be fully available and refresh if needed
        await keycloak.updateToken(30);

        const userInitService = injector.get(UserInitializationService);
        await userInitService.initializeUser();
      }

      return authenticated;
    } catch (error) {
      console.error('[KEYCLOAK] Init Error:', error);
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
