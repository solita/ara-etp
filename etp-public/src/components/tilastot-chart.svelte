<script>
  import { tick } from 'svelte';
  import { _ } from '@Localization/localization';
  import {
    Chart,
    BarElement,
    BarController,
    CategoryScale,
    LinearScale,
    Tooltip
  } from 'chart.js';

  Chart.register(
    BarElement,
    BarController,
    CategoryScale,
    LinearScale,
    Tooltip
  );

  export let data = [];
  export let printing;
  export let labels = ['A', 'B', 'C', 'D', 'E', 'F', 'G'];
  let labelsVisual = [];
  let ariaLabelText = $_('TILASTOT_CHART_ALT') + ' ';
  let ariaLabelStats = '';
  let chartCanvas, chartInstance;

  $: {
    ariaLabelStats = '';
    labels.forEach((label, index) => {
      labelsVisual[index] = `${label} (${Math.round(data?.[index] * 100)}%)`;
      ariaLabelStats += `${label}: ${Math.round(data?.[index] * 100)}%`;

      if (index < labels.length - 1) {
        ariaLabelStats += ', ';
      }
    });
  }

  const colors = [
    '#1d8c38',
    '#72a42f',
    '#c3cc16',
    '#ffe900',
    '#e5ac00',
    '#c95a00',
    '#bc000b'
  ];

  const options = {
    responsive: true,
    maintainAspectRatio: true,
    scales: {
      y: {
        ticks: {
          callback: value => {
            return value * 100 + '%';
          }
        }
      }
    },
    plugins: {
      legend: { display: false },
      tooltip: {
        enabled: true,
        displayColors: false,
        callbacks: {
          label: ctx => {
            // return (ctx.raw * 100).toFixed(0) + '%';
            return '';
          }
        }
      }
    }
  };

  const drawChart = data => {
    if (chartInstance && data) {
      chartInstance.data.datasets.forEach(dataset => {
        dataset.data.push(data);
      });
      chartInstance.update();
    } else if (chartCanvas) {
      chartInstance = new Chart(chartCanvas, {
        type: 'bar',
        data: {
          labels: labelsVisual,
          datasets: [
            {
              data: data,
              backgroundColor: colors
            }
          ]
        },
        options: options
      });
    }
  };

  $: {
    tick().then(() => {
      drawChart(data);
    });
  }
</script>

<style>
  .chart-parent {
    width: 99%;
  }

  .printing {
    width: 440px !important;
    height: 220px;
  }

  .printing canvas {
    width: 440px !important;
    height: 220px !important;
  }
  canvas {
    break-inside: avoid;
    page-break-inside: avoid;
  }
</style>

<div class:printing class:chart-parent={!printing}>
  <canvas
    bind:this={chartCanvas}
    aria-label={ariaLabelText + ariaLabelStats}
    role="img" />
</div>
