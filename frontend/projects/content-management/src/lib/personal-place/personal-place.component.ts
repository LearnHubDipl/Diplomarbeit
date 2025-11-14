import { Component, OnDestroy, OnInit } from '@angular/core';
import {
  getClassFromDN,
  getDecodedClaims,
  getDisplayName,
  getFamilyName,
  getGivenName,
  getIsStudent,
  getToken,
  logout
} from '../../../../../src/main';
import {NgIf, SlicePipe} from '@angular/common'; // passe Pfad an, falls nötig

@Component({
  selector: 'lib-personal-place',
  templateUrl: './personal-place.component.html',
  imports: [
    NgIf
  ],
  styleUrls: ['./personal-place.component.css']
})
export class PersonalPlaceComponent implements OnInit, OnDestroy {
  givenName: string = '';
  familyName: string = '';
  displayName: string = '';
  klasse: string = '';
  isStudent: boolean = false;
  token: string | null = null;

  private authListener = (ev: any) => {
    this.refreshFromMain();
  };

  ngOnInit(): void {
    this.refreshFromMain();
    window.addEventListener('auth-state-changed', this.authListener);
  }

  ngOnDestroy(): void {
    window.removeEventListener('auth-state-changed', this.authListener);
  }

  refreshFromMain() {
    try {
      this.token = getToken();
      this.givenName = getGivenName();
      this.familyName = getFamilyName();
      this.displayName = getDisplayName();
      this.klasse = getClassFromDN() ?? '';
      this.isStudent = getIsStudent();

      console.log('PersonalPlaceComponent -> token:', this.token);
      console.log('claims:', getDecodedClaims());
      console.log('givenName, familyName, displayName, klasse, isStudent:',
        this.givenName, this.familyName, this.displayName, this.klasse, this.isStudent);
    } catch (err) {
      console.error('Error refreshing personal place:', err);
    }
  }

  doLogout() {
    logout();
  }
}
