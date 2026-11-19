import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ShareService } from '../../core/services/share.service';
import { ToastService } from '../../core/services/toast.service';
import { ShareLink } from '../../core/models/share.model';
import { CreateShareModalComponent } from './create-share-modal.component';

@Component({
  selector: 'app-shares-list',
  standalone: true,
  imports: [CommonModule, CreateShareModalComponent],
  template: `
    <div class="space-y-4">
      <div class="flex items-center justify-between">
        <div>
          <h4 class="text-base font-semibold text-white">Share Links</h4>
          <p class="text-xs text-slate-400">Manage individual links created for this file</p>
        </div>
        <button
          (click)="showCreateModal.set(true)"
          class="px-3.5 py-1.5 text-xs font-semibold rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white flex items-center gap-1.5 shadow-md shadow-indigo-600/20 transition-all hover:scale-[1.02]"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
          <span>New Link</span>
        </button>
      </div>

      @if (loading()) {
        <div class="py-8 text-center text-slate-400 text-sm">
          <svg class="animate-spin h-5 w-5 mx-auto text-indigo-400 mb-2" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          Loading links...
        </div>
      } @else if (shares().length === 0) {
        <div class="p-8 text-center bg-slate-900/40 border border-slate-800 rounded-2xl">
          <div class="w-10 h-10 rounded-xl bg-slate-800 flex items-center justify-center text-slate-400 mx-auto mb-2">
            <svg class="w-5 h-5 text-indigo-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"/></svg>
          </div>
          <p class="text-sm font-medium text-slate-300">No share links yet</p>
          <p class="text-xs text-slate-500 mt-1">Create your first link to send this file to someone</p>
          <button
            (click)="showCreateModal.set(true)"
            class="mt-4 px-4 py-2 text-xs font-medium rounded-lg bg-indigo-600/20 text-indigo-400 hover:bg-indigo-600/30 border border-indigo-500/30 transition-colors"
          >
            Create Share Link
          </button>
        </div>
      } @else {
        <div class="space-y-3">
          @for (share of shares(); track share.id) {
            <div
              class="p-4 rounded-xl border transition-all flex flex-col sm:flex-row sm:items-center justify-between gap-3"
              [ngClass]="{
                'bg-slate-900/60 border-slate-800': share.active && !share.expired,
                'bg-slate-950/40 border-slate-900 opacity-60': !share.active || share.expired
              }"
            >
              <div class="space-y-1.5 flex-1 min-w-0">
                <div class="flex items-center gap-2 flex-wrap">
                  <span class="font-mono text-sm text-indigo-400 font-medium truncate max-w-[240px]">{{ share.publicUrl }}</span>
                  <button
                    (click)="copyLink(share.publicUrl)"
                    class="p-1 text-slate-400 hover:text-white rounded hover:bg-slate-800 transition-colors"
                    title="Copy Link"
                  >
                    <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 5H6a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2v-1M8 5a2 2 0 002 2h2a2 2 0 002-2M8 5a2 2 0 012-2h2a2 2 0 012 2m0 0h2a2 2 0 012 2v3m2 4H10m0 0l3-3m-3 3l3 3"/></svg>
                  </button>

                  @if (share.passwordProtected) {
                    <span class="px-2 py-0.5 text-[10px] font-semibold rounded-md bg-amber-500/10 text-amber-300 border border-amber-500/20">Password</span>
                  }
                  @if (share.expired) {
                    <span class="px-2 py-0.5 text-[10px] font-semibold rounded-md bg-rose-500/10 text-rose-300 border border-rose-500/20">Expired</span>
                  }
                  @if (!share.active) {
                    <span class="px-2 py-0.5 text-[10px] font-semibold rounded-md bg-slate-800 text-slate-400">Inactive</span>
                  }
                </div>

                <div class="flex items-center gap-4 text-xs text-slate-400 flex-wrap">
                  <span>Downloads: <strong class="text-slate-200">{{ share.downloadCount }}</strong>@if (share.maxDownloads) { / {{ share.maxDownloads }}}</span>
                  <span>Expires: <strong class="text-slate-200">{{ share.expiresAt ? (share.expiresAt | date:'mediumDate') : 'Never' }}</strong></span>
                  <span>Created: {{ share.createdAt | date:'shortDate' }}</span>
                </div>
              </div>

              <div class="flex items-center gap-2 shrink-0 self-end sm:self-center">
                <!-- Toggle Active -->
                <button
                  (click)="toggleActive(share)"
                  [title]="share.active ? 'Disable Link' : 'Enable Link'"
                  class="px-2.5 py-1 text-xs font-medium rounded-lg border transition-colors"
                  [ngClass]="share.active ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300 hover:bg-emerald-500/20' : 'bg-slate-800 border-slate-700 text-slate-400 hover:text-white'"
                >
                  {{ share.active ? 'Active' : 'Disabled' }}
                </button>

                <!-- Revoke button -->
                <button
                  (click)="revokeShare(share.id)"
                  class="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg border border-transparent hover:border-rose-500/20 transition-colors"
                  title="Revoke and delete this link"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
                </button>
              </div>
            </div>
          }
        </div>
      }

      @if (showCreateModal()) {
        <app-create-share-modal
          [fileId]="fileId"
          [fileName]="fileName"
          (close)="showCreateModal.set(false)"
          (linkCreated)="onLinkCreated($event)"
        />
      }
    </div>
  `
})
export class SharesListComponent implements OnInit {
  private shareService = inject(ShareService);
  private toast = inject(ToastService);

  @Input({ required: true }) fileId!: string;
  @Input({ required: true }) fileName!: string;
  @Output() countChange = new EventEmitter<number>();

  shares = signal<ShareLink[]>([]);
  loading = signal(false);
  showCreateModal = signal(false);

  ngOnInit() {
    this.loadShares();
  }

  loadShares() {
    this.loading.set(true);
    this.shareService.listShares(this.fileId).subscribe({
      next: (list) => {
        this.loading.set(false);
        this.shares.set(list);
        this.countChange.emit(list.length);
      },
      error: () => this.loading.set(false)
    });
  }

  toggleActive(share: ShareLink) {
    const newActive = !share.active;
    this.shareService.updateShare(share.id, { active: newActive }).subscribe({
      next: (updated) => {
        this.shares.update(list => list.map(s => s.id === updated.id ? updated : s));
        this.toast.success(newActive ? 'Share link activated' : 'Share link disabled');
      },
      error: () => this.toast.error('Failed to update share link')
    });
  }

  revokeShare(shareId: string) {
    if (!confirm('Are you sure you want to permanently revoke this link? Anyone with this URL will immediately lose access.')) {
      return;
    }

    this.shareService.revokeShare(shareId).subscribe({
      next: () => {
        this.shares.update(list => list.filter(s => s.id !== shareId));
        this.toast.success('Share link revoked.');
        this.countChange.emit(this.shares().length);
      },
      error: () => this.toast.error('Failed to revoke link')
    });
  }

  copyLink(url: string) {
    navigator.clipboard.writeText(url).then(() => {
      this.toast.success('Link copied to clipboard!');
    });
  }

  onLinkCreated(newLink: ShareLink) {
    this.shares.update(list => [newLink, ...list]);
    this.countChange.emit(this.shares().length);
  }
}
