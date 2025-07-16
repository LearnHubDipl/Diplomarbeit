import {Component, inject, OnInit} from '@angular/core';
import { Question } from '../../../../shared/src/lib/interfaces/question';
import { QuestionService } from '../../../../shared/src/lib/services/question.service';
import {NgClass, NgForOf, NgIf, NgStyle} from '@angular/common';
import {FormArray, FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {CheckAnswerRequest} from '../../../../shared/src/lib/interfaces/answer';
import {AnswerService} from '../../../../shared/src/lib/services/answer.service';


@Component({
  selector: 'lib-question-runner',
  imports: [
    NgForOf,
    NgStyle,
    ReactiveFormsModule,
    NgClass,
    NgIf
  ],
  templateUrl: './question-runner.component.html',
  styleUrls: [
    './question-runner.component.css',
    '../styles/shared-styles.css'
  ]
})
export class QuestionRunnerComponent implements OnInit {
  questionService = inject(QuestionService);
  answerService = inject(AnswerService);
  fb = inject(FormBuilder);

  question: Question | undefined;
  form: FormGroup = this.fb.group({
    answers: this.fb.array([]),
    freeTextAnswer: ['']
  });

  answerResult: {
    correct: boolean;
    correctAnswerIds: number[] | null;
    correctFreeTextAnswers: string[] | null;
  } | null = null;

  submitted = false;

  ngOnInit() {
    this.loadQuestion(1)
  }

  loadQuestion(id: number) {
    this.questionService.getQuestionById(id).subscribe(q => {
      this.question = q;
      this.answerResult = null;
      this.submitted = false;

      if (q.type === 'MULTIPLE_CHOICE') {
        const answerControls = this.fb.array(q.answers.map(() => false));
        this.form.setControl('answers', answerControls);
        this.form.get('freeTextAnswer')?.disable();
      } else if (q.type === 'FREETEXT') {
        this.form.setControl('answers', this.fb.array([])); // clear answers array
        this.form.get('freeTextAnswer')?.enable();
        this.form.get('freeTextAnswer')?.setValue('');
      }
    });
  }

  get answersArray(): FormArray {
    return this.form.get('answers') as FormArray;
  }

  anySelected(): boolean {
    if (this.question?.type === 'MULTIPLE_CHOICE') {
      return this.answersArray?.controls.some(ctrl => ctrl.value);
    } else if (this.question?.type === 'FREETEXT') {
      return (this.form.get('freeTextAnswer')?.value ?? '').trim().length > 0;
    }
    return false;
  }

  submit(): void {
    if (!this.question) return;

    let payload: CheckAnswerRequest;

    if (this.question.type === 'MULTIPLE_CHOICE') {
      const selectedAnswerIds = this.answersArray.controls
        .map((ctrl, index) => ctrl.value ? this.question!.answers[index].id : null)
        .filter((id): id is number => id !== null);

      payload = {
        questionId: this.question.id,
        selectedAnswerIds: selectedAnswerIds,
        freeTextAnswer: null
      };
    } else {
      payload = {
        questionId: this.question.id,
        selectedAnswerIds: [],
        freeTextAnswer: this.form.get('freeTextAnswer')?.value.trim() ?? null
      };
    }

    this.answerService.checkAnswers(payload).subscribe(result => {
      this.answerResult = {
        correct: result.correct,
        correctAnswerIds: result.correctAnswerIds ?? null,
        correctFreeTextAnswers: result.correctFreeTextAnswers ?? null
      };

      // lock inputs
      if (this.question?.type === 'MULTIPLE_CHOICE') {
        this.answersArray.controls.forEach(ctrl => ctrl.disable());
      } else if (this.question?.type === 'FREETEXT') {
        this.form.get('freeTextAnswer')?.disable();
      }

      this.submitted = true;
    });
  }

  loadNextQuestion(): void {
    const nextId = (this.question?.id ?? 0) + 1;

    this.loadQuestion(nextId);
  }


  isCorrectAnswer(answerId: number): boolean {
    return this.answerResult?.correctAnswerIds?.includes(answerId) ?? false;
  }

  isIncorrectAnswer(answerId: number, index: number): boolean {
    if (!this.answerResult?.correctAnswerIds) return false;

    const wasSelected = this.answersArray.at(index).value;
    const isActuallyCorrect = this.answerResult.correctAnswerIds.includes(answerId);

    return wasSelected && !isActuallyCorrect;
  }
}

