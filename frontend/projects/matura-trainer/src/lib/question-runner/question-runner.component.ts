import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { Question } from '../../../../shared/src/lib/interfaces/question';
import { QuestionService } from '../../../../shared/src/lib/services/question.service';
import { NgClass, NgForOf, NgIf, NgStyle, Location } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CheckAnswerRequest } from '../../../../shared/src/lib/interfaces/answer';
import { AnswerService } from '../../../../shared/src/lib/services/answer.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Exam } from '../../../../shared/src/lib/interfaces/exam';
import { QuestionPoolService } from '../../../../shared/src/lib/services/question-pool.service';
import { SolutionService } from '../../../../shared/src/lib/services/solution.service';
import { Solution } from '../../../../shared/src/lib/interfaces/solution';
import { ExamService } from '../../../../shared/src/lib/services/exam.service';
import { CreateSolutionComponent } from '../create-solution/create-solution.component';

@Component({
  selector: 'lib-question-runner',
  standalone: true,
  imports: [
    NgForOf,
    NgStyle,
    ReactiveFormsModule,
    NgClass,
    NgIf,
    CreateSolutionComponent
  ],
  templateUrl: './question-runner.component.html',
  styleUrls: [
    './question-runner.component.css',
    '../styles/shared-styles.css'
  ]
})
export class QuestionRunnerComponent implements OnInit {
  @Input() mode: 'practice' | 'exam' | 'review' = 'practice';
  @Input() exam?: Exam;
  @Input() timeLimit?: number = 0;
  @Output() answered = new EventEmitter<CheckAnswerRequest>();
  @Output() finishedExam = new EventEmitter<CheckAnswerRequest[]>();
  @Input() questionIdList: number[] = [];
  @Input() navigateBack: boolean = true;

  errorMessage: string | null = null;
  lastFailedAction: 'loadQuestion' | 'submitAnswer' | 'vote' | null = null;
  loading: boolean = false;

  currentQuestionIndex: number = 0;
  isFinished = false;
  showSolutionEditor = false;
  submitted = false;
  showAllSolutions = false;
  userId = 1;

  timeLeft: number = 0;
  question: Question | undefined;
  voteCounts: { [solutionId: number]: { up: number, down: number } } = {};
  previousAnswers: { [questionId: number]: CheckAnswerRequest } = {};
  answerResult: {
    correct: boolean;
    correctAnswerIds: number[] | null;
    correctFreeTextAnswers: string[] | null;
  } | null = null;

  form: FormGroup;

  // Injections
  router = inject(Router);
  questionService = inject(QuestionService);
  answerService = inject(AnswerService);
  questionPoolService = inject(QuestionPoolService);
  solutionService = inject(SolutionService);
  examService = inject(ExamService);
  fb = inject(FormBuilder);
  route = inject(ActivatedRoute);
  location = inject(Location);

  constructor() {
    this.form = this.fb.group({
      answers: this.fb.array([]),
      freeTextAnswer: ['']
    });
  }

  ngOnInit() {
    let state = history.state;
    if (this.mode !== 'review') {
      if (state?.questionIds) {
        this.questionIdList = state.questionIds;
      }
      if (this.questionIdList.length > 0) {
        this.loadQuestion(this.currentQuestionIndex);
      }
    } else {
      this.loadQuestion(this.currentQuestionIndex);
    }

    if (this.mode === 'exam') {
      this.timeLeft = this.timeLimit! * 60;
      this.timerTickDown();
    } else if (this.mode === 'review' && this.exam) {
      let started = new Date(this.exam.startedAt);
      let finished = new Date(this.exam.finishedAt);
      let elapsed = (finished.getTime() - started.getTime()) / 1000;
      this.timeLeft = (this.exam.timeLimit * 60) - elapsed;
    }
  }

  loadQuestion(index: number) {
    if (this.mode === 'review' && this.exam) {
      let reviewed = this.exam.questions[index];
      if (reviewed) {
        this.question = reviewed.question;
        this.setupFormForReview(reviewed);
        this.submitted = true;
        return;
      }
    }

    let id = this.questionIdList[index];
    if (!id) return;

    this.loading = true;
    this.clearError();

    this.questionService.getQuestionById(id).subscribe({
      next: q => {
        this.loading = false;
        this.question = q;
        let previous = this.previousAnswers[q.id];

        if (q.type === 'MULTIPLE_CHOICE') {
          let answerControls = this.fb.array(
            q.answers.map((a) => previous?.selectedAnswerIds?.includes(a.id) ?? false)
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
        this.handleError('Die Frage konnte nicht geladen werden.', 'loadQuestion', err);
      }
    });
  }

  private setupFormForReview(reviewed: any) {
    if (this.question?.type === 'MULTIPLE_CHOICE') {
      let answerControls = this.fb.array(
        this.question.answers.map(a => reviewed.selectedAnswers?.some((sa: any) => sa.id === a.id) ?? false)
      );
      this.form.setControl('answers', answerControls);
    } else if (this.question?.type === 'FREETEXT') {
      this.form.get('freeTextAnswer')?.setValue(reviewed.freeTextAnswer ?? '');
    }
    this.answerResult = {
      correct: reviewed.isCorrect,
      correctAnswerIds: reviewed.correctAnswerIds ?? null,
      correctFreeTextAnswers: reviewed.correctFreeTextAnswers ?? null
    };
    this.lockInputs();
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
    let payload = this.buildPayLoad();

    if (this.mode === 'practice') {
      this.answerService.checkAnswers(payload).subscribe({
        next: result => {
          this.answerResult = {
            correct: result.correct,
            correctAnswerIds: result.correctAnswerIds ?? null,
            correctFreeTextAnswers: result.correctFreeTextAnswers ?? null
          };
          this.lockInputs();
          this.submitted = true;

          if (result.correct) {
            this.questionPoolService.increaseCorrectCount(this.question!.id, this.userId)
              .subscribe(() => console.log('CorrectCount erhöht'));
          }
          this.advance();
        },
        error: err => this.handleError('Prüfung fehlgeschlagen.', 'submitAnswer', err)
      });
    } else {
      this.previousAnswers[this.question.id] = payload;
    }
  }

  buildPayLoad(): CheckAnswerRequest {
    if (!this.question) throw new Error("No Question selected");
    if (this.question.type === 'MULTIPLE_CHOICE') {
      const selectedAnswerIds = this.answersArray.controls
        .map((ctrl, index) => ctrl.value ? this.question!.answers[index].id : null)
        .filter((id): id is number => id !== null);
      return { questionId: this.question.id, selectedAnswerIds, freeTextAnswer: null };
    } else {
      return {
        questionId: this.question.id,
        selectedAnswerIds: [],
        freeTextAnswer: this.form.get('freeTextAnswer')?.value.trim() ?? null
      };
    }
  }

  private lockInputs() {
    this.form.disable();
  }

  private advance() {
    if (this.currentQuestionIndex >= this.currentQuestions.length - 1) {
      this.isFinished = true;
    }
  }

  loadNextQuestion(): void {
    if (this.currentQuestionIndex < this.currentQuestions.length - 1) {
      this.currentQuestionIndex++;
      this.answerResult = null;
      this.submitted = false;
      this.loadQuestion(this.currentQuestionIndex);
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
    if (this.isFinished) return;
    this.timeLeft -= 1;
    if (this.timeLeft <= 0) {
      this.isFinished = true;
      this.finish();
    } else {
      setTimeout(() => this.timerTickDown(), 1000);
    }
  }

  get currentQuestions(): any[] {
    return this.mode === 'review' ? this.exam?.questions ?? [] : this.questionIdList;
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
    this.submit();
    if (this.mode !== 'exam') {
      this.location.back();
    } else {
      let allAnswers = Object.values(this.previousAnswers);
      this.finishedExam.emit(allAnswers);
    }
  }

  createExamCopy(id: number) {
    this.examService.createExamCopy(id).subscribe(exam => {
      let settings = exam;
      let target = '/trainer/practice/setup-exam/exam';
      if (this.router.url.startsWith(target)) {
        this.router.navigateByUrl('/', { skipLocationChange: true })
          .then(() => this.router.navigate([target], { state: { settings, examCreated: true } }));
      } else {
        this.router.navigate([target], { state: { settings, examCreated: true } });
      }
    });
  }

  loadVotes(solutionId: number) {
    this.solutionService.getVoteCount(solutionId).subscribe({
      next: (res) => {
        this.voteCounts[solutionId] = { up: res.upVotes, down: res.downVotes };
      },
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

  openSolutionEditor() {
    this.showSolutionEditor = true;
  }

  closeEditor() {
    this.showSolutionEditor = false;
    if (this.question) {
      this.questionService.getQuestionById(this.question.id).subscribe(q => {
        this.question!.solutions = q.solutions;
      });
    }
  }

  protected readonly Math = Math;

  handleError(msg: string, action: 'loadQuestion' | 'submitAnswer' | 'vote' | null, err: any) {
    console.error(err);
    this.errorMessage = msg;
    this.lastFailedAction = action;
  }

  clearError() {
    this.errorMessage = null;
    this.lastFailedAction = null;
  }
}
