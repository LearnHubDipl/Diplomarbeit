import {Component, inject, OnInit} from '@angular/core';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {UserService} from '../../../../shared/src/lib/services/user.service';
import {UserSlim} from '../../../../shared/src/lib/interfaces/userSlim';

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
  userService: UserService = inject(UserService);
  user: UserSlim | null = null;
  sidebarOpen = false;

  ngOnInit() {
    this.userService.getUserById(1).subscribe(user => this.user = user);
  }
}
