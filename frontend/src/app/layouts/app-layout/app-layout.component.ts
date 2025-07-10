import { Component } from '@angular/core';
import {RouterLink, RouterOutlet} from "@angular/router";
import {NgOptimizedImage} from '@angular/common';

@Component({
  selector: 'app-app-layout',
  imports: [
    RouterLink,
    RouterOutlet,
    NgOptimizedImage
  ],
  templateUrl: './app-layout.component.html',
  styleUrl: './app-layout.component.css'
})
export class AppLayoutComponent {

}
