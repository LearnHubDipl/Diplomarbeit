import {Component, inject, OnInit} from '@angular/core';
import { ChartData, ChartType, ChartConfiguration, Plugin } from 'chart.js';
import { StatsLegendEntry, StatsOverviewDto, StatsService } from '../../../../shared/src/lib/services/stats.service';
import { CenterTextPlugin } from '../plugin/chart-text.plugin';
import {NgChartsModule} from 'ng2-charts';
import {NgForOf, NgStyle} from '@angular/common';
import {UserInitializationService} from '../../../../shared/src/lib/services/user-initialization.service';

@Component({
  selector: 'lib-stats-home',
  templateUrl: './stats-home.component.html',
  imports: [
    NgChartsModule,
    NgForOf,
    NgStyle
  ],
  styleUrls: [
    '../styles/shared-styles.css',
    './stats-home.component.css'
  ]
})
export class StatsHomeComponent implements OnInit {

  userService: UserInitializationService = inject(UserInitializationService);

  chartPlugins: Plugin[] = [CenterTextPlugin];

  public doughnutChartLabels: string[] = [];

  public doughnutChartData: ChartData<'doughnut'> = {
    labels: [],
    datasets: [{ data: [], backgroundColor: [] }]
  };

  public doughnutChartType: ChartType = 'doughnut';

  public chartOptions: ChartConfiguration['options'] = {
    plugins: {
      legend: { display: false },
      centerText: { text: '' }
    }
  };

  public legendData: StatsLegendEntry[] = [];
  streak = 0;
  userId = this.userService.getCurrentUser()!.id;

  userAverage: number | null = null;
  examCount: number = 0;

  constructor(private statsService: StatsService) {}

  ngOnInit(): void {
    this.statsService.getStatsOverview(this.userId).subscribe((data: StatsOverviewDto) => {

      this.doughnutChartLabels = data.legend.map(entry => entry.label);
      const rawData = data.legend.map(entry => entry.value);
      const colors = data.legend.map(entry => entry.color);

      this.doughnutChartData = {
        labels: this.doughnutChartLabels,
        datasets: [{ data: rawData, backgroundColor: colors }]
      };

      this.legendData = data.legend;

      // CenterText zeigt offene Fragen
      this.chartOptions!.plugins!.centerText!.text =  `${data.unanswered} offene Fragen`;
    });

    this.statsService.getUserExamAverage(this.userId).subscribe({
      next: dto => {
        console.log('Average DTO', dto);
        this.userAverage = dto.average;
        this.examCount = dto.count;
      },
      error: err => console.error('Fehler beim Laden des Averages:', err)
    });
  }
}
