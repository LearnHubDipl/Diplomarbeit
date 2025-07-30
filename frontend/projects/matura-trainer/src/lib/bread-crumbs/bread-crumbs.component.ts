import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterLink} from "@angular/router";
import {filter} from 'rxjs';
import {NgForOf, NgIf} from '@angular/common';

/* unique to this component */
interface Breadcrumb {
  label: string;
  url: string;
}

@Component({
  selector: 'lib-bread-crumbs',
  imports: [
    RouterLink,
    NgForOf,
    NgIf
  ],
  templateUrl: './bread-crumbs.component.html',
  styleUrl: './bread-crumbs.component.css'
})
export class BreadCrumbsComponent implements OnInit {
  breadcrumbs: Breadcrumb[] = [];
  router: Router = inject(Router);
  activatedRoute: ActivatedRoute = inject(ActivatedRoute);

  constructor() {}

  ngOnInit(): void {
    this.breadcrumbs = this.buildBreadcrumbs(this.activatedRoute.root)

    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.breadcrumbs = this.buildBreadcrumbs(this.activatedRoute.root);
      });
  }

  private buildBreadcrumbs(route: ActivatedRoute, url: string = '', breadcrumbs: Breadcrumb[] = []): Breadcrumb[] {
    let children: ActivatedRoute[] = route.children;

    if (children.length === 0) return breadcrumbs;

    for (let child of children) {
      let routeURL: string = child.snapshot.url.map(segment => segment.path).join('/');
      if (routeURL !== '') {
        url += `/${routeURL}`;
      }

      let label = child.snapshot.data['breadcrumb'];
      if (label) {
        breadcrumbs.push({ label, url });
      }

      return this.buildBreadcrumbs(child, url, breadcrumbs);
    }

    return breadcrumbs;
  }
}
