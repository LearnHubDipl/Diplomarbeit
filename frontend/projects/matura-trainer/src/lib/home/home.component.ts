import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { StatsService } from '../../../../shared/src/lib/services/stats.service';
import { NgForOf, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { QuestionService } from '../../../../shared/src/lib/services/question.service';

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
  imports: [NgForOf, FormsModule, NgIf],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css', '../styles/shared-styles.css']
})
export class HomeComponent implements OnInit {
  private router = inject(Router);
  private activatedRoute = inject(ActivatedRoute);
  private statsService = inject(StatsService);
  private questionService = inject(QuestionService);

  userId = 1;
  topicPools: { id: number; name: string }[] = [];
  selectedTopicPoolIds: Set<number> = new Set<number>();

  progressLevels: ProgressLevel[] = [];
  hasQuestions = false;
  showNoQuestionsWarning = false;

  ngOnInit(): void {
    this.loadTopicPools();
    this.checkQuestions();
    this.loadProgressData();
  }

  loadTopicPools(): void {
    this.statsService.getTopicPools(this.userId).subscribe({
      next: pools => (this.topicPools = pools),
      error: err => console.error('Fehler beim Laden der Topic Pools:', err)
    });
  }

  toggleTopicPool(poolId: number): void {
    if (this.selectedTopicPoolIds.has(poolId)) {
      this.selectedTopicPoolIds.delete(poolId);
    } else {
      this.selectedTopicPoolIds.add(poolId);
    }

    this.showNoQuestionsWarning = false;
    this.loadProgressData();
    this.checkQuestions();
  }

  loadProgressData(): void {
    const ids = Array.from(this.selectedTopicPoolIds);
    this.statsService.getProgressOverview(this.userId, ids.length > 0 ? ids : undefined).subscribe({
      next: (overview: ProgressOverviewDto) => (this.progressLevels = overview.levels),
      error: err => {
        console.error('Fehler beim Laden der Progress-Daten:', err);
        this.progressLevels = [];
      }
    });
  }

  checkQuestions(): void {
    const ids = Array.from(this.selectedTopicPoolIds);
    this.questionService.getQuestionsForPractice(this.userId, ids).subscribe({
      next: (questionIds: number[]) => {
        this.hasQuestions = questionIds.length > 0;
      },
      error: () => {
        this.hasQuestions = false;
      }
    });
  }

  startPractice(): void {
    if (!this.hasQuestions) {
      this.showNoQuestionsWarning = true;
      return;
    }

    const ids = Array.from(this.selectedTopicPoolIds);
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
}
