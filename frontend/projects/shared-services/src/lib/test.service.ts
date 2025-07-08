import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TestService {
  getTestMessage(): string {
    return 'Hallo aus dem Shared Service!';
  }
  constructor() { }
}
