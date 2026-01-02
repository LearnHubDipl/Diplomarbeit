import {Injectable, OnInit} from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { KeycloakTokenParsed } from 'keycloak-js';

@Injectable({
  providedIn: 'root'
})
export class KeycloakOperationService{

  constructor(private readonly keycloak: KeycloakService) {}

  isLoggedIn(): boolean {
    return this.keycloak.isLoggedIn();
  }

  async logout(): Promise<void> {
    await this.keycloak.logout(window.location.origin);
  }

  async getToken(): Promise<string> {
    return await this.keycloak.getToken() ?? '';
  }

  async getUserProfile(): Promise<any> {
    return this.keycloak.loadUserProfile();
  }

  getUserRoles(): string[] {
    return this.keycloak.getKeycloakInstance()?.realmAccess?.roles ?? [];
  }

  getDecodedToken(): KeycloakTokenParsed | undefined {
    return this.keycloak.getKeycloakInstance()?.tokenParsed;
  }

  getGivenName(): string {
    const claims = this.getDecodedToken();
    return (claims?.['given_name'] || claims?.['givenName'] || '') as string;
  }

  getFamilyName(): string {
    const claims = this.getDecodedToken();
    return (claims?.['family_name'] || claims?.['familyName'] || '') as string;
  }

  getDisplayName(): string {
    const claims = this.getDecodedToken();
    return (
      claims?.['display_name'] ||
      claims?.['name'] ||
      `${this.getGivenName()} ${this.getFamilyName()}`
    ).trim();
  }
  getIsTeacher(): boolean {
    return !this.getIsStudent();
  }

  getClassFromDN(): string {
    const claims = this.getDecodedToken();
    const dn: string = claims?.['distinguishedName'] || claims?.['distinguished_name'] || '';

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

  /**getIsStudent(): boolean {
    const claims = this.getDecodedToken();
    const dn: string = claims?.['distinguishedName'] || claims?.['distinguished_name'] || '';
    if (!dn) return false;
    return /OU=Students/i.test(dn) || /Students/i.test(dn);
  }**/

  getIsStudent(): boolean {
    return false; // TEMP: Test als Lehrkraft
  }

  getEmail(): string {
    const claims = this.getDecodedToken();
    return (claims?.['email'] || '') as string;
  }
}
