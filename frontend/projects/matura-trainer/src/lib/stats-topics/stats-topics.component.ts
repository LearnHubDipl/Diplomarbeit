import {Component, inject, OnInit, ViewChild} from '@angular/core';
import { BaseChartDirective, NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType, Plugin } from 'chart.js';
import { StatsService, StatsOverviewDto, StatsLegendEntry } from '../../../../shared/src/lib/services/stats.service';
import { CenterTextPlugin } from '../plugin/chart-text.plugin';
import { FormsModule } from '@angular/forms';
import {NgClass, NgForOf, NgIf, NgStyle} from '@angular/common';
import {UserInitializationService} from '../../../../shared/src/lib/services/user-initialization.service';
import {RouterLink} from '@angular/router';

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
    NgClass,
    NgIf,
    RouterLink
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
  userId = -1;

  chartPlugins: Plugin[] = [CenterTextPlugin];
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

  // VORINITIALISIERUNG: Damit die Legende immer da ist
  legendData: StatsLegendEntry[] = [
    { label: 'ausreichend gelernt', value: 0, color: '#28a745' },
    { label: '2x richtig beantwortet', value: 0, color: '#44ff44' },
    { label: '1x richtig beantwortet', value: 0, color: '#b3ffb3' },
    { label: 'falsch', value: 0, color: '#ff8888' },
    { label: 'nicht beantwortet', value: 0, color: '#ffeeba' }
  ];

  constructor(private statsService: StatsService) {}

  ngOnInit(): void {
    const user = this.userService.getCurrentUser();
    if (user) {
      this.userId = user.id;
      this.statsService.getTopicPools(this.userId).subscribe(pools => {
        this.topicPools = pools;
        if (pools.length > 0) {
          this.selectedTopicPoolId = pools[0].id;
          this.loadChartData();
        }
      });
    }
  }

  get hasChartData(): boolean {
    const data = this.doughnutChartData.datasets[0].data;
    return data && data.length > 0 && data.some(value => value > 0);
  }

  onTopicPoolChange(value: any): void {
    this.selectedTopicPoolId = Number(value);
    this.loadChartData();
  }

  loadChartData(): void {
    if (!this.selectedTopicPoolId) return;
    this.statsService.getStatsOverviewForTopicPool(this.userId, this.selectedTopicPoolId)
      .subscribe((data: StatsOverviewDto) => {
        const rawData = data.legend.map(entry => entry.value);
        const colors = data.legend.map(entry => entry.color);

        this.doughnutChartData = {
          labels: data.legend.map(entry => entry.label),
          datasets: [{ data: rawData, backgroundColor: colors }]
        };

        this.legendData = data.legend;

        const unansweredEntry = data.legend.find(e => e.label.toLowerCase().includes('nicht beantwortet'));
        this.chartOptions!.plugins!.centerText!.text = unansweredEntry ? `${unansweredEntry.value} offene Fragen` : '';
        this.chartOptions!.plugins!.centerText!.text = `${data.unanswered} offene Fragen`;

        this.chart?.update();
      });
  }
}
