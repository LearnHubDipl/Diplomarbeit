import { Component, OnInit } from '@angular/core';
import { ChartData, ChartType, ChartConfiguration, Plugin } from 'chart.js';
import { StatsLegendEntry, StatsOverviewDto, StatsService } from '../stats.service';
import { CenterTextPlugin } from '../plugin/chart-text.plugin';

@Component({
  selector: 'lib-stats-home',
  templateUrl: './stats-home.component.html',
  styleUrls: [
    '../styles/shared-styles.css',
    './stats-home.component.css'
  ]
})
export class StatsHomeComponent implements OnInit {

  chartPlugins: Plugin[] = [CenterTextPlugin];

  // Labels werden optional aus Backend übernommen
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
    // Backend liefert aggregierte Zahlen inkl. Legend
    this.statsService.getStatsOverview(this.userId).subscribe((data: StatsOverviewDto) => {

      // Labels, Daten und Farben direkt aus Backend-Legende
      this.doughnutChartLabels = data.legend.map(entry => entry.label);
      const rawData = data.legend.map(entry => entry.value);
      const colors = data.legend.map(entry => entry.color);

      this.doughnutChartData = {
        labels: this.doughnutChartLabels,
        datasets: [{ data: rawData, backgroundColor: colors }]
      };

      // Legend direkt aus Backend übernehmen
      this.legendData = data.legend;

      // CenterText zeigt offene Fragen
      const unansweredEntry = data.legend.find(e => e.label.toLowerCase().includes('nicht beantwortet'));
      this.chartOptions!.plugins!.centerText!.text = unansweredEntry ? `${unansweredEntry.value} offene Fragen` : '';
    });
  }
}
