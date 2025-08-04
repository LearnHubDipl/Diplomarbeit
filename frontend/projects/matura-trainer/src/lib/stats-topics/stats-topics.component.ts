import { Component, OnInit, ViewChild } from '@angular/core';
import { BaseChartDirective, NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { StatsService } from '../stats.service';
import { Chart, Plugin } from 'chart.js';
import { CenterTextPlugin } from '../plugin/chart-text.plugin';
import { FormsModule } from '@angular/forms';
import { NgClass, NgForOf, NgStyle } from '@angular/common';

export interface TopicPool {
  id: number;
  name: string;
}

@Component({
  selector: 'lib-stats-topics',
  imports: [
    FormsModule,
    NgForOf,
    NgChartsModule,
    NgStyle,
    NgClass
  ],
  templateUrl: './stats-topics.component.html',
  styleUrls: ['./stats-topics.component.css', '../styles/shared-styles.css']
})
export class StatsTopicsComponent implements OnInit {

  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

  isDropdownOpen = false;

  chartPlugins: Plugin[] = [CenterTextPlugin];

  topicPools: TopicPool[] = [];
  selectedTopicPoolId = 0; // statisch

  public doughnutChartLabels: string[] = [
    'falsch',
    'ausreichend gelernt',
    '2x richtig beantwortet',
    '1x richtig beantwortet',
    'nicht beantwortet'
  ];

  doughnutChartData: ChartData<'doughnut'> = {
    labels: this.doughnutChartLabels,
    datasets: [{
      data: [],
      backgroundColor: ['#FE8B8B', '#309F22', '#3DD32B', '#B7F0B0', '#FFEAA4']
    }]
  };

  doughnutChartType: ChartType = 'doughnut';

  chartOptions: ChartConfiguration['options'] = {
    plugins: {
      legend: {
        display: false
      }
    }
  };

  legendData: { label: string; value: number; color: string }[] = [];

  userId = 1;

  constructor(private statsService: StatsService) {}

  ngOnInit(): void {
    this.statsService.getTopicPools(this.userId).subscribe({
      next: (pools) => {
        console.log('Pools geladen:', pools);
        this.topicPools = pools;
        if (this.topicPools.length > 0) {
          this.selectedTopicPoolId = this.topicPools[0].id;
          this.loadChartData();
        }
      },
      error: (err) => {
        console.error('Fehler beim Laden der TopicPools:', err);
        this.topicPools = [];
        this.selectedTopicPoolId = 0;
      }
    });

  }

  onTopicPoolChange(value: any): void {
    this.selectedTopicPoolId = Number(value);
    this.loadChartData();
  }

  loadChartData(): void {
    if (!this.selectedTopicPoolId) {

      this.doughnutChartData.datasets[0].data = [];
      this.legendData = [];
      this.chart?.update();
      return;
    }

    this.statsService.getEntriesByTopicPool(this.userId, this.selectedTopicPoolId).subscribe(entries => {
      const safeEntries = entries ?? [];

      let incorrect = 0;
      let sufficient = 0;
      let correctTwice = 0;
      let correctOnce = 0;
      let unanswered = 0;

      for (const entry of safeEntries) {
        if (entry.lastAnsweredCorrectly == null ) {
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

      const rawData = [incorrect, sufficient, correctTwice, correctOnce, unanswered];
      const total = rawData.reduce((a, b) => a + b, 0);
      const colors = ['#FE8B8B', '#309F22', '#3DD32B', '#B7F0B0', '#FFEAA4'];

      this.doughnutChartData = {
        labels: this.doughnutChartLabels,
        datasets: [{
          data: rawData,
          backgroundColor: colors
        }]
      };

      this.legendData = rawData.map((value, index) => ({
        label: this.doughnutChartLabels[index],
        value: total > 0 ? Math.round((value / total) * 100) : 0,
        color: colors[index]
      }));

      this.chart?.update();
    });

  }
}
