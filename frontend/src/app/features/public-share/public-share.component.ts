import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ShareService } from '../../core/services/share.service';
import { PublicShareMetadata } from '../../core/models/share.model';
import { FileSizePipe } from '../../shared/pipes/file-size.pipe';

@Component({
  selector: 'app-public-share',
  standalone: true,
  imports: [CommonModule, FormsModule, FileSizePipe],
  template: `
    <div class="min-h-screen bg-slate-950 text-slate-100 flex flex-col justify-between p-4 selection:bg-indigo-500 selection:text-white">
      <!-- Minimalist Public Header -->
      <header class="max-w-4xl mx-auto w-full py-6 flex items-center justify-between">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-xl bg-gradient-to-tr from-indigo-600 to-sky-400 p-0.5 shadow-lg shadow-indigo-500/20">
            <div class="w-full h-full bg-slate-950 rounded-[10px] flex items-center justify-center">
              <svg class="w-4 h-4 text-indigo-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"/></svg>
            </div>
          </div>
          <span class="text-lg font-bold bg-gradient-to-r from-white to-slate-400 bg-clip-text text-transparent">CloudShare</span>
        </div>
        <div class="text-xs text-slate-500">Secure Direct Download</div>
      </header>

      <!-- Center Container -->
      <main class="max-w-md mx-auto w-full my-auto">
        <div class="bg-slate-900/80 border border-slate-800 rounded-3xl p-8 backdrop-blur-xl shadow-2xl relative overflow-hidden text-center">
          <!-- Glow background accents -->
          <div class="absolute -top-24 -left-24 w-48 h-48 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none"></div>
          <div class="absolute -bottom-24 -right-24 w-48 h-48 bg-sky-500/10 rounded-full blur-3xl pointer-events-none"></div>

          @if (loading()) {
            <!-- Loading State -->
            <div class="py-12 space-y-3">
              <svg class="animate-spin h-8 w-8 mx-auto text-indigo-500" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              <p class="text-sm text-slate-400">Verifying share link...</p>
            </div>
          } @else if (notFound()) {
            <!-- 404 / Inactive State -->
            <div class="py-6 space-y-4">
              <div class="w-16 h-16 rounded-2xl bg-rose-500/10 text-rose-400 border border-rose-500/20 flex items-center justify-center mx-auto">
                <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>
              </div>
              <h2 class="text-xl font-bold text-white">Link Not Available</h2>
              <p class="text-sm text-slate-400 leading-relaxed">
                This share link does not exist, has been revoked by the owner, or is no longer active.
              </p>
            </div>
          } @else if (meta()?.expired) {
            <!-- Expired State -->
            <div class="py-6 space-y-4">
              <div class="w-16 h-16 rounded-2xl bg-amber-500/10 text-amber-400 border border-amber-500/20 flex items-center justify-center mx-auto">
                <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
              </div>
              <h2 class="text-xl font-bold text-white">Share Link Expired</h2>
              <p class="text-sm text-slate-400 leading-relaxed">
                This link was set to expire on {{ meta()?.expiresAt | date:'medium' }} and can no longer be downloaded.
              </p>
            </div>
          } @else if (meta()?.maxDownloadsReached) {
            <!-- Max Downloads Reached State -->
            <div class="py-6 space-y-4">
              <div class="w-16 h-16 rounded-2xl bg-rose-500/10 text-rose-400 border border-rose-500/20 flex items-center justify-center mx-auto">
                <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"/></svg>
              </div>
              <h2 class="text-xl font-bold text-white">Download Limit Reached</h2>
              <p class="text-sm text-slate-400 leading-relaxed">
                This file has already reached its maximum allowed download count.
              </p>
            </div>
          } @else {
            <!-- File Info & Download Area -->
            <div class="space-y-6">
              <div class="w-16 h-16 rounded-2xl bg-indigo-600/20 text-indigo-400 border border-indigo-500/30 flex items-center justify-center mx-auto shadow-inner">
                <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
              </div>

              <div>
                <h2 class="text-xl font-bold text-white tracking-tight break-words">{{ meta()?.fileName }}</h2>
                <div class="flex items-center justify-center gap-2 text-xs text-slate-400 mt-1">
                  <span>{{ meta()?.sizeBytes | fileSize }}</span>
                  <span>•</span>
                  <span>{{ meta()?.contentType }}</span>
                </div>
              </div>

              <!-- Password Required Form -->
              @if (meta()?.passwordRequired) {
                <div class="p-4 rounded-2xl bg-slate-950/60 border border-slate-800 text-left space-y-2">
                  <label class="block text-xs font-semibold uppercase tracking-wider text-slate-300">Password Required</label>
                  <p class="text-xs text-slate-400">The sender protected this file with a password.</p>
                  <input
                    type="password"
                    [(ngModel)]="password"
                    placeholder="Enter password to download"
                    class="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-white placeholder-slate-500 text-sm focus:outline-none focus:border-indigo-500 transition-colors mt-2"
                  />
                  @if (passwordError()) {
                    <span class="text-xs text-rose-400 block pt-1">{{ passwordError() }}</span>
                  }
                </div>
              }

              <!-- Download Button -->
              <button
                (click)="onDownload()"
                [disabled]="downloading()"
                class="w-full py-3.5 px-6 rounded-2xl font-semibold text-sm text-white bg-gradient-to-r from-indigo-600 to-indigo-500 hover:from-indigo-500 hover:to-indigo-400 shadow-xl shadow-indigo-600/30 transition-all hover:scale-[1.02] flex items-center justify-center gap-2.5 disabled:opacity-50"
              >
                @if (downloading()) {
                  <svg class="animate-spin h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  <span>Generating secure download...</span>
                } @else {
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"/></svg>
                  <span>Download File</span>
                }
              </button>

              <div class="text-[11px] text-slate-500 space-y-1">
                <p>Delivered securely via encrypted presigned storage URL</p>
                @if (meta()?.expiresAt) {
                  <p>Link expires on {{ meta()?.expiresAt | date:'medium' }}</p>
                }
              </div>
            </div>
          }
        </div>
      </main>

      <!-- Footer -->
      <footer class="max-w-4xl mx-auto w-full py-4 text-center text-xs text-slate-600">
        CloudShare • Fast, Secure & Ephemeral File Sharing
      </footer>
    </div>
  `
})
export class PublicShareComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private shareService = inject(ShareService);

  token = '';
  loading = signal(true);
  notFound = signal(false);
  meta = signal<PublicShareMetadata | null>(null);

  password = '';
  passwordError = signal<string | null>(null);
  downloading = signal(false);

  ngOnInit() {
    this.token = this.route.snapshot.paramMap.get('token') || '';
    if (this.token) {
      this.loadMetadata();
    } else {
      this.loading.set(false);
      this.notFound.set(true);
    }
  }

  loadMetadata() {
    this.loading.set(true);
    this.shareService.getPublicMetadata(this.token).subscribe({
      next: (m) => {
        this.loading.set(false);
        this.meta.set(m);
      },
      error: () => {
        this.loading.set(false);
        this.notFound.set(true);
      }
    });
  }

  onDownload() {
    if (this.meta()?.passwordRequired && !this.password) {
      this.passwordError.set('Please enter the password');
      return;
    }

    this.downloading.set(true);
    this.passwordError.set(null);

    this.shareService.downloadPublicShare(this.token, this.password || undefined).subscribe({
      next: (res) => {
        this.downloading.set(false);
        // Direct browser to the presigned S3 download URL
        window.location.href = res.downloadUrl;
      },
      error: (err) => {
        this.downloading.set(false);
        if (err.status === 401) {
          this.passwordError.set('Incorrect password. Please try again.');
        } else {
          this.passwordError.set(err.error?.message || 'Download failed. The link may have reached its limit.');
        }
      }
    });
  }
}
