import { Component } from '@angular/core';
import {RouterLink, RouterOutlet} from "@angular/router";

@Component({
  selector: 'app-trainer-layout',
  imports: [
    RouterLink,
    RouterOutlet
  ],
  templateUrl: './trainer-layout.component.html',
  styleUrl: './trainer-layout.component.css'
})
export class TrainerLayoutComponent {

}
