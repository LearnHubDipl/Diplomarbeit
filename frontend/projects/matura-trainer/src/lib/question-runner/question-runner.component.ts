import {Component, inject, OnInit} from '@angular/core';
import { Question } from '../../../../shared/src/lib/interfaces/question';
import { QuestionService } from '../../../../shared/src/lib/services/question.service';
import {NgForOf, NgStyle} from '@angular/common';
import {FormArray, FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';


@Component({
  selector: 'lib-question-runner',
  imports: [
    NgForOf,
    NgStyle,
    ReactiveFormsModule
  ],
  templateUrl: './question-runner.component.html',
  styleUrls: [
    './question-runner.component.css',
    '../styles/shared-styles.css'
  ]
})
export class QuestionRunnerComponent implements OnInit {
  questionService = inject(QuestionService);
  fb = inject(FormBuilder);

  question: Question | undefined;
  form: FormGroup = this.fb.group({
    answers: this.fb.array([])
  });

  ngOnInit() {
    this.questionService.getQuestionById(7).subscribe(q => {
      this.question = q;

      const answerControls = this.fb.array(q.answers.map(() => false));
      this.form.setControl('answers', answerControls);
    });
  }

  get answersArray(): FormArray {
    return this.form.get('answers') as FormArray;
  }

  anySelected(): boolean {
    return this.answersArray?.controls.some(ctrl => ctrl.value);
  }

  submit(): void {
    const selectedAnswers = this.answersArray.controls
      .map((ctrl, index) => ctrl.value ? this.question?.answers[index] : null)
      .filter(Boolean);

    console.log('Selected answers:', selectedAnswers);
  }
}

