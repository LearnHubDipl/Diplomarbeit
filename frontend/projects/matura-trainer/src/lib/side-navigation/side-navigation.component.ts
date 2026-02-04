import {Component, inject, OnInit} from '@angular/core';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {UserSlim} from '../../../../shared/src/lib/interfaces/userSlim';
import {UserInitializationService} from '../../../../shared/src/lib/services/user-initialization.service';

@Component({
  selector: 'lib-side-navigation',
  imports: [
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './side-navigation.component.html',
  styleUrl: './side-navigation.component.css'
})
export class SideNavigationComponent implements OnInit {
  userService: UserInitializationService = inject(UserInitializationService);
  user: UserSlim | null = null;
  sidebarOpen = false;

  ngOnInit() {
    this.user = this.userService.getCurrentUser();
  }
}
