import {Component, OnInit} from '@angular/core';
import { NgChartsModule } from 'ng2-charts';
import {ChartConfiguration, ChartType} from 'chart.js';
import {NgForOf, NgStyle} from '@angular/common';
import {StatsService} from '../stats.service';
import { Chart, Plugin } from 'chart.js';




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

export class StatsHomeComponent implements OnInit {
  public chartPlugins: Plugin[] = [];

  getCenterTextPlugin():Plugin{
    return {
      id: 'centerText',
      beforeDraw(chart) {
        const { width, height, ctx } = chart;
        const text = '2993 offene Fragen';

        ctx.save();
        ctx.font = 'bold 1rem Arial';
        ctx.fillStyle = '#444';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(text, width / 2, height / 2);
        ctx.restore();
      }
    };
  }

  public doughnutChartLabels: string[] = [ 'falsch' , 'ausreichend gelernt','2x richtig beantwortet','1x richtig beantwortet','nicht beantwortet' ];

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

  // Streak
  streak = 0;
  userId = 2;

  constructor(private statsService: StatsService) { }

  ngOnInit(): void {
    this.statsService.getStreak(this.userId).subscribe({
      next: (s) => this.streak = s,
      error: () => this.streak = 0
    });

    this.chartPlugins = [this.getCenterTextPlugin()];
  }






}
