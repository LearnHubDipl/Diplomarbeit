import { Component } from '@angular/core';
import { NgChartsModule } from 'ng2-charts';
import {ChartConfiguration, ChartType} from 'chart.js';
import {NgForOf, NgStyle} from '@angular/common';


@Component({
  selector: 'lib-stats-home',
  imports: [
    NgChartsModule,
    NgForOf,
    NgStyle
  ],
  templateUrl: './stats-home.component.html',
  styleUrl: './stats-home.component.css'
})
export class StatsHomeComponent {
  public doughnutChartLabels: string[] = [ 'Mathe', 'Deutsch', 'Englisch' ];

  public doughnutChartData = {
    labels: this.doughnutChartLabels,
    datasets: [
      {
        data: [25, 10, 15,30,20],
        backgroundColor: ['#FE8B8B', '#309F22', '#3DD32B','#B7F0B0','#FFEAA4'],
        barThickness: 10
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

  public legendData = [
    { label: 'falsch', value: 25, color: '#FE8B8B' },
    { label: 'nicht beantwortet', value: 22, color: '#FFEAA4' },
    { label: 'ausreichend gelernt', value: 25, color: '#309F22' },
    { label: '2x richtig beantwortet', value: 13, color: '#3DD32B' },
    { label: '1x richtig beantwortet', value: 15, color: '#B7F0B0' },
  ];




}
