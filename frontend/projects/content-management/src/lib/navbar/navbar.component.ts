import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router, NavigationEnd } from '@angular/router';
import { filter, Subscription, timer, switchMap } from 'rxjs';
import {NotificationsService} from '../../../../shared/src/lib/services/notification.service';


@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './navbar.component.html',
  styleUrls: ['navbar.component.css']
})
export class NavbarComponent implements OnInit, OnDestroy {
  unreadCount = 0;

  private sub = new Subscription();

  constructor(
    private notifications: NotificationsService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUnread();

    this.sub.add(
      this.router.events.pipe(filter(e => e instanceof NavigationEnd)).subscribe(() => {
        this.loadUnread();
      })
    );

    this.sub.add(
      timer(0, 30000).pipe(
        switchMap(() => this.notifications.listMe())
      ).subscribe({
        next: list => this.unreadCount = (list ?? []).filter(n => !n.read).length,
        error: () => { }
      })
    );
  }

  private loadUnread(): void {
    this.notifications.listMe().subscribe({
      next: (list: any) => this.unreadCount = (list ?? []).filter((n: { read: any; }) => !n.read).length,
      error: () => this.unreadCount = 0
    });
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }
}
