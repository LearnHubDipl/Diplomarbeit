import {Component, OnInit} from '@angular/core';
import { NgForOf } from '@angular/common';
import {RevealOnScrollDirective} from './reveal-on-scroll.directive';
import {RouterLink} from '@angular/router';
import {KeycloakService} from 'keycloak-angular';

@Component({
  selector: 'lib-home',
  standalone: true,
  imports: [
    RevealOnScrollDirective,
    RouterLink
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  constructor(private keycloak: KeycloakService) {}

  ngOnInit(): void {
    console.log('[TOKEN PARSED]', this.keycloak.getKeycloakInstance().tokenParsed);
    console.log('[REALM ROLES]', this.keycloak.getKeycloakInstance().realmAccess?.roles);
    console.log('[RESOURCE ACCESS]', this.keycloak.getKeycloakInstance().resourceAccess);
  }
}
