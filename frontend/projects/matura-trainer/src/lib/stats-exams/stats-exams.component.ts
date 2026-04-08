import {Component, inject, Input, OnInit} from '@angular/core';
import {ExamDto, ExamQuestionDetailDto, ExamQuestionSlimDto, StatsService} from '../../../../shared/src/lib/services/stats.service';
import {DatePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import {Exam} from '../../../../shared/src/lib/interfaces/exam';
import {QuestionRunnerComponent} from '../question-runner/question-runner.component';
import {ExamHistoryDto} from '../../../../shared/src/lib/interfaces/ExamHistoryDto';
import { ChangeDetectorRef } from '@angular/core';
import {UserInitializationService} from '../../../../shared/src/lib/services/user-initialization.service';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'lib-stats-exams',
  imports: [
    DatePipe,
    NgForOf,
    NgClass,
    NgIf,
    QuestionRunnerComponent,
    RouterLink
  ],
  templateUrl: './stats-exams.component.html',
  styleUrls:[
    '../styles/shared-styles.css',
    './stats-exams.component.css',
    '../question-runner/question-runner.component.css'
  ]
})

export class StatsExamsComponent implements OnInit {
  exams: ExamHistoryDto[] = [];
  selectedExamDetails?: ExamDto;
  expandedExamId: number | undefined = undefined;
  userid = -1;
  userService: UserInitializationService = inject(UserInitializationService);

  constructor(private service: StatsService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.userid = this.userService.getCurrentUser()!.id;
    this.loadExams();
  }

  loadExams() {
    this.service.getExamsByUser(this.userid).subscribe({
      next: (data) => {
        this.exams = data;
      },
      error: (err) => console.error('Fehler beim Laden der Prüfungen:', err)
    });
  }

  toggleExam(id: number) {
    if (this.expandedExamId === id) {
      this.expandedExamId = undefined;
      this.selectedExamDetails = undefined;
    } else {
      this.selectedExamDetails = undefined;
      this.expandedExamId = id;

      this.service.getExamDetails(id).subscribe({
        next: (data) => {
          this.selectedExamDetails = data;
          this.cdr.detectChanges();
        }
      });
    }
  }

  protected readonly Math = Math;

  formatDuration(exam: any) {
    if (!exam.startedAt || !exam.finishedAt) return '';
    const started = new Date(exam.startedAt);
    const finished = new Date(exam.finishedAt);
    const diffMs = finished.getTime() - started.getTime();
    const diffMinutes = Math.floor(diffMs / (1000 * 60));
    return diffMinutes < 60 ? `${diffMinutes} min` : `${Math.floor(diffMinutes / 60)}h ${diffMinutes % 60}min`;
  }
}
