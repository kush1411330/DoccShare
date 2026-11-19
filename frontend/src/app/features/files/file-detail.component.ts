import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { FileService } from '../../core/services/file.service';
import { ToastService } from '../../core/services/toast.service';
import { StoredFile } from '../../core/models/file.model';
import { FileSizePipe } from '../../shared/pipes/file-size.pipe';
import { SharesListComponent } from '../share/shares-list.component';
import { FileAnalyticsViewComponent } from '../analytics/file-analytics-view.component';

@Component({
  selector: 'app-file-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, FileSizePipe, SharesListComponent, FileAnalyticsViewComponent],
  template: `
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      <!-- Back button -->
      <a routerLink="/dashboard" class="inline-flex items-center gap-2 text-sm text-slate-400 hover:text-white transition-colors">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"/></svg>
        <span>Back to Files</span>
      </a>

      @if (loading()) {
        <div class="py-16 text-center text-slate-400">
          <svg class="animate-spin h-6 w-6 mx-auto text-indigo-400 mb-2" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          Loading file details...
        </div>
      } @else if (file()) {
        <!-- Header Info Card -->
        <div class="p-6 rounded-2xl bg-slate-900/70 border border-slate-800 backdrop-blur-xl shadow-xl flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div class="flex items-start gap-4">
            <div class="w-12 h-12 rounded-2xl bg-indigo-600/20 text-indigo-400 flex items-center justify-center border border-indigo-500/30 shrink-0">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
            </div>
            <div class="space-y-1">
              @if (editingName()) {
                <div class="flex items-center gap-2">
                  <input
                    type="text"
                    [(ngModel)]="newName"
                    class="px-3 py-1 text-base bg-slate-950 border border-slate-700 rounded-lg text-white focus:border-indigo-500 focus:outline-none"
                  />
                  <button (click)="saveRename()" class="p-1 text-emerald-400 hover:text-emerald-300">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>
                  </button>
                  <button (click)="editingName.set(false)" class="p-1 text-slate-400 hover:text-white">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
                  </button>
                </div>
              } @else {
                <div class="flex items-center gap-2">
                  <h2 class="text-xl font-bold text-white tracking-tight">{{ file()?.originalName }}</h2>
                  <button (click)="startRename()" class="text-slate-400 hover:text-white p-1" title="Rename file">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"/></svg>
                  </button>
                </div>
              }
              <div class="flex items-center gap-4 text-xs text-slate-400">
                <span>{{ file()?.sizeBytes | fileSize }}</span>
                <span>•</span>
                <span>{{ file()?.contentType }}</span>
                <span>•</span>
                <span>Uploaded {{ file()?.createdAt | date:'mediumDate' }}</span>
              </div>
            </div>
          </div>

          <div class="flex items-center gap-3">
            <button
              (click)="deleteFile()"
              class="px-4 py-2 text-xs font-medium text-rose-300 hover:text-white bg-rose-500/10 hover:bg-rose-500/20 rounded-xl border border-rose-500/30 transition-colors flex items-center gap-2"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
              <span>Delete File</span>
            </button>
          </div>
        </div>

        <!-- Navigation Tabs -->
        <div class="flex items-center gap-2 border-b border-slate-800">
          <button
            (click)="activeTab.set('shares')"
            [class.border-indigo-500]="activeTab() === 'shares'"
            [class.text-indigo-400]="activeTab() === 'shares'"
            [class.border-transparent]="activeTab() !== 'shares'"
            [class.text-slate-400]="activeTab() !== 'shares'"
            class="pb-3 px-4 text-sm font-medium border-b-2 hover:text-white transition-colors flex items-center gap-2"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"/></svg>
            <span>Share Links</span>
          </button>
          <button
            (click)="activeTab.set('analytics')"
            [class.border-indigo-500]="activeTab() === 'analytics'"
            [class.text-indigo-400]="activeTab() === 'analytics'"
            [class.border-transparent]="activeTab() !== 'analytics'"
            [class.text-slate-400]="activeTab() !== 'analytics'"
            class="pb-3 px-4 text-sm font-medium border-b-2 hover:text-white transition-colors flex items-center gap-2"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/></svg>
            <span>Analytics</span>
          </button>
        </div>

        <!-- Tab Body -->
        @if (activeTab() === 'shares') {
          <app-shares-list [fileId]="file()!.id" [fileName]="file()!.originalName" />
        } @else {
          <app-file-analytics-view [fileId]="file()!.id" />
        }
      }
    </div>
  `
})
export class FileDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fileService = inject(FileService);
  private toast = inject(ToastService);

  file = signal<StoredFile | null>(null);
  loading = signal(false);
  activeTab = signal<'shares' | 'analytics'>('shares');

  editingName = signal(false);
  newName = '';

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadFile(id);
    }
  }

  loadFile(id: string) {
    this.loading.set(true);
    this.fileService.getFile(id).subscribe({
      next: (f) => {
        this.loading.set(false);
        this.file.set(f);
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('File not found or access denied');
        this.router.navigate(['/dashboard']);
      }
    });
  }

  startRename() {
    if (this.file()) {
      this.newName = this.file()!.originalName;
      this.editingName.set(true);
    }
  }

  saveRename() {
    if (!this.file() || !this.newName.trim()) return;

    this.fileService.renameFile(this.file()!.id, this.newName.trim()).subscribe({
      next: (updated) => {
        this.file.set(updated);
        this.editingName.set(false);
        this.toast.success('File renamed successfully');
      },
      error: () => this.toast.error('Failed to rename file')
    });
  }

  deleteFile() {
    if (!confirm('Are you sure you want to delete this file and all its share links and analytics? This cannot be undone.')) {
      return;
    }

    if (this.file()) {
      this.fileService.deleteFile(this.file()!.id).subscribe({
        next: () => {
          this.toast.success('File deleted successfully');
          this.router.navigate(['/dashboard']);
        },
        error: () => this.toast.error('Failed to delete file')
      });
    }
  }
}
