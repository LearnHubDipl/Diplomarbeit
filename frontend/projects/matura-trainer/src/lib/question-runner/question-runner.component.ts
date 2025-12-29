import {Component, EventEmitter, inject, Input, OnInit, Output} from '@angular/core';
import { Question } from '../../../../shared/src/lib/interfaces/question';
import { QuestionService } from '../../../../shared/src/lib/services/question.service';
import {NgClass, NgForOf, NgIf, NgStyle} from '@angular/common';
import {FormArray, FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {CheckAnswerRequest} from '../../../../shared/src/lib/interfaces/answer';
import {AnswerService} from '../../../../shared/src/lib/services/answer.service';
import {ActivatedRoute, Router} from '@angular/router';
import {Location} from '@angular/common';
import {Exam} from '../../../../shared/src/lib/interfaces/exam';
import {QuestionPoolService} from '../../../../shared/src/lib/services/question-pool.service';
import {StatsService} from '../../../../shared/src/lib/services/stats.service';
import {SolutionService} from '../../../../shared/src/lib/services/solution.service';
import {Solution} from '../../../../shared/src/lib/interfaces/solution';
import {Observable} from 'rxjs';
import {CreatedExamResponse, ExamService} from '../../../../shared/src/lib/services/exam.service';


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
  @Input() mode: 'practice' | 'exam' | 'review' = 'practice';
  @Input() exam?: Exam;   // only needed for review mode
  @Input() timeLimit?: number = 0;
  @Output() answered = new EventEmitter<CheckAnswerRequest>();
  @Output() finishedExam = new EventEmitter<CheckAnswerRequest[]>(); // emit all answers at the end of exam
  @Input() questionIdList: number[] = [];

  errorMessage: string | null = null;
  lastFailedAction: 'loadQuestion' | 'submitAnswer' | 'vote' | null = null;
  loading: boolean = false;

  currentQuestionIndex: number = 0;
  isFinished = false;

  userId = 1;
  voteCounts: { [solutionId: number]: number } = {};

  questionService = inject(QuestionService);
  answerService = inject(AnswerService);
  questionPoolService = inject(QuestionPoolService);
  solutionService = inject(SolutionService);
  examService = inject(ExamService);
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
  showAllSolutions = false;

  timeLeft: number = 0;

  router: Router = inject(Router);


  ngOnInit() {
    let state = history.state;
    if (this.mode !== 'review') {
      if (state?.questionIds) {
        this.questionIdList = state.questionIds;
      }

      if (this.questionIdList.length > 0) {
        this.loadQuestion(this.currentQuestionIndex);
      } else {
        // TODO: Screen that states that there are no question that could be loaded
      }
    } else {
      this.loadQuestion(this.currentQuestionIndex)
    }
    if (this.mode === 'exam') {
      this.timeLeft = this.timeLimit! * 60
      this.timerTickDown()
    } else if (this.mode === 'review') {
      let started = new Date(this.exam!.startedAt);
      let finished = new Date(this.exam!.finishedAt);

      let elapsed = (finished.getTime() - started.getTime()) / 1000;
      this.timeLeft = this.exam!.timeLimit*60 - elapsed;
      console.log(this.exam, this.timeLeft)
    }
  }

  loadQuestion(index: number) {
    if (this.mode === 'review' && this.exam) {
      let reviewed = this.exam.questions[index]
      if (reviewed) {
        this.question = reviewed.question;

        // Restore user’s answer
        if (this.question.type === 'MULTIPLE_CHOICE') {
          let answerControls = this.fb.array(
            this.question.answers.map(a => reviewed.selectedAnswers?.some(sa => sa.id === a.id) ?? false)
          );
          this.form.setControl('answers', answerControls);
          this.form.get('freeTextAnswer')?.disable();
        } else if (this.question.type === 'FREETEXT') {
          this.form.setControl('answers', this.fb.array([]));
          this.form.get('freeTextAnswer')?.setValue(reviewed.freeTextAnswer ?? '');
          this.form.get('freeTextAnswer')?.disable();
        }

        // Lock and set evaluation
        this.answerResult = {
          correct: reviewed.isCorrect,
          correctAnswerIds: reviewed.correctAnswerIds ?? null,
          correctFreeTextAnswers: reviewed.correctFreeTextAnswers ?? null
        };

        this.lockInputs();
        this.submitted = true;
        return;
      }
    }

    let id = this.questionIdList[index]
    if(!id) return;

    this.loading = true;
    this.clearError();

    this.questionService.getQuestionById(id).subscribe({
      next: q => {
        this.loading = false;
        this.question = q;
        //this.sortSolutions();

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
      },
      error: err => {
        this.loading = false;
        this.handleError(
          'Die Frage konnte nicht geladen werden.',
          'loadQuestion',
          err
        )
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
      this.answerService.checkAnswers(payload).subscribe({
        next: result => {
          this.answerResult = {
            correct: result.correct,
            correctAnswerIds: result.correctAnswerIds ?? null,
            correctFreeTextAnswers: result.correctFreeTextAnswers ?? null
          };

          this.lockInputs();
          this.submitted = true;

          //testuser
          const userId = 1;

          if (result.correct) {
            this.questionPoolService.increaseCorrectCount(this.question!.id, userId)
              .subscribe(() => console.log('CorrectCount erhöht'));
          }

          this.advance();
        },
        error: err => {
          this.handleError(
            'Die Frage konnte nicht geladen werden.',
            'submitAnswer',
            err
          )
        }
      });
    } else {
      // save answers
      this.previousAnswers[this.question!.id] = payload;
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
    if (this.currentQuestionIndex >= this.currentQuestions.length) {
      this.isFinished = true;
    }
  }

  navigateExam(direction: 'next' | 'prev') {
    this.submit();
    if (direction === 'next' && this.currentQuestionIndex < this.currentQuestions.length - 1) {
      this.currentQuestionIndex++;
      this.loadQuestion(this.currentQuestionIndex);
    } else if (direction === 'prev' && this.currentQuestionIndex > 0) {
      this.currentQuestionIndex--;
      this.loadQuestion(this.currentQuestionIndex);
    } else if (direction === 'next' && this.currentQuestionIndex === this.currentQuestions.length - 1) {
      this.finish();
    }
  }

  navigateToQuestion(id: number) {
    this.submit();
    this.currentQuestionIndex = id;
    this.loadQuestion(this.currentQuestionIndex);
  }

  timerTickDown() {
    this.timeLeft -= 1;
    if (this.timeLeft <= 0) {
      this.isFinished = true;
      this.finish()
    } else {
      setTimeout(() => this.timerTickDown(), 1000);
    }
  }


  loadNextQuestion(): void {
    if (!this.isFinished) {
      this.answerResult = null;
      this.submitted = false;
      this.loadQuestion(this.currentQuestionIndex);
    }
  }

  get currentQuestions(): { id: number; question: Question; selectedAnswers?: any; freetextAnswer?: string }[] | number[] {
    return this.mode === 'review'
      ? this.exam?.questions ?? []
      : this.questionIdList;
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

    if (this.mode !== 'exam') {
      this.location.back()
    } else {
      let allAnswers = Object.values(this.previousAnswers);
      this.finishedExam.emit(allAnswers);
    }
  }

  createExamCopy(id: number) {
    this.examService.createExamCopy(id).subscribe(exam => {
      console.log(exam)
      let settings = exam;
      let target = '/trainer/practice/setup-exam/exam';

      if (this.router.url.startsWith(target)) {
        // navigate to dummy route, then back (because if on same page angular does not automatically reload it via the navigate function)
        this.router.navigateByUrl('/', { skipLocationChange: true })
          .then(() =>
            this.router.navigate([target], {
              state: { settings, examCreated: true }
            })
          );
      } else {
        this.router.navigate([target], {
          state: { settings, examCreated: true }
        });
      }

    })
  }

  loadVotes(solutionId: number) {
    this.solutionService.getVoteCount(solutionId).subscribe({
      next: (res) => this.voteCounts[solutionId] = res.score,
      error: (err) => console.error(err)
    });
  }

  upvoteSolution(solutionId: number) {
    this.solutionService.upvote(solutionId, this.userId).subscribe({
      next: () => this.loadVotes(solutionId),
      error: (err) => console.error(err)
    });
  }

  downvoteSolution(solutionId: number) {
    this.solutionService.downvote(solutionId, this.userId).subscribe({
      next: () => this.loadVotes(solutionId),
      error: (err) => console.error(err)
    });
  }
  get solutions(): Solution[] {
    return this.question?.solutions ?? [];
  }

  get hasMultipleSolutions(): boolean {
    return this.solutions.length > 1;
  }


  protected readonly Math = Math;

  handleError(msg: string, action: typeof this.lastFailedAction, err: any) {
    console.error(err);
    this.errorMessage = msg;
    this.lastFailedAction = action;
  }

  clearError() {
    this.errorMessage = null;
    this.lastFailedAction = null;
  }
}

