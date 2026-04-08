import {Component, inject, OnInit} from '@angular/core';
import { ChartData, ChartType, ChartConfiguration, Plugin } from 'chart.js';
import { StatsLegendEntry, StatsOverviewDto, StatsService } from '../../../../shared/src/lib/services/stats.service';
import { CenterTextPlugin } from '../plugin/chart-text.plugin';
import {NgChartsModule} from 'ng2-charts';
import {NgForOf, NgIf, NgStyle} from '@angular/common';
import {UserInitializationService} from '../../../../shared/src/lib/services/user-initialization.service';
import {RouterLink} from '@angular/router';
import {StreakService} from '../../../../shared/src/lib/services/streak.service';

@Component({
  selector: 'lib-stats-home',
  templateUrl: './stats-home.component.html',
  imports: [
    NgChartsModule,
    NgForOf,
    NgStyle,
    NgIf,
    RouterLink
  ],
  styleUrls: [
    '../styles/shared-styles.css',
    './stats-home.component.css'
  ]
})
export class StatsHomeComponent implements OnInit {

  userService: UserInitializationService = inject(UserInitializationService);
  streakService: StreakService=inject(StreakService);
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
  private testTimer: any;
  userId = this.userService.getCurrentUser()!.id;

  userAverage: number = 0;
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
        this.userAverage = dto.average ? Math.round(dto.average) : 0;
        this.examCount = dto.count;
      },
      error: () => this.userAverage = 0
    });

    this.loadStreak();
  }

  get hasChartData(): boolean {
    return this.doughnutChartData.datasets[0].data.some(value => value > 0);
  }

  private loadStreak() {
    this.streakService.getStreak(this.userId).subscribe({
      next: res => this.streak = res.streak || 0,
      error: () => this.streak = 0
    });
  }

  private updateStreak() {
    this.streakService.updateStreak(this.userId).subscribe({
      next: res => {
        this.streak = res.streak || 0;
        console.log('Streak updated to:', this.streak);
      },
      error: err => console.error('Update failed:', err)
    });
  }

  ngOnDestroy() {
    if (this.testTimer) clearTimeout(this.testTimer);
  }

}
