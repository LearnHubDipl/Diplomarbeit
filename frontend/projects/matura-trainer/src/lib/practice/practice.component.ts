import {Component, inject, Input, OnInit} from '@angular/core';
import {QuestionRunnerComponent} from '../question-runner/question-runner.component';
import {Router} from '@angular/router';

@Component({
  selector: 'lib-practice',
  imports: [
    QuestionRunnerComponent
  ],
  templateUrl: './practice.component.html',
  styleUrl: './practice.component.css'
})
export class PracticeComponent implements OnInit {
    @Input() questionIdList: number[] = [];
    router: Router = inject(Router);

    ngOnInit() {
      this.questionIdList = history.state['questionIdList'];
      console.log('questionIdList:', this.questionIdList);
    }
}
