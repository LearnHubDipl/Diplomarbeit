import { APP_INITIALIZER, Provider } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';

export function initializeKeycloak(keycloak: KeycloakService) {
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
      deps: [KeycloakService],
      useFactory: initializeKeycloak
    }
  ];
}
