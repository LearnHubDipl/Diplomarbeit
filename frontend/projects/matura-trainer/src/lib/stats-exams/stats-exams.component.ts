import {Component, Input, OnInit} from '@angular/core';
import {ExamDto, ExamQuestionDetailDto, ExamQuestionSlimDto, StatsService} from '../../../../shared/src/lib/services/stats.service';
import {DatePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import {Exam} from '../../../../shared/src/lib/interfaces/exam';
import {QuestionRunnerComponent} from '../question-runner/question-runner.component';

interface ExamViewState {
  exam: ExamDto;
  expanded: boolean;
  currentIndex: number;
}

@Component({
  selector: 'lib-stats-exams',
  imports: [
    DatePipe,
    NgForOf,
    NgClass,
    NgIf,
    QuestionRunnerComponent
  ],
  templateUrl: './stats-exams.component.html',
  styleUrls:[
    '../styles/shared-styles.css',
    './stats-exams.component.css',
    '../question-runner/question-runner.component.css'
  ]
})

export class StatsExamsComponent implements OnInit {
  exams: Exam[] = [];
  expandedExamId?: number;

  constructor(private service: StatsService) {}

  ngOnInit() {
    this.loadExams();
  }

  loadExams() {
    this.service.getAllExams().subscribe({
      next: (data) => {
        this.exams = data;
        // Optional: letzte Prüfung automatisch aufklappen
        if (this.exams.length) this.expandedExamId = this.exams[0].id;
      },
      error: (err) => console.error('Fehler beim Laden der Prüfungen:', err)
    });
  }

  toggleExam(id: number) {
    this.expandedExamId = this.expandedExamId === id ? undefined : id;
  }

  protected readonly Math = Math;
}

