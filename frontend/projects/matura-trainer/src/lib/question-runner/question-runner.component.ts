import {Component, EventEmitter, inject, Input, OnInit, Output} from '@angular/core';
import { Question } from '../../../../shared/src/lib/interfaces/question';
import { QuestionService } from '../../../../shared/src/lib/services/question.service';
import {NgClass, NgForOf, NgIf, NgStyle} from '@angular/common';
import {FormArray, FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {CheckAnswerRequest} from '../../../../shared/src/lib/interfaces/answer';
import {AnswerService} from '../../../../shared/src/lib/services/answer.service';
import {ActivatedRoute} from '@angular/router';
import {Location} from '@angular/common';


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
  @Input() mode: 'practice' | 'exam' = 'practice';
  @Output() answered = new EventEmitter<CheckAnswerRequest>();
  @Input() questionIdList: number[] = [];

  currentQuestionIndex: number = 0;
  isFinished = false;

  questionService = inject(QuestionService);
  answerService = inject(AnswerService);
  fb = inject(FormBuilder);
  route: ActivatedRoute = inject(ActivatedRoute);
  location: Location = inject(Location)

  question: Question | undefined;
  form: FormGroup = this.fb.group({
    answers: this.fb.array([]),
    freeTextAnswer: ['']
  });

  previousAnswers: { [questionId: number]: CheckAnswerRequest } = {};

  answerResult: {
    correct: boolean;
    correctAnswerIds: number[] | null;
    correctFreeTextAnswers: string[] | null;
  } | null = null;

  submitted = false;

  ngOnInit() {
    let state = history.state;
    if (state?.questionIds) {
      this.questionIdList = state.questionIds;
    }

    if (this.questionIdList.length > 0) {
      this.loadQuestion(this.questionIdList[this.currentQuestionIndex]);
    } else {
      // TODO: Screen that states that there are no question that could be loaded
    }
  }

  loadQuestion(id: number) {
    this.questionService.getQuestionById(id).subscribe(q => {
      this.question = q;

      let previous = this.previousAnswers[q.id];

      if (q.type === 'MULTIPLE_CHOICE') {
        let answerControls = this.fb.array(
          q.answers.map((a, i) => previous?.selectedAnswerIds?.includes(a.id) ?? false)
        );
        this.form.setControl('answers', answerControls);
        this.form.get('freeTextAnswer')?.disable();
      } else if (q.type === 'FREETEXT') {
        this.form.setControl('answers', this.fb.array([]));
        this.form.get('freeTextAnswer')?.enable();
        this.form.get('freeTextAnswer')?.setValue(previous?.freeTextAnswer ?? '');
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
    let payload = this.buildPayLoad();
    if (this.mode === 'practice') {
      // practice mode: give instant feedback
      this.answerService.checkAnswers(payload).subscribe(result => {
        this.answerResult = {
          correct: result.correct,
          correctAnswerIds: result.correctAnswerIds ?? null,
          correctFreeTextAnswers: result.correctFreeTextAnswers ?? null
        };

        this.lockInputs();
        this.submitted = true;
        this.advance();
      });
    } else {
      // exam mode: just emit to parent
      this.previousAnswers[this.question!.id] = payload;
      this.answered.emit(payload);
    }
  }

  buildPayLoad() : CheckAnswerRequest {
    if (!this.question) throw new Error("There is no Question selected");

    let payload: CheckAnswerRequest;
    if (this.question.type === 'MULTIPLE_CHOICE') {
      const selectedAnswerIds = this.answersArray.controls
        .map((ctrl, index) => ctrl.value ? this.question!.answers[index].id : null)
        .filter((id): id is number => id !== null);

      payload = {
        questionId: this.question.id,
        selectedAnswerIds,
        freeTextAnswer: null
      };
    } else {
      payload = {
        questionId: this.question.id,
        selectedAnswerIds: [],
        freeTextAnswer: this.form.get('freeTextAnswer')?.value.trim() ?? null
      };
    }
    return payload;
  }

  private lockInputs() {
    if (this.question?.type === 'MULTIPLE_CHOICE') {
      this.answersArray.controls.forEach(ctrl => ctrl.disable());
    } else if (this.question?.type === 'FREETEXT') {
      this.form.get('freeTextAnswer')?.disable();
    }
  }

  private advance() {
    this.currentQuestionIndex++;
    if (this.currentQuestionIndex >= this.questionIdList.length) {
      this.isFinished = true;
    }
  }

  loadNextQuestion(): void {
    if (!this.isFinished) {
      this.answerResult = null;
      this.submitted = false;
      let nextIndex = this.questionIdList![this.currentQuestionIndex];
      this.loadQuestion(nextIndex);
    }
  }

  navigateExam(direction: 'next' | 'prev') {
    this.submit(); // always emit current answer

    if (direction === 'next') {
      if (this.currentQuestionIndex < this.questionIdList.length - 1) {
        this.currentQuestionIndex++;
        this.loadQuestion(this.questionIdList[this.currentQuestionIndex]);
      } else {
        this.isFinished = true;
      }
    } else if (direction === 'prev') {
      if (this.currentQuestionIndex > 0) {
        this.currentQuestionIndex--;
        this.loadQuestion(this.questionIdList[this.currentQuestionIndex]);
      }
    }
  }


  get hasSolutions(): boolean {
    return (this.question?.solutions?.length ?? 0) > 0;
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

  finish() {
    this.submit()
    this.location.back()
  }
}

