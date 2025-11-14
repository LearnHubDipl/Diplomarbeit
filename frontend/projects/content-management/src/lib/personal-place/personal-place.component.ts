import { Component, OnInit } from '@angular/core';
import { KeycloakOperationService } from '../../../../shared/src/lib/auth';
import {NgIf} from '@angular/common';

@Component({
  selector: 'lib-personal-place',
  standalone: true,
  templateUrl: './personal-place.component.html',
  imports: [
    NgIf
  ],
  styleUrls: ['./personal-place.component.css']
})
export class PersonalPlaceComponent implements OnInit {
  givenName = '';
  familyName = '';
  displayName = '';
  klasse = '';
  isStudent = false;
  token = '';
  isTeacher = false;
  email = '';

  constructor(private keycloakService: KeycloakOperationService) {}

  async ngOnInit() {
    await this.refreshUserData();
  }

  async refreshUserData() {
    try {
      this.token = await this.keycloakService.getToken();
      this.givenName = this.keycloakService.getGivenName();
      this.familyName = this.keycloakService.getFamilyName();
      this.displayName = this.keycloakService.getDisplayName();
      this.klasse = this.keycloakService.getClassFromDN();
      this.isStudent = this.keycloakService.getIsStudent();
      this.isTeacher = this.keycloakService.getIsTeacher();
      this.email = this.keycloakService.getEmail();

      console.log('PersonalPlaceComponent -> token:', this.token);
      console.log('claims:', this.keycloakService.getDecodedToken());
      console.log(
        'givenName, familyName, displayName, klasse, isStudent:',
        this.givenName,
        this.familyName,
        this.displayName,
        this.klasse,
        this.isStudent,
        this.isTeacher,
        this.email
      );
    } catch (err) {
      console.error('Error refreshing personal place:', err);
    }
  }

  async doLogout() {
    await this.keycloakService.logout();
  }
}
