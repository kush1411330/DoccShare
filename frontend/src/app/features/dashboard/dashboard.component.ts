import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { FileService } from '../../core/services/file.service';
import { AnalyticsService } from '../../core/services/analytics.service';
import { ToastService } from '../../core/services/toast.service';
import { Page, StoredFile } from '../../core/models/file.model';
import { AnalyticsOverview } from '../../core/models/analytics.model';
import { FileSizePipe } from '../../shared/pipes/file-size.pipe';
import { UploadDialogComponent } from './upload-dialog.component';
import { CreateShareModalComponent } from '../share/create-share-modal.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, FileSizePipe, UploadDialogComponent, CreateShareModalComponent],
  template: `
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      <!-- Welcome & Action Header -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 class="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">File Dashboard</h1>
          <p class="text-sm text-slate-400 mt-1">Upload files, generate secure share links, and monitor download analytics</p>
        </div>
        <button
          (click)="showUploadModal.set(true)"
          class="px-5 py-2.5 rounded-xl font-medium text-sm text-white bg-indigo-600 hover:bg-indigo-500 shadow-lg shadow-indigo-600/30 transition-all hover:scale-[1.02] flex items-center justify-center gap-2 self-start sm:self-auto"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"/></svg>
          <span>Upload File</span>
        </button>
      </div>

      <!-- Quick KPI Stats -->
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div class="p-5 rounded-2xl bg-slate-900/60 border border-slate-800 backdrop-blur-sm">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Total Files</span>
            <div class="w-8 h-8 rounded-lg bg-indigo-600/20 text-indigo-400 flex items-center justify-center border border-indigo-500/30">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z"/></svg>
            </div>
          </div>
          <div class="text-3xl font-bold text-white mt-3">{{ overview()?.totalFiles || 0 }}</div>
          <div class="text-xs text-slate-500 mt-1">Ready in cloud storage</div>
        </div>

        <div class="p-5 rounded-2xl bg-slate-900/60 border border-slate-800 backdrop-blur-sm">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Active Share Links</span>
            <div class="w-8 h-8 rounded-lg bg-sky-600/20 text-sky-400 flex items-center justify-center border border-sky-500/30">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"/></svg>
            </div>
          </div>
          <div class="text-3xl font-bold text-white mt-3">{{ overview()?.totalLinks || 0 }}</div>
          <div class="text-xs text-slate-500 mt-1">Expiring & password-protected</div>
        </div>

        <div class="p-5 rounded-2xl bg-slate-900/60 border border-slate-800 backdrop-blur-sm">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold uppercase tracking-wider text-slate-400">Total Downloads</span>
            <div class="w-8 h-8 rounded-lg bg-emerald-600/20 text-emerald-400 flex items-center justify-center border border-emerald-500/30">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"/></svg>
            </div>
          </div>
          <div class="text-3xl font-bold text-white mt-3">{{ overview()?.totalDownloads || 0 }}</div>
          <div class="text-xs text-slate-500 mt-1">{{ overview()?.uniqueVisitors30Days || 0 }} unique visitors (30d)</div>
        </div>
      </div>

      <!-- Search & Controls -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pt-2">
        <div class="relative max-w-sm w-full">
          <input
            type="text"
            [(ngModel)]="searchQuery"
            (ngModelChange)="onSearchChange()"
            placeholder="Search files by name..."
            class="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-900/80 border border-slate-800 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition-colors"
          />
          <svg class="w-4 h-4 text-slate-500 absolute left-3.5 top-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
        </div>

        <div class="flex items-center gap-2 text-xs text-slate-400 self-end sm:self-auto">
          <span>Sort by:</span>
          <select
            [(ngModel)]="sortOption"
            (ngModelChange)="loadFiles()"
            class="bg-slate-900 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-slate-300 focus:outline-none focus:border-indigo-500"
          >
            <option value="createdAt,desc">Newest First</option>
            <option value="createdAt,asc">Oldest First</option>
            <option value="originalName,asc">Name (A-Z)</option>
            <option value="sizeBytes,desc">Largest Size</option>
          </select>
        </div>
      </div>

      <!-- File Table / Cards -->
      <div class="bg-slate-900/60 border border-slate-800 rounded-2xl overflow-hidden backdrop-blur-sm">
        @if (loading()) {
          <div class="py-16 text-center text-slate-400 text-sm">
            <svg class="animate-spin h-6 w-6 mx-auto text-indigo-400 mb-2" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            Loading files...
          </div>
        } @else if (files().length === 0) {
          <div class="py-16 text-center">
            <div class="w-12 h-12 rounded-2xl bg-slate-800 flex items-center justify-center text-slate-500 mx-auto mb-3">
              <svg class="w-6 h-6 text-indigo-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"/></svg>
            </div>
            <h4 class="text-base font-medium text-slate-200">No files found</h4>
            <p class="text-xs text-slate-500 mt-1 mb-4">
              {{ searchQuery ? 'Try a different search term' : 'Upload your first file to get started' }}
            </p>
            @if (!searchQuery) {
              <button
                (click)="showUploadModal.set(true)"
                class="px-4 py-2 text-xs font-semibold rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white shadow-md shadow-indigo-600/20 transition-all"
              >
                Upload File
              </button>
            }
          </div>
        } @else {
          <div class="overflow-x-auto">
            <table class="w-full text-left text-sm">
              <thead class="text-xs uppercase tracking-wider text-slate-400 bg-slate-950/40 border-b border-slate-800">
                <tr>
                  <th class="py-3 px-4 font-semibold">Name</th>
                  <th class="py-3 px-4 font-semibold">Size</th>
                  <th class="py-3 px-4 font-semibold hidden md:table-cell">Uploaded</th>
                  <th class="py-3 px-4 font-semibold text-center">Downloads</th>
                  <th class="py-3 px-4 font-semibold text-right">Actions</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-800/60">
                @for (file of files(); track file.id) {
                  <tr class="hover:bg-slate-800/30 transition-colors group">
                    <td class="py-3.5 px-4 font-medium text-slate-200">
                      <a [routerLink]="['/files', file.id]" class="flex items-center gap-3 hover:text-indigo-400 transition-colors">
                        <div class="w-8 h-8 rounded-lg bg-slate-800 flex items-center justify-center text-indigo-400 shrink-0">
                          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
                        </div>
                        <span class="truncate max-w-[200px] sm:max-w-xs md:max-w-sm font-semibold">{{ file.originalName }}</span>
                      </a>
                    </td>
                    <td class="py-3.5 px-4 text-xs text-slate-400 whitespace-nowrap">{{ file.sizeBytes | fileSize }}</td>
                    <td class="py-3.5 px-4 text-xs text-slate-400 whitespace-nowrap hidden md:table-cell">{{ file.createdAt | date:'mediumDate' }}</td>
                    <td class="py-3.5 px-4 text-center">
                      <span class="px-2.5 py-1 rounded-full text-xs font-semibold bg-indigo-500/10 text-indigo-300 border border-indigo-500/20">
                        {{ file.downloadCount }}
                      </span>
                    </td>
                    <td class="py-3.5 px-4 text-right">
                      <div class="flex items-center justify-end gap-1.5">
                        <button
                          (click)="openShareModal(file)"
                          class="px-2.5 py-1 text-xs font-medium text-indigo-300 hover:text-white bg-indigo-600/20 hover:bg-indigo-600/40 rounded-lg border border-indigo-500/30 transition-colors flex items-center gap-1"
                          title="Create share link"
                        >
                          <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"/></svg>
                          <span class="hidden sm:inline">Share</span>
                        </button>
                        <a
                          [routerLink]="['/files', file.id]"
                          class="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors"
                          title="View Details & Analytics"
                        >
                          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/></svg>
                        </a>
                        <button
                          (click)="deleteFile(file)"
                          class="p-1.5 text-slate-400 hover:text-rose-400 rounded-lg hover:bg-rose-500/10 transition-colors"
                          title="Delete File"
                        >
                          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
                        </button>
                      </div>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>

          <!-- Pagination -->
          @if (totalPages() > 1) {
            <div class="px-4 py-3 bg-slate-950/40 border-t border-slate-800 flex items-center justify-between text-xs text-slate-400">
              <span>Showing Page {{ currentPage() + 1 }} of {{ totalPages() }}</span>
              <div class="flex items-center gap-1">
                <button
                  [disabled]="currentPage() === 0"
                  (click)="setPage(currentPage() - 1)"
                  class="px-2.5 py-1 rounded bg-slate-900 border border-slate-800 disabled:opacity-40 hover:text-white"
                >
                  Prev
                </button>
                <button
                  [disabled]="currentPage() >= totalPages() - 1"
                  (click)="setPage(currentPage() + 1)"
                  class="px-2.5 py-1 rounded bg-slate-900 border border-slate-800 disabled:opacity-40 hover:text-white"
                >
                  Next
                </button>
              </div>
            </div>
          }
        }
      </div>

      <!-- Upload Modal -->
      @if (showUploadModal()) {
        <app-upload-dialog
          (close)="showUploadModal.set(false)"
          (uploadComplete)="onUploadSuccess()"
        />
      }

      <!-- Quick Share Modal -->
      @if (selectedFileForShare()) {
        <app-create-share-modal
          [fileId]="selectedFileForShare()!.id"
          [fileName]="selectedFileForShare()!.originalName"
          (close)="selectedFileForShare.set(null)"
          (linkCreated)="loadOverview()"
        />
      }
    </div>
  `
})
export class DashboardComponent implements OnInit {
  private fileService = inject(FileService);
  private analyticsService = inject(AnalyticsService);
  private toast = inject(ToastService);

  files = signal<StoredFile[]>([]);
  overview = signal<AnalyticsOverview | null>(null);
  loading = signal(false);

  showUploadModal = signal(false);
  selectedFileForShare = signal<StoredFile | null>(null);

  searchQuery = '';
  sortOption = 'createdAt,desc';
  currentPage = signal(0);
  totalPages = signal(0);
  private searchTimeout: any;

  ngOnInit() {
    this.loadOverview();
    this.loadFiles();
  }

  loadOverview() {
    this.analyticsService.getOverview().subscribe({
      next: (data) => this.overview.set(data),
      error: () => {}
    });
  }

  loadFiles() {
    this.loading.set(true);
    this.fileService.listFiles(this.currentPage(), 10, this.sortOption, this.searchQuery).subscribe({
      next: (page) => {
        this.loading.set(false);
        this.files.set(page.content);
        this.totalPages.set(page.totalPages);
      },
      error: () => this.loading.set(false)
    });
  }

  onSearchChange() {
    clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => {
      this.currentPage.set(0);
      this.loadFiles();
    }, 300);
  }

  setPage(page: number) {
    this.currentPage.set(page);
    this.loadFiles();
  }

  openShareModal(file: StoredFile) {
    this.selectedFileForShare.set(file);
  }

  onUploadSuccess() {
    this.loadOverview();
    this.loadFiles();
  }

  deleteFile(file: StoredFile) {
    if (!confirm(`Are you sure you want to delete "${file.originalName}"? This will delete the file from cloud storage and remove all its share links.`)) {
      return;
    }

    this.fileService.deleteFile(file.id).subscribe({
      next: () => {
        this.toast.success(`"${file.originalName}" deleted`);
        this.loadOverview();
        this.loadFiles();
      },
      error: () => this.toast.error('Failed to delete file')
    });
  }
}
