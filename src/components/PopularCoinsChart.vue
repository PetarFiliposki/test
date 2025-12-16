<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { Line } from "vue-chartjs";
import ChartService from "../service/chartService";
import { Chart, registerables } from "chart.js";
import { useCoinsStore } from "../store/coinsStore";

Chart.register(...registerables);

const coinsStore = useCoinsStore();

const topCoins = computed(() => {
  return [...coinsStore.coinDataTop50]
      .sort((a, b) => parseFloat(b.volumeUsd24Hr) - parseFloat(a.volumeUsd24Hr))
      .slice(0, 4);
});

const coinColors2 = ref({});
const colors = [
  "rgba(75, 192, 192, 0.5)",
  "rgba(54, 162, 235, 0.5)",
  "rgba(255, 99, 132, 0.5)",
  "rgba(255, 206, 86, 0.5)",
];

const selectedCoinData = ref("1DAY");
const chartData = ref({});
const loading = ref(false);

const chartOptions = {
  responsive: true,
  maintainAspectRatio: true,
  interaction: {
    mode: "index",
    intersect: false,
  },
  plugins: {
    tooltip: {
      enabled: true,
      mode: "index",
      intersect: false,
      callbacks: {
        label: function (context) {
          return `Price: $${formatPrice(context.parsed.y)}`;
        },
      },
    },
    legend: {
      display: true,
      labels: {
        color: 'white'
      }
    }
  },
  scales: {
    x: {
      display: true,
      ticks: {
        color: 'white',
        maxRotation: 45,
        minRotation: 45
      }
    },
    y: {
      display: true,
      ticks: {
        color: 'white'
      }
    },
  },
};

const fetchChartData = async () => {
  loading.value = true;
  chartData.value = {}; // Clear previous data

  console.log("Fetching chart data for period:", selectedCoinData.value);

  for (const coin of topCoins.value) {
    try {
      console.log(`Fetching data for ${coin.id}...`);
      const response = await ChartService.getChart(coin.id, selectedCoinData.value);

      console.log(`Response for ${coin.id}:`, response.data);

      const chartArray = response.data?.data;

      if (chartArray && chartArray.length > 0) {
        const prices = chartArray.map((entry) => {
          const date = new Date(entry.time);
          return {
            date: selectedCoinData.value === "1DAY"
                ? date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
                : date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
            price: parseFloat(entry.priceUsd),
          };
        });

        chartData.value[coin.id] = {
          labels: prices.map((entry) => entry.date),
          datasets: [
            {
              label: `${coin.symbol} Price (USD)`,
              backgroundColor: coinColors2.value[coin.id].backgroundColor,
              borderColor: coinColors2.value[coin.id].borderColor,
              borderWidth: 2,
              fill: true,
              tension: 0.4, // Smooth lines
              data: prices.map((entry) => entry.price),
            },
          ],
        };

        console.log(`Successfully loaded ${prices.length} data points for ${coin.id}`);
      } else {
        console.error(`No data returned for ${coin.id}`);
      }
    } catch (error) {
      console.error(`Error fetching chart data for ${coin.id}:`, error);
      console.error("Error details:", error.response?.data || error.message);
    }
  }

  loading.value = false;
};

// Watch for changes in topCoins
watch(topCoins, (newCoins) => {
  if (newCoins.length > 0) {
    newCoins.forEach((coin, index) => {
      if (!coinColors2.value[coin.id]) {
        coinColors2.value[coin.id] = {
          backgroundColor: colors[index % colors.length],
          borderColor: colors[index % colors.length].replace("0.5", "1"),
        };
      }
    });

    // Only fetch if we don't have data yet
    if (Object.keys(chartData.value).length === 0) {
      fetchChartData();
    }
  }
}, { immediate: true });

onMounted(() => {
  if (topCoins.value.length > 0) {
    topCoins.value.forEach((coin, index) => {
      coinColors2.value[coin.id] = {
        backgroundColor: colors[index],
        borderColor: colors[index].replace("0.5", "1"),
      };
    });
    fetchChartData();
  }
});

function formatPrice(price) {
  if (price >= 1.01) return price.toFixed(2);
  const [_, decimals] = price.toString().split(".");
  if (!decimals) return price.toFixed(2);
  const firstNonZeroIndex = decimals.search(/[^0]/);
  if (firstNonZeroIndex >= 4) {
    return price.toFixed(8);
  } else if (firstNonZeroIndex >= 2) {
    return price.toFixed(4);
  } else {
    return price.toFixed(2);
  }
}
</script>

<template>
  <div className="bg-[#1B2028] rounded-[10px] p-[20px] w-[1000px]">
    <div className="w-full flex justify-between items-center mb-6">
      <h1 className="font-bold text-white text-3xl">
        Today's most popular coins chart
      </h1>

      <select
          v-model="selectedCoinData"
          @change="fetchChartData"
          className="border-none bg-[#454151] text-white font-bold p-[8px] rounded-md cursor-pointer hover:bg-[#5a5566] transition-colors"
      >
        <option value="1DAY">24 Hours</option>
        <option value="7DAY">7 Days</option>
        <option value="1MTH">1 Month</option>
      </select>
    </div>

    <div v-if="loading" className="text-white text-center py-8">
      Loading charts...
    </div>

    <div v-else className="flex flex-wrap justify-around gap-4">
      <div v-for="coin in topCoins" :key="coin.id" className="w-[48%] mb-6 bg-[#252a34] p-4 rounded-lg">
        <h2 class="text-white font-bold text-center mb-4 text-xl">
          {{ coin.name }} ({{ coin.symbol }})
        </h2>
        <div v-if="chartData[coin.id] && chartData[coin.id].labels.length > 0" className="h-[250px]">
          <Line
              :data="chartData[coin.id]"
              :options="chartOptions"
          />
        </div>
        <p v-else class="text-red-400 text-center py-8">
          Error: Unable to load chart data for {{ coin.symbol }}
        </p>
      </div>
    </div>
  </div>
</template>