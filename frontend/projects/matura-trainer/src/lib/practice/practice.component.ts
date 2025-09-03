import { Component } from '@angular/core';
import {QuestionRunnerComponent} from '../question-runner/question-runner.component';

@Component({
  selector: 'lib-practice',
  imports: [
    QuestionRunnerComponent
  ],
  templateUrl: './practice.component.html',
  styleUrl: './practice.component.css'
})
export class PracticeComponent {

}
