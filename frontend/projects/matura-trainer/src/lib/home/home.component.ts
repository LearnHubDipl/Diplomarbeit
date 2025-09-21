import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import { StatsService } from '../../../../shared/src/lib/services/stats.service';
import { NgForOf } from '@angular/common';
import { FormsModule } from '@angular/forms';

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

  userId = 1; // statisch
  topicPools: { id: number; name: string }[] = [];
  selectedTopicPoolId: number | null = null;
  progressLevels: ProgressLevel[] = [];

  constructor(private statsService: StatsService) {}

  ngOnInit(): void {
    this.loadTopicPools();
    this.selectedTopicPoolId = null;
    this.loadProgressData();
  }

  loadTopicPools(): void {
    this.statsService.getTopicPools(this.userId).subscribe({
      next: pools => {
        this.topicPools = pools;
      },
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
  }

  loadProgressData(): void {
    this.statsService.getProgressOverview(this.userId, this.selectedTopicPoolId ?? undefined)
      .subscribe({
        next: (overview: ProgressOverviewDto) => {
          console.log('Progress Overview:', overview); // 👈 Check hier
          this.progressLevels = overview.levels;
        },
        error: err => {
          console.error('Fehler beim Laden der Progress-Daten:', err);
          this.progressLevels = [];
        }
      });
  }


  startPractice() {
    this.router.navigate(
      ['quiz'],
      { relativeTo: this.activatedRoute, state: { questionIds: [1, 2, 3] } } // pass IDs via state
    );
  }
}
