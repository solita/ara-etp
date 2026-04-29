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
  export let version = '2018';
  $: labels =
    version === '2026'
      ? ['A0', 'A+', 'A', 'B', 'C', 'D', 'E', 'F', 'G']
      : ['A', 'B', 'C', 'D', 'E', 'F', 'G'];
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

  const colorMap = {
    'A+': '#009641',
    A0: '#52ae32',
    A: '#c8d302',
    B: '#ffed00',
    C: '#fbb900',
    D: '#ec6608',
    E: '#e50104',
    F: '#e40202',
    G: '#e40202'
  };

  $: colors = labels.map(label => colorMap[label]);

  $: options = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      x: {
        ticks: {
          maxRotation: 45,
          minRotation: 45
        }
      },
      y: {
        min: 0,
        max: 1,
        ticks: {
          stepSize: 0.2,
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
    height: 300px;
  }

  @media (min-width: 768px) {
    .chart-parent {
      height: 250px;
    }
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
  <canvas bind:this={chartCanvas} aria-label={ariaLabelText + ariaLabelStats} />
</div>
