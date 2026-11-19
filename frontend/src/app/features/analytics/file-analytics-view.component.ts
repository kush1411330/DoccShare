import { AfterViewInit, Component, ElementRef, Input, OnDestroy, OnInit, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, registerables } from 'chart.js';
import { AnalyticsService } from '../../core/services/analytics.service';
import { FileAnalytics } from '../../core/models/analytics.model';

Chart.register(...registerables);

@Component({
  selector: 'app-file-analytics-view',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="space-y-6">
      <!-- Top KPIs & Date Range Filter -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h4 class="text-base font-semibold text-white">Download Analytics</h4>
          <p class="text-xs text-slate-400">Visitor insights, geographic distributions, and device stats</p>
        </div>

        <div class="flex items-center gap-1.5 p-1 bg-slate-900 border border-slate-800 rounded-xl self-start sm:self-auto">
          @for (range of dateRanges; track range.days) {
            <button
              (click)="selectRange(range.days)"
              [class.bg-indigo-600]="selectedDays() === range.days"
              [class.text-white]="selectedDays() === range.days"
              [class.text-slate-400]="selectedDays() !== range.days"
              class="px-3 py-1 text-xs font-medium rounded-lg hover:text-white transition-colors"
            >
              {{ range.label }}
            </button>
          }
        </div>
      </div>

      <!-- KPI Metric Cards -->
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div class="p-5 rounded-2xl bg-slate-900/60 border border-slate-800 relative overflow-hidden">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Total Downloads</span>
            <div class="w-8 h-8 rounded-lg bg-indigo-600/20 text-indigo-400 flex items-center justify-center border border-indigo-500/30">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"/></svg>
            </div>
          </div>
          <div class="text-3xl font-extrabold text-white mt-3">{{ analytics()?.totalDownloads || 0 }}</div>
          <div class="text-xs text-slate-500 mt-1">Downloads within selected period</div>
        </div>

        <div class="p-5 rounded-2xl bg-slate-900/60 border border-slate-800 relative overflow-hidden">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Unique Visitors</span>
            <div class="w-8 h-8 rounded-lg bg-sky-600/20 text-sky-400 flex items-center justify-center border border-sky-500/30">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/></svg>
            </div>
          </div>
          <div class="text-3xl font-extrabold text-white mt-3">{{ analytics()?.uniqueVisitors || 0 }}</div>
          <div class="text-xs text-slate-500 mt-1">Hashed IP privacy-preserving count</div>
        </div>
      </div>

      <!-- Time Series Chart -->
      <div class="p-5 rounded-2xl bg-slate-900/60 border border-slate-800">
        <h5 class="text-sm font-semibold text-slate-200 mb-4">Downloads Over Time</h5>
        <div class="h-64 w-full relative">
          <canvas #lineChartCanvas></canvas>
        </div>
      </div>

      <!-- Breakdowns Grid -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <!-- Browsers -->
        <div class="p-5 rounded-2xl bg-slate-900/60 border border-slate-800 space-y-3">
          <h5 class="text-sm font-semibold text-slate-200">Browsers</h5>
          @if (!analytics()?.browsers?.length) {
            <p class="text-xs text-slate-500 py-4 text-center">No browser data yet</p>
          } @else {
            <div class="space-y-2">
              @for (b of analytics()?.browsers; track b.name) {
                <div>
                  <div class="flex justify-between text-xs text-slate-300 mb-1">
                    <span>{{ b.name }}</span>
                    <span class="font-semibold">{{ b.count }}</span>
                  </div>
                  <div class="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
                    <div class="bg-indigo-500 h-full rounded-full" [style.width.%]="getPercentage(b.count)"></div>
                  </div>
                </div>
              }
            </div>
          }
        </div>

        <!-- Operating Systems -->
        <div class="p-5 rounded-2xl bg-slate-900/60 border border-slate-800 space-y-3">
          <h5 class="text-sm font-semibold text-slate-200">Operating Systems</h5>
          @if (!analytics()?.operatingSystems?.length) {
            <p class="text-xs text-slate-500 py-4 text-center">No OS data yet</p>
          } @else {
            <div class="space-y-2">
              @for (os of analytics()?.operatingSystems; track os.name) {
                <div>
                  <div class="flex justify-between text-xs text-slate-300 mb-1">
                    <span>{{ os.name }}</span>
                    <span class="font-semibold">{{ os.count }}</span>
                  </div>
                  <div class="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
                    <div class="bg-sky-500 h-full rounded-full" [style.width.%]="getPercentage(os.count)"></div>
                  </div>
                </div>
              }
            </div>
          }
        </div>

        <!-- Devices -->
        <div class="p-5 rounded-2xl bg-slate-900/60 border border-slate-800 space-y-3">
          <h5 class="text-sm font-semibold text-slate-200">Device Types</h5>
          @if (!analytics()?.devices?.length) {
            <p class="text-xs text-slate-500 py-4 text-center">No device data yet</p>
          } @else {
            <div class="space-y-2">
              @for (d of analytics()?.devices; track d.name) {
                <div>
                  <div class="flex justify-between text-xs text-slate-300 mb-1">
                    <span class="capitalize">{{ d.name }}</span>
                    <span class="font-semibold">{{ d.count }}</span>
                  </div>
                  <div class="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
                    <div class="bg-emerald-500 h-full rounded-full" [style.width.%]="getPercentage(d.count)"></div>
                  </div>
                </div>
              }
            </div>
          }
        </div>
      </div>

      <!-- Countries Table -->
      <div class="p-5 rounded-2xl bg-slate-900/60 border border-slate-800 space-y-3">
        <h5 class="text-sm font-semibold text-slate-200">Top Countries & Locations</h5>
        @if (!analytics()?.countries?.length) {
          <p class="text-xs text-slate-500 py-4 text-center">No geographic data recorded yet</p>
        } @else {
          <div class="overflow-x-auto">
            <table class="w-full text-left text-xs">
              <thead class="text-slate-400 border-b border-slate-800">
                <tr>
                  <th class="pb-2 font-medium">Country</th>
                  <th class="pb-2 font-medium">Code</th>
                  <th class="pb-2 font-medium text-right">Downloads</th>
                  <th class="pb-2 font-medium text-right">Share</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-800/60">
                @for (c of analytics()?.countries; track c.countryCode) {
                  <tr>
                    <td class="py-2.5 font-medium text-slate-200">{{ c.countryName || 'Unknown' }}</td>
                    <td class="py-2.5 font-mono text-slate-400">{{ c.countryCode }}</td>
                    <td class="py-2.5 text-right font-semibold text-slate-200">{{ c.count }}</td>
                    <td class="py-2.5 text-right text-slate-400">{{ getPercentage(c.count) }}%</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>

      <!-- Recent Downloads Table -->
      <div class="p-5 rounded-2xl bg-slate-900/60 border border-slate-800 space-y-3">
        <h5 class="text-sm font-semibold text-slate-200">Recent Download Events</h5>
        @if (!analytics()?.recent?.length) {
          <p class="text-xs text-slate-500 py-4 text-center">No download activity yet</p>
        } @else {
          <div class="overflow-x-auto">
            <table class="w-full text-left text-xs">
              <thead class="text-slate-400 border-b border-slate-800">
                <tr>
                  <th class="pb-2 font-medium">Timestamp</th>
                  <th class="pb-2 font-medium">Location</th>
                  <th class="pb-2 font-medium">Client / Device</th>
                  <th class="pb-2 font-medium">Referrer</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-800/60">
                @for (ev of analytics()?.recent; track ev.id) {
                  <tr>
                    <td class="py-2.5 text-slate-400 whitespace-nowrap">{{ ev.downloadedAt | date:'medium' }}</td>
                    <td class="py-2.5 text-slate-300">
                      {{ ev.city ? ev.city + ', ' : '' }}{{ ev.countryName || 'Unknown' }}
                    </td>
                    <td class="py-2.5 text-slate-300">
                      {{ ev.browser }} on {{ ev.os }} <span class="text-slate-500">({{ ev.deviceType }})</span>
                    </td>
                    <td class="py-2.5 text-slate-400 truncate max-w-[200px]">{{ ev.referrer || 'Direct' }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>
    </div>
  `
})
export class FileAnalyticsViewComponent implements OnInit, AfterViewInit, OnDestroy {
  private analyticsService = inject(AnalyticsService);

  @Input({ required: true }) fileId!: string;
  @ViewChild('lineChartCanvas') lineChartCanvas?: ElementRef<HTMLCanvasElement>;

  analytics = signal<FileAnalytics | null>(null);
  selectedDays = signal<number>(30);

  dateRanges = [
    { label: '7 Days', days: 7 },
    { label: '30 Days', days: 30 },
    { label: '90 Days', days: 90 },
  ];

  private chartInstance?: Chart;

  ngOnInit() {
    this.loadAnalytics();
  }

  ngAfterViewInit() {
    if (this.analytics()) {
      this.renderChart();
    }
  }

  ngOnDestroy() {
    if (this.chartInstance) {
      this.chartInstance.destroy();
    }
  }

  selectRange(days: number) {
    this.selectedDays.set(days);
    this.loadAnalytics();
  }

  loadAnalytics() {
    const from = new Date(Date.now() - this.selectedDays() * 24 * 3600 * 1000).toISOString();
    const to = new Date().toISOString();

    this.analyticsService.getFileAnalytics(this.fileId, from, to).subscribe({
      next: (data) => {
        this.analytics.set(data);
        setTimeout(() => this.renderChart(), 50);
      }
    });
  }

  getPercentage(count: number): number {
    const total = this.analytics()?.totalDownloads || 0;
    if (total === 0) return 0;
    return Math.round((count / total) * 100);
  }

  private renderChart() {
    if (!this.lineChartCanvas?.nativeElement) return;

    if (this.chartInstance) {
      this.chartInstance.destroy();
    }

    const data = this.analytics();
    const timeSeries = data?.timeSeries || [];

    const labels = timeSeries.map(item => item.date);
    const counts = timeSeries.map(item => item.count);

    const ctx = this.lineChartCanvas.nativeElement.getContext('2d');
    if (!ctx) return;

    this.chartInstance = new Chart(ctx, {
      type: 'line',
      data: {
        labels: labels.length > 0 ? labels : ['No downloads yet'],
        datasets: [{
          label: 'Downloads',
          data: counts.length > 0 ? counts : [0],
          borderColor: '#6366f1',
          backgroundColor: 'rgba(99, 102, 241, 0.1)',
          fill: true,
          tension: 0.3,
          pointBackgroundColor: '#818cf8',
          pointBorderColor: '#1e1b4b',
          pointBorderWidth: 2,
          pointRadius: 4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false }
        },
        scales: {
          x: {
            grid: { color: 'rgba(255, 255, 255, 0.05)' },
            ticks: { color: '#94a3b8', font: { size: 11 } }
          },
          y: {
            beginAtZero: true,
            grid: { color: 'rgba(255, 255, 255, 0.05)' },
            ticks: {
              color: '#94a3b8',
              font: { size: 11 },
              stepSize: 1
            }
          }
        }
      }
    });
  }
}
