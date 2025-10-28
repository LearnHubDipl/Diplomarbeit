import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import Keycloak from 'keycloak-js';

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));

// Create Keycloak instance
const keycloak = new Keycloak({
  url: 'https://auth.htl-leonding.ac.at',
  realm: '2526_5bhitm',
  clientId: 'frontend',
});

// Setup token refresh interval
let refreshInterval: any;

// Create a custom event for auth changes
const createAuthEvent = (token: string | null) => {
  const event = new CustomEvent('auth-state-changed', {
    detail: { token }
  });
  window.dispatchEvent(event);
};

// Keycloak initialization function
async function init() {
  try {
    const authenticated = await keycloak.init({
      onLoad: 'login-required',
      checkLoginIframe: false,
      pkceMethod: 'S256',
      enableLogging: true
    });
    console.log(`User is ${authenticated ? 'authenticated' : 'not authenticated'}`);

    if (!authenticated) {
      await keycloak.login();
    } else {
      // Store token in localStorage
      localStorage.setItem('token', keycloak.token!);

      // Notify application of authentication
      createAuthEvent(keycloak.token!);

      // Setup token refresh
      setupTokenRefresh();
    }
  } catch (error) {
    console.error('Failed to initialize adapter:', error);
  }
}

// Setup token refresh interval
function setupTokenRefresh() {
  // Clear any existing interval
  if (refreshInterval) {
    clearInterval(refreshInterval);
  }

  // Set up token refresh (70% of token lifespan)
  const tokenRefreshSeconds = Math.floor((keycloak.tokenParsed?.exp! - keycloak.tokenParsed?.iat!) * 0.7);
  refreshInterval = setInterval(async () => {
    try {
      const refreshed = await keycloak.updateToken(20);
      if (refreshed) {
        console.log('Token refreshed');
        localStorage.setItem('token', keycloak.token!);

        // Notify of token update
        createAuthEvent(keycloak.token!);
      }
    } catch (error) {
      console.error('Failed to refresh token:', error);
      clearInterval(refreshInterval);
      await keycloak.logout();
    }
  }, tokenRefreshSeconds * 1000);
}

export function logout() {
  if (refreshInterval) {
    clearInterval(refreshInterval);
  }

  localStorage.removeItem('token');

  createAuthEvent(null);

  keycloak.logout();
}

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));

init();
