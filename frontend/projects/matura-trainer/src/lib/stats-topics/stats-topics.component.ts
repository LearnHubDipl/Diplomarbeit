import { Component, OnInit, ViewChild } from '@angular/core';
import { BaseChartDirective, NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType, Plugin } from 'chart.js';
import { StatsService, StatsOverviewDto, StatsLegendEntry } from '../stats.service';
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

  topicPools: TopicPool[] = [];
  selectedTopicPoolId = 0;

  chartPlugins: Plugin[] = [CenterTextPlugin];

  doughnutChartLabels: string[] = [];
  doughnutChartData: ChartData<'doughnut'> = {
    labels: [],
    datasets: [{ data: [], backgroundColor: [] }]
  };
  doughnutChartType: ChartType = 'doughnut';

  chartOptions: ChartConfiguration['options'] = {
    plugins: {
      legend: { display: false },
      centerText: { text: '' }
    }
  };

  legendData: StatsLegendEntry[] = [];
  userId = 1; // statisch für Test

  constructor(private statsService: StatsService) {}

  ngOnInit(): void {
    // Alle TopicPools vom Backend laden
    this.statsService.getTopicPools(this.userId).subscribe(pools => {
      this.topicPools = pools;
      if (pools.length > 0) {
        this.selectedTopicPoolId = pools[0].id;
        this.loadChartData();
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

    // Backend liefert aggregierte Statistik inkl. Legend
    this.statsService.getStatsOverviewForTopicPool(this.userId, this.selectedTopicPoolId)
      .subscribe((data: StatsOverviewDto) => {

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

        // CenterText zeigt offene Fragen (nicht beantwortet)
        const unansweredEntry = data.legend.find(e => e.label.toLowerCase().includes('nicht beantwortet'));
        this.chartOptions!.plugins!.centerText!.text = unansweredEntry ? `${unansweredEntry.value} offene Fragen` : '';

        this.chart?.update();
      });
  }
}
