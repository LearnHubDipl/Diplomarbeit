import {Component, HostListener, inject, Input, OnInit, ViewChild} from '@angular/core';
import {QuestionRunnerComponent} from '../question-runner/question-runner.component';
import {CheckAnswerRequest} from '../../../../shared/src/lib/interfaces/answer';
import {CreatedExamResponse, ExamService} from '../../../../shared/src/lib/services/exam.service';
import {Exam} from '../../../../shared/src/lib/interfaces/exam';
import {ActivatedRoute} from '@angular/router';
import {Location, NgIf} from '@angular/common';
import {query} from '@angular/animations';

@Component({
  selector: 'lib-exam',
  imports: [
    QuestionRunnerComponent,
    NgIf
  ],
  templateUrl: './exam.component.html',
  styleUrl: './exam.component.css'
})
export class ExamComponent implements OnInit{
  @ViewChild(QuestionRunnerComponent) runner!: QuestionRunnerComponent;
  answers: CheckAnswerRequest[] = [];
  examService: ExamService = inject(ExamService);
  activatedRoute: ActivatedRoute = inject(ActivatedRoute);
  location: Location = inject(Location)

  errorMessage: string | null = null;

  exam: CreatedExamResponse | null = null;
  submittedExam?: Exam;
  questionIds : number[] = [];
  isLoading = true;

  examSubmitted = false;


  ngOnInit() {
    // Grab the settings passed via router state
    const settings = history.state.settings;
    let examCreated = history.state.examCreated;
    if (!settings) {
      console.error('No exam settings provided!');
      return;
    }

    if (!examCreated) {
      this.examService.createExam(settings).subscribe({
        next: (createdExam) => {
          this.exam = createdExam;
          this.questionIds = createdExam.questions.map(q => q.id);
          this.isLoading = false;
          this.clearError()
        },
        error: (err) => {
          this.handleError("Prüfung konnte nicht erstellt werden.", err)
          this.isLoading = false;
        }
      });
    } else {
      this.exam = settings;
      this.questionIds = this.exam!.questions.map(q => q.id);
      this.initializeEmptyAnswers();
      this.isLoading = false;
    }
  }

  private initializeEmptyAnswers() {
    this.answers = this.questionIds.map(id => ({
      questionId: id,
      selectedAnswerIds: [],
      freeTextAnswer: null
    }));
  }

  @HostListener('window:beforeunload', ['$event'])
  handleBeforeUnload($event: BeforeUnloadEvent) {
    if (this.exam && !this.examSubmitted) {
      $event.preventDefault();
      this.runner.finish()
      $event.returnValue = true;
    }
  }


  storeAnswer(answer: CheckAnswerRequest) {
    const index = this.answers.findIndex(a => a.questionId === answer.questionId);
    if (index !== -1) {
      this.answers[index] = answer;
    } else {
      this.answers.push(answer);
    }
  }

  submitExam(answers: CheckAnswerRequest[]) {
    if (!this.exam) return;

    this.examService.submitExam({
      examId: this.exam.examId,
      answers: answers
    }).subscribe(result => {
      this.submittedExam = result;
      this.examSubmitted = true;

      console.log('Exam submitted:', result);
    });
  }

  navigateBack() {
    this.location.back()
  }

  protected readonly query = query;
  protected readonly Math = Math;

  handleError(msg: string, err: any) {
    console.error(err);
    this.errorMessage = msg;
  }

  clearError() {
    this.errorMessage = null;
  }
}
