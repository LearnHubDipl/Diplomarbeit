import { Component } from '@angular/core';
import { NgForOf } from '@angular/common';
import {RevealOnScrollDirective} from './reveal-on-scroll.directive';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'lib-home',
  standalone: true,
  imports: [
    NgForOf,
    RevealOnScrollDirective,
    RouterLink
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent {}
