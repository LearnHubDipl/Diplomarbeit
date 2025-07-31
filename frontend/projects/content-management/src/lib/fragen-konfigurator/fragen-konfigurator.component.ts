import { Component } from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {NgForOf} from '@angular/common';

@Component({
  selector: 'lib-fragen-konfigurator',
  imports: [
    ReactiveFormsModule,
    NgForOf
  ],
  templateUrl: './fragen-konfigurator.component.html',
  styleUrl: './fragen-konfigurator.component.css'
})
export class FragenKonfiguratorComponent {

}
