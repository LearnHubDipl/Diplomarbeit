import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { StatsService } from '../../../../shared/src/lib/services/stats.service';
import { CommonModule } from '@angular/common';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { QuestionService } from '../../../../shared/src/lib/services/question.service';
import { QuestionPoolService } from '../../../../shared/src/lib/services/question-pool.service';

// Angular Material Imports
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';

// Interfaces
import { Subject } from '../../../../shared/src/lib/interfaces/subject';
import {UserInitializationService} from '../../../../shared/src/lib/services/user-initialization.service';

export interface ProgressEntry {
  label: string;
  color: string;
  value: number;
  description: string;
}

export interface ProgressLevel {
  title: string;
  entries: ProgressEntry[];
}

export interface ProgressOverviewDto {
  levels: ProgressLevel[];
}

@Component({
  selector: 'lib-home',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatCheckboxModule
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css', '../styles/shared-styles.css']
})
export class HomeComponent implements OnInit {
  private router = inject(Router);
  private activatedRoute = inject(ActivatedRoute);
  private statsService = inject(StatsService);
  private questionService = inject(QuestionService);
  private questionPoolService = inject(QuestionPoolService);
  private userService: UserInitializationService = inject(UserInitializationService);

  userId = -1;
  subjects: Subject[] = [];

  topicPoolControl = new FormControl<number[]>([]);

  progressLevels: ProgressLevel[] = [];
  hasQuestions = false;

  ngOnInit(): void {
    this.userId = this.userService.getCurrentUser()!.id;

    this.loadSubjects();
    this.loadProgressData();

    this.topicPoolControl.valueChanges.subscribe(selectedIds => {
      const ids = selectedIds || [];
      this.checkQuestions(ids);
    });

    this.checkQuestions([]);
  }

  loadSubjects(): void {
    this.questionPoolService.getSubjectsForUser(this.userId).subscribe({
      next: subjects => (this.subjects = subjects),
      error: err => console.error('Fehler beim Laden der Fächer:', err)
    });
  }

  loadProgressData(): void {
    this.statsService.getProgressOverview(this.userId, undefined).subscribe({
      next: (overview: ProgressOverviewDto) => {
        this.progressLevels = overview.levels;
      },
      error: err => {
        console.error('Fehler beim Laden der Progress-Daten:', err);
        this.progressLevels = [];
      }
    });
  }

  checkQuestions(ids: number[]): void {
    this.questionService.getQuestionsForPractice(this.userId, ids).subscribe({
      next: (questionIds: number[]) => {
        this.hasQuestions = questionIds.length > 0;
      },
      error: () => (this.hasQuestions = false)
    });
  }

  isSubjectSelected(subject: Subject): boolean {
    const selected = this.topicPoolControl.value || [];
    if (!subject.topicPools.length) return false;
    return subject.topicPools.every(pool => selected.includes(pool.id));
  }

  toggleSubject(subject: Subject, checked: boolean): void {
    let selected = [...(this.topicPoolControl.value || [])];
    if (checked) {
      subject.topicPools.forEach(pool => {
        if (!selected.includes(pool.id)) selected.push(pool.id);
      });
    } else {
      selected = selected.filter(id => !subject.topicPools.some(pool => pool.id === id));
    }
    this.topicPoolControl.setValue(selected);
  }

  getSelectedSubjectNames(): string[] {
    const selectedIds = this.topicPoolControl.value || [];
    return this.subjects
      .filter(subject => subject.topicPools.some(pool => selectedIds.includes(pool.id)))
      .map(subject => subject.name);
  }

  startPractice(): void {
    const ids = this.topicPoolControl.value || [];
    this.questionService.getQuestionsForPractice(this.userId, ids.length > 0 ? ids : undefined).subscribe({
      next: (questionIds: number[]) => {
        this.router.navigate(['quiz'], {
          relativeTo: this.activatedRoute,
          state: { questionIds }
        });
      },
      error: err => console.error('Fehler beim Starten des Quiz:', err)
    });
  }
  trackByTitle(index: number, item: any) {
    return item.title;
  }
}
