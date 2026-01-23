import {Component, inject} from '@angular/core';
import {Router} from '@angular/router';

@Component({
  selector: 'lib-not-found',
  imports: [],
  templateUrl: './not-found.component.html',
  styleUrl: './not-found.component.css'
})
export class NotFoundComponent {
  router: Router = inject(Router);
  navigateHome(): void {
    this.router.navigate(['/']);
  }
}
