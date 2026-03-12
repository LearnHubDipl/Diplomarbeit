import {Component, inject, OnInit, ViewChild} from '@angular/core';
import { BaseChartDirective, NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType, Plugin } from 'chart.js';
import { StatsService, StatsOverviewDto, StatsLegendEntry } from '../../../../shared/src/lib/services/stats.service';
import { CenterTextPlugin } from '../plugin/chart-text.plugin';
import { FormsModule } from '@angular/forms';
import { NgClass, NgForOf, NgStyle } from '@angular/common';
import {UserInitializationService} from '../../../../shared/src/lib/services/user-initialization.service';

export interface TopicPool {
  id: number;
  name: string;
}

@Component({
  selector: 'lib-stats-topics',
  standalone: true,
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

  userService: UserInitializationService = inject(UserInitializationService);

  topicPools: TopicPool[] = [];
  selectedTopicPoolId = 0;
  isDropdownOpen = false;

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
  userId = -1;

  constructor(private statsService: StatsService) {}

  ngOnInit(): void {
    this.userId = this.userService.getCurrentUser()!.id;
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

    this.statsService.getStatsOverviewForTopicPool(this.userId, this.selectedTopicPoolId)
      .subscribe((data: StatsOverviewDto) => {

        this.doughnutChartLabels = data.legend.map(entry => entry.label);
        const rawData = data.legend.map(entry => entry.value);
        const colors = data.legend.map(entry => entry.color);

        this.doughnutChartData = {
          labels: this.doughnutChartLabels,
          datasets: [{ data: rawData, backgroundColor: colors }]
        };

        this.legendData = data.legend;

        this.chartOptions!.plugins!.centerText!.text = `${data.unanswered} offene Fragen`;

        this.chart?.update();
      });
  }
}
