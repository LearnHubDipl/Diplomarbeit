import { Component } from '@angular/core';
import {RouterLink, RouterOutlet} from "@angular/router";
import {NavbarComponent} from '../navbar/navbar.component';
import {FooterComponent} from '../footer/footer.component';

@Component({
  selector: 'lib-navigation',
  imports: [
    RouterOutlet,
    RouterLink,
    NavbarComponent,
    FooterComponent
  ],
  templateUrl: './navigation.component.html',
  styleUrl: './navigation.component.css'
})
export class NavigationComponent {

}
