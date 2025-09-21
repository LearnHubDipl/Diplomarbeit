import { Component, OnInit } from '@angular/core';
import { ChartData, ChartType, ChartConfiguration, Plugin } from 'chart.js';
import { StatsLegendEntry, StatsOverviewDto, StatsService } from '../../../../shared/src/lib/services/stats.service';
import { CenterTextPlugin } from '../plugin/chart-text.plugin';
import {NgChartsModule} from 'ng2-charts';
import {NgForOf, NgStyle} from '@angular/common';

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
  userId = 1; // statisch für Test

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
      const unansweredEntry = data.legend.find(e => e.label.toLowerCase().includes('nicht beantwortet'));
      this.chartOptions!.plugins!.centerText!.text = unansweredEntry ? `${unansweredEntry.value} offene Fragen` : '';
    });
  }
}
