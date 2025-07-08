import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {TestCommonServiceComponent} from '../test-common-service/test-common-service.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, TestCommonServiceComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'maturatrainer';
}
