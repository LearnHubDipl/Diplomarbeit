import { Plugin } from 'chart.js';

export const CenterTextPlugin: Plugin = {
  id: 'centerText',
  beforeDraw(chart) {
    const { width, height, ctx } = chart;
    const text = '??? offene Fragen';

    ctx.save();
    ctx.font = 'bold 1rem Arial';
    ctx.fillStyle = '#444';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(text, width / 2, height / 2);
    ctx.restore();
  }
};
