import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import { StatsService } from '../../../../shared/src/lib/services/stats.service';
import { NgForOf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {QuestionService} from '../../../../shared/src/lib/services/question.service';

// DTOs vom Backend
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
  imports: [
    NgForOf,
    FormsModule
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css', '../styles/shared-styles.css']
})
export class HomeComponent implements OnInit {
  router: Router = inject(Router);
  activatedRoute: ActivatedRoute = inject(ActivatedRoute);

  userId = 1;
  topicPools: { id: number; name: string }[] = [];
  selectedTopicPoolId: number | null = null;
  progressLevels: ProgressLevel[] = [];
  hasQuestions = false;

  constructor(private statsService: StatsService, private questionService: QuestionService) {}

  ngOnInit(): void {
    this.loadTopicPools();
    this.selectedTopicPoolId = null;
    this.loadProgressData();

    // 👇 Direkt beim Start prüfen, ob Fragen vorhanden sind (alle Fragen)
    this.checkQuestions();
  }

  loadTopicPools(): void {
    this.statsService.getTopicPools(this.userId).subscribe({
      next: pools => (this.topicPools = pools),
      error: err => {
        console.error('Fehler beim Laden der Topic Pools:', err);
        this.topicPools = [];
      }
    });
  }

  onTopicPoolChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    const value = Number(select.value);
    this.selectedTopicPoolId = value === 0 ? null : value;
    this.loadProgressData();

    // 👇 Nach Wechsel des Pools wieder prüfen
    this.checkQuestions();
  }

  loadProgressData(): void {
    this.statsService.getProgressOverview(this.userId, this.selectedTopicPoolId ?? undefined).subscribe({
      next: (overview: ProgressOverviewDto) => {
        this.progressLevels = overview.levels;
      },
      error: err => {
        console.error('Fehler beim Laden der Daten:', err);
        this.progressLevels = [];
      }
    });
  }

  checkQuestions(): void {
    this.questionService.getQuestionsForPractice(this.userId, this.selectedTopicPoolId ?? undefined).subscribe({
      next: (questionIds: number[]) => {
        this.hasQuestions = questionIds.length > 0;
      },
      error: err => {
        console.error('Fehler beim Prüfen der Fragen:', err);
        this.hasQuestions = false;
      }
    });
  }

  startPractice(): void {
    if (!this.hasQuestions) return; // Sicherheitscheck

    this.questionService.getQuestionsForPractice(this.userId, this.selectedTopicPoolId ?? undefined).subscribe({
      next: (questionIds: number[]) => {
        this.router.navigate(['quiz'], {
          relativeTo: this.activatedRoute,
          state: { questionIds }
        });
      },
      error: err => {
        console.error('Fehler beim Laden der Daten:', err);
      }
    });
  }
}
