import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AnalyticsService } from '../../core/services/analytics.service';
import { FileService } from '../../core/services/file.service';
import { AnalyticsOverview } from '../../core/models/analytics.model';
import { StoredFile } from '../../core/models/file.model';
import { FileSizePipe } from '../../shared/pipes/file-size.pipe';

@Component({
  selector: 'app-analytics-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, FileSizePipe],
  template: `
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      <div>
        <h1 class="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">Platform Analytics</h1>
        <p class="text-sm text-slate-400 mt-1">Global download telemetry, traffic trends, and file performance</p>
      </div>

      <!-- KPI Summary Cards -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="p-5 rounded-2xl bg-slate-900/60 border border-slate-800 backdrop-blur-sm">
          <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Total Downloads</span>
          <div class="text-3xl font-bold text-white mt-2">{{ overview()?.totalDownloads || 0 }}</div>
          <div class="text-xs text-slate-500 mt-1">All-time downloads across links</div>
        </div>

        <div class="p-5 rounded-2xl bg-slate-900/60 border border-slate-800 backdrop-blur-sm">
          <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Unique Visitors</span>
          <div class="text-3xl font-bold text-sky-400 mt-2">{{ overview()?.uniqueVisitors30Days || 0 }}</div>
          <div class="text-xs text-slate-500 mt-1">Unique IP hashes in last 30 days</div>
        </div>

        <div class="p-5 rounded-2xl bg-slate-900/60 border border-slate-800 backdrop-blur-sm">
          <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Total Files</span>
          <div class="text-3xl font-bold text-indigo-400 mt-2">{{ overview()?.totalFiles || 0 }}</div>
          <div class="text-xs text-slate-500 mt-1">Active files in storage</div>
        </div>

        <div class="p-5 rounded-2xl bg-slate-900/60 border border-slate-800 backdrop-blur-sm">
          <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Active Links</span>
          <div class="text-3xl font-bold text-emerald-400 mt-2">{{ overview()?.totalLinks || 0 }}</div>
          <div class="text-xs text-slate-500 mt-1">Configured share links</div>
        </div>
      </div>

      <!-- Top Files Table -->
      <div class="p-6 rounded-2xl bg-slate-900/60 border border-slate-800 backdrop-blur-sm space-y-4">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-base font-semibold text-white">Top Performing Files</h3>
            <p class="text-xs text-slate-400">Click on any file to view granular per-link geography, browser & device breakdowns</p>
          </div>
          <a routerLink="/dashboard" class="text-xs font-medium text-indigo-400 hover:text-indigo-300">View All Files →</a>
        </div>

        @if (topFiles().length === 0) {
          <p class="text-xs text-slate-500 py-6 text-center">No files uploaded yet</p>
        } @else {
          <div class="overflow-x-auto">
            <table class="w-full text-left text-sm">
              <thead class="text-xs uppercase tracking-wider text-slate-400 border-b border-slate-800">
                <tr>
                  <th class="pb-2.5 font-semibold">File</th>
                  <th class="pb-2.5 font-semibold">Size</th>
                  <th class="pb-2.5 font-semibold text-center">Total Downloads</th>
                  <th class="pb-2.5 font-semibold text-right">Action</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-800/60">
                @for (file of topFiles(); track file.id) {
                  <tr class="hover:bg-slate-800/30 transition-colors">
                    <td class="py-3 font-medium text-slate-200">
                      <a [routerLink]="['/files', file.id]" class="hover:text-indigo-400 transition-colors">
                        {{ file.originalName }}
                      </a>
                    </td>
                    <td class="py-3 text-xs text-slate-400">{{ file.sizeBytes | fileSize }}</td>
                    <td class="py-3 text-center">
                      <span class="px-2.5 py-1 rounded-full text-xs font-semibold bg-indigo-500/10 text-indigo-300 border border-indigo-500/20">
                        {{ file.downloadCount }}
                      </span>
                    </td>
                    <td class="py-3 text-right">
                      <a
                        [routerLink]="['/files', file.id]"
                        class="px-3 py-1 text-xs font-medium rounded-lg bg-indigo-600/20 text-indigo-300 hover:bg-indigo-600/30 border border-indigo-500/30 transition-colors"
                      >
                        Deep Dive
                      </a>
                    </td>
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
export class AnalyticsDashboardComponent implements OnInit {
  private analyticsService = inject(AnalyticsService);
  private fileService = inject(FileService);

  overview = signal<AnalyticsOverview | null>(null);
  topFiles = signal<StoredFile[]>([]);

  ngOnInit() {
    this.analyticsService.getOverview().subscribe({
      next: (data) => this.overview.set(data)
    });

    this.fileService.listFiles(0, 10, 'downloadCount,desc').subscribe({
      next: (page) => this.topFiles.set(page.content)
    });
  }
}
