import { Component, OnInit } from '@angular/core';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { NgForOf, NgStyle } from '@angular/common';
import { StatsService } from '../stats.service';
import { Chart, Plugin } from 'chart.js';
import { CenterTextPlugin } from '../plugin/chart-text.plugin';


@Component({
  selector: 'lib-stats-home',
  imports: [
    NgChartsModule,
    NgForOf,
    NgStyle
  ],
  templateUrl: './stats-home.component.html',
  styleUrls: [
    '../styles/shared-styles.css',
    './stats-home.component.css'
  ]
})
export class StatsHomeComponent implements OnInit {

  chartPlugins: Plugin[] = [CenterTextPlugin];

  public doughnutChartLabels: string[] = [
    'falsch',
    'ausreichend gelernt',
    '2x richtig beantwortet',
    '1x richtig beantwortet',
    'nicht beantwortet'
  ];

  public doughnutChartData: ChartData<'doughnut'> = {
    labels: this.doughnutChartLabels,
    datasets: [
      {
        data: [],
        backgroundColor: ['#FE8B8B', '#309F22', '#3DD32B', '#B7F0B0', '#FFEAA4']
      }
    ]
  };

  public doughnutChartType: ChartType = 'doughnut';

  public chartOptions: ChartConfiguration['options'] = {
    plugins: {
      legend: {
        display: false
      }
    }
  };

  public legendData: { label: string, value: number, color: string }[] = [];

  streak = 0;
  userId = 1; //statisch

  constructor(private statsService: StatsService) {}

  ngOnInit(): void {
    // Streak laden
    this.statsService.getStreak(this.userId).subscribe({
      next: (s) => this.streak = s,
      error: () => this.streak = 0
    });

    this.statsService.getAllEntries(this.userId).subscribe(data => {

      //console.log(data);

      let incorrect = 0;
      let sufficient = 0;
      let correctTwice = 0;
      let correctOnce = 0;
      let unanswered = 0;

      for (const entry of data) {
        if (entry.lastAnsweredCorrectly === null)  {
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

      const labels = this.doughnutChartLabels;
      const colors = ['#FE8B8B', '#309F22', '#3DD32B', '#B7F0B0', '#FFEAA4'];

      this.doughnutChartData = {
        labels: labels,
        datasets: [{
          data: rawData,
          backgroundColor: colors
        }]
      };
      this.legendData = rawData.map((value, index) => ({
        label: labels[index],
        value: total > 0 ? Math.round((value / total) * 100) : 0,
        color: colors[index]
      }));
    });
  }
}
