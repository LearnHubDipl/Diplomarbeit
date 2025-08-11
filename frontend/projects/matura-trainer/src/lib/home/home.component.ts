import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { StatsService } from '../stats.service';
import { NgForOf } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'lib-home',
  imports: [
    RouterLink,
    NgForOf,
    FormsModule
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css', '../styles/shared-styles.css']
})
export class HomeComponent implements OnInit {
  userId = 1; // statisch

  topicPools: { id: number; name: string }[] = [];
  selectedTopicPoolId: number | null = null;

  progressLevels: any[] = [];

  constructor(private statsService: StatsService) { }

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
    if (this.selectedTopicPoolId === null) {
      // Alle Fragen laden
      this.statsService.getAllEntries(this.userId).subscribe(entries => {
        this.processEntries(entries);
      }, err => {
        console.error('Fehler beim Laden aller Fragen:', err);
        this.progressLevels = [];
      });
    } else {
      this.statsService.getEntriesByTopicPool(this.userId, this.selectedTopicPoolId).subscribe(entries => {
        this.processEntries(entries);
      }, err => {
        console.error('Fehler beim Laden der TopicPool-Einträge:', err);
        this.progressLevels = [];
      });
    }
  }

  private processEntries(entries: any[]): void {
    let incorrect = 0;
    let sufficient = 0;
    let correctTwice = 0;
    let correctOnce = 0;
    let unanswered = 0;

    for (const entry of entries) {
      if (entry.lastAnsweredCorrectly === null ) {
        unanswered++;
      } else if (entry.correctCount === 0) {
        incorrect++;
      } else if (entry.correctCount === 1) {
        correctOnce++;
      } else if (entry.correctCount === 2) {
        correctTwice++;
      } else if (entry.correctCount >= 3) {
        sufficient++;
      }
    }

    this.progressLevels = [
      {
        title: 'Startlevel',
        entries: [
          { label: 'Falsch', color: '#FE8B8B', value: incorrect, description: `${incorrect} Fragen falsch beantwortet` },
          { label: 'Nicht beantwortet', color: '#FFEAA4', value: unanswered, description: `${unanswered} Fragen noch nicht beantwortet` }
        ]
      },
      {
        title: 'Basislevel',
        entries: [
          { label: '1x richtig', color: '#B7F0B0', value: correctOnce, description: `${correctOnce} Fragen einmal richtig beantwortet` }
        ]
      },
      {
        title: 'Trainingslevel',
        entries: [
          { label: '2x richtig', color: '#3DD32B', value: correctTwice, description: `${correctTwice} Fragen zweimal richtig beantwortet` }
        ]
      },
      {
        title: 'Highscorelevel',
        entries: [
          { label: 'Ausreichend geübt', color: '#309F22', value: sufficient, description: `${sufficient} Fragen ausreichend geübt` }
        ]
      }
    ];
  }
}
