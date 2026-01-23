import { Component } from '@angular/core';
import { RouterOutlet } from "@angular/router";
import {
  SideNavigationComponent
} from '../../../../projects/matura-trainer/src/lib/side-navigation/side-navigation.component';
import {BreadCrumbsComponent} from '../../../../projects/matura-trainer/src/lib/bread-crumbs/bread-crumbs.component';
import {NavbarComponent} from '../../../../projects/content-management/src/lib/navbar/navbar.component';
import {FooterComponent} from '../../../../projects/content-management/src/lib/footer/footer.component';

@Component({
  selector: 'app-trainer-layout',
  imports: [
    RouterOutlet,
    SideNavigationComponent,
    BreadCrumbsComponent,
    NavbarComponent,
    FooterComponent
  ],
  templateUrl: './trainer-layout.component.html',
  styleUrl: './trainer-layout.component.css'
})
export class TrainerLayoutComponent {

}
