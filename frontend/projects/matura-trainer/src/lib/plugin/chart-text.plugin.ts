import { Plugin } from 'chart.js';

declare module 'chart.js' {
  interface PluginOptionsByType<TType extends ChartType> {
    centerText?: {
      text?: string;
    };
  }
}


export const CenterTextPlugin: Plugin = {
  id: 'centerText',
  beforeDraw(chart) {
    const { width, height, ctx, config } = chart;
    const text = config.options?.plugins?.centerText?.text || '';

    ctx.save();
    ctx.font = 'bold 1rem Arial';
    ctx.fillStyle = '#444';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(text, width / 2, height / 2);
    ctx.restore();
  }
};
