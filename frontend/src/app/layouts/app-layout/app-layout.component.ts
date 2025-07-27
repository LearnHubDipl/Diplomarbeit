import { Component } from '@angular/core';
import {RouterLink, RouterOutlet} from "@angular/router";
import {NgOptimizedImage} from '@angular/common';
import {NavigationComponent} from '../../../../projects/content-management/src/lib/navigation/navigation.component';

@Component({
  selector: 'app-app-layout',
  imports: [
    RouterLink,
    RouterOutlet,
    NgOptimizedImage,
    NavigationComponent
  ],
  templateUrl: './app-layout.component.html',
  styleUrl: './app-layout.component.css'
})
export class AppLayoutComponent {

}
