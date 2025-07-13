import { Component } from '@angular/core';
import { RouterOutlet } from "@angular/router";
import {
  SideNavigationComponent
} from '../../../../projects/matura-trainer/src/lib/side-navigation/side-navigation.component';
import {BreadCrumbsComponent} from '../../../../projects/matura-trainer/src/lib/bread-crumbs/bread-crumbs.component';

@Component({
  selector: 'app-trainer-layout',
  imports: [
    RouterOutlet,
    SideNavigationComponent,
    BreadCrumbsComponent
  ],
  templateUrl: './trainer-layout.component.html',
  styleUrl: './trainer-layout.component.css'
})
export class TrainerLayoutComponent {

}
