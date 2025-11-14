import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: 'https://auth.htl-leonding.ac.at',
  realm: '2526_5bhitm',
  clientId: 'frontend',
});

let refreshInterval: any;

let _currentToken: string | null = null;
let _decodedClaims: any = null;

export function getToken(): string | null {
  return _currentToken;
}
export function getDecodedClaims(): any {
  return _decodedClaims;
}
export function getGivenName(): string {
  return (_decodedClaims?.given_name ?? _decodedClaims?.givenName ?? '') as string;
}
export function getFamilyName(): string {
  return (_decodedClaims?.family_name ?? _decodedClaims?.familyName ?? '') as string;
}
export function getDisplayName(): string {
  return (_decodedClaims?.display_name ?? _decodedClaims?.name ?? `${getGivenName()} ${getFamilyName()}`).trim();
}
export function getClassFromDN(): string {
  const dn: string | undefined = _decodedClaims?.distinguishedName ?? _decodedClaims?.distinguished_name ?? '';
  if (!dn) return '';
  const matches = dn.match(/OU=([^,]+)/gi);
  if (!matches || matches.length === 0) return '';
  for (const m of matches) {
    const val = m.replace(/^OU=/i, '');
    if (/[0-9]/.test(val) || /HIF|HITM|HEL|HBG|FELA|CIF|BIFT|CIFT|ABIF|ACIF/i.test(val)) {
      return val;
    }
  }
  return matches[0].replace(/^OU=/i, '');
}
export function getIsStudent(): boolean {
  const dn: string | undefined = _decodedClaims?.distinguishedName ?? _decodedClaims?.distinguished_name ?? '';
  if (!dn) {
    return false;
  }
  return /OU=Students/i.test(dn) || /Students/i.test(dn);
}

const createAuthEvent = (token: string | null) => {
  const event = new CustomEvent('auth-state-changed', {
    detail: { token }
  });
  window.dispatchEvent(event);
};

function base64UrlDecode(input: string) {
  input = input.replace(/-/g, '+').replace(/_/g, '/');
  const pad = input.length % 4;
  if (pad) input += '='.repeat(4 - pad);
  try {
    return atob(input);
  } catch (e) {
    console.error('base64 decode failed', e);
    return '';
  }
}

function decodeJwt(token: string | null) {
  if (!token) return null;
  try {
    const parts = token.split('.');
    if (parts.length < 2) return null;
    const payload = parts[1];
    const jsonStr = base64UrlDecode(payload);
    return JSON.parse(jsonStr);
  } catch (err) {
    console.error('Failed to decode JWT', err);
    return null;
  }
}

async function init() {
  try {
    const authenticated = await keycloak.init({
      onLoad: 'login-required',
      checkLoginIframe: false,
      pkceMethod: 'S256',
      enableLogging: true
    });

    console.log(`[KEYCLOAK] authenticated = ${authenticated}`);

    if (!authenticated) {
      await keycloak.login();
    } else {
      _currentToken = keycloak.token ?? null;
      localStorage.setItem('token', _currentToken ?? '');
      _decodedClaims = decodeJwt(_currentToken);

      console.log('TOKEN:', _currentToken);
      console.log('Decoded Payload:', _decodedClaims);

      createAuthEvent(_currentToken);
      await loadUserInfoFromToken();
      setupTokenRefresh();
    }
  } catch (error) {
    console.error('Failed to initialize adapter:', error);
  }
}

export async function loadUserInfoFromToken() {
  _decodedClaims = _decodedClaims ?? (keycloak.tokenParsed ?? null) ?? decodeJwt(_currentToken);
  console.log('loadUserInfoFromToken -> claims', _decodedClaims);
}

function setupTokenRefresh() {
  if (refreshInterval) clearInterval(refreshInterval);

  const parsed = keycloak.tokenParsed as any;
  if (!parsed || !parsed.exp || !parsed.iat) {
    refreshInterval = setInterval(async () => {
      try {
        const refreshed = await keycloak.updateToken(20);
        if (refreshed) {
          _currentToken = keycloak.token ?? null;
          _decodedClaims = decodeJwt(_currentToken);
          console.log('REFRESHED TOKEN:', _currentToken);
          createAuthEvent(_currentToken);
          await loadUserInfoFromToken();
        }
      } catch (err) {
        console.error('Token refresh failed:', err);
        clearInterval(refreshInterval);
        await logout();
      }
    }, 60 * 1000);
    return;
  }

  const tokenLifeSeconds = Math.floor((parsed.exp - parsed.iat) * 0.7);
  refreshInterval = setInterval(async () => {
    try {
      const refreshed = await keycloak.updateToken(20);
      if (refreshed) {
        _currentToken = keycloak.token ?? null;
        _decodedClaims = decodeJwt(_currentToken);
        console.log('REFRESHED TOKEN:', _currentToken);
        localStorage.setItem('token', _currentToken ?? '');
        createAuthEvent(_currentToken);
        await loadUserInfoFromToken();
      }
    } catch (err) {
      console.error('Token refresh failed:', err);
      clearInterval(refreshInterval);
      await logout();
    }
  }, tokenLifeSeconds * 1000);
}

export async function logout() {
  if (refreshInterval) clearInterval(refreshInterval);
  _currentToken = null;
  _decodedClaims = null;
  localStorage.removeItem('token');
  createAuthEvent(null);
  await keycloak.logout();
}

bootstrapApplication(AppComponent, appConfig).catch(err => console.error(err));
init();

export const KeycloakState = {
  getToken,
  getDecodedClaims,
  getGivenName,
  getFamilyName,
  getDisplayName,
  getClassFromDN,
  getIsStudent,
};
