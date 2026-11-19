import { Component, EventEmitter, Input, Output, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ShareService } from '../../core/services/share.service';
import { ToastService } from '../../core/services/toast.service';
import { ShareLink } from '../../core/models/share.model';

@Component({
  selector: 'app-create-share-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-fade-in">
      <div class="w-full max-w-lg bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-2xl relative">
        <div class="flex items-center justify-between pb-4 border-b border-slate-800">
          <div class="flex items-center gap-2.5">
            <div class="w-8 h-8 rounded-lg bg-indigo-600/20 text-indigo-400 flex items-center justify-center border border-indigo-500/30">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"/></svg>
            </div>
            <div>
              <h3 class="text-lg font-semibold text-white">Create Share Link</h3>
              <p class="text-xs text-slate-400 truncate max-w-[320px]">{{ fileName }}</p>
            </div>
          </div>
          <button (click)="close.emit()" class="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition-colors">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
          </button>
        </div>

        @if (!createdLink()) {
          <form [formGroup]="form" (ngSubmit)="onSubmit()" class="my-6 space-y-5">
            <!-- Password Protection -->
            <div class="p-4 rounded-xl bg-slate-950/60 border border-slate-800 space-y-3">
              <div class="flex items-center justify-between">
                <div>
                  <div class="text-sm font-medium text-slate-200">Password Protection</div>
                  <div class="text-xs text-slate-400">Require recipients to enter a password</div>
                </div>
                <input
                  type="checkbox"
                  formControlName="enablePassword"
                  class="w-5 h-5 rounded border-slate-700 text-indigo-600 focus:ring-indigo-500 bg-slate-900 cursor-pointer"
                />
              </div>

              @if (form.get('enablePassword')?.value) {
                <div class="pt-2">
                  <input
                    type="password"
                    formControlName="password"
                    placeholder="Enter link password"
                    class="w-full px-3.5 py-2 text-sm bg-slate-900 border border-slate-700 rounded-lg text-white placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  />
                </div>
              }
            </div>

            <!-- Expiry Duration -->
            <div class="p-4 rounded-xl bg-slate-950/60 border border-slate-800 space-y-3">
              <div>
                <div class="text-sm font-medium text-slate-200">Link Expiry</div>
                <div class="text-xs text-slate-400">After this date, the link will stop working</div>
              </div>

              <div class="grid grid-cols-4 gap-2 pt-1">
                @for (preset of presets; track preset.label) {
                  <button
                    type="button"
                    (click)="selectPreset(preset.hours)"
                    [class.bg-indigo-600]="selectedPresetHours() === preset.hours"
                    [class.text-white]="selectedPresetHours() === preset.hours"
                    [class.bg-slate-900]="selectedPresetHours() !== preset.hours"
                    [class.text-slate-300]="selectedPresetHours() !== preset.hours"
                    class="py-1.5 px-2 text-xs font-medium rounded-lg border border-slate-700/80 hover:border-indigo-500/50 transition-colors"
                  >
                    {{ preset.label }}
                  </button>
                }
              </div>

              @if (selectedPresetHours() === -1) {
                <div class="pt-2">
                  <input
                    type="datetime-local"
                    formControlName="customExpiresAt"
                    class="w-full px-3.5 py-2 text-sm bg-slate-900 border border-slate-700 rounded-lg text-white focus:border-indigo-500 focus:outline-none"
                  />
                </div>
              }
            </div>

            <!-- Max Downloads -->
            <div class="p-4 rounded-xl bg-slate-950/60 border border-slate-800 space-y-3">
              <div class="flex items-center justify-between">
                <div>
                  <div class="text-sm font-medium text-slate-200">Download Limit</div>
                  <div class="text-xs text-slate-400">Automatically disable after N downloads</div>
                </div>
                <input
                  type="checkbox"
                  formControlName="enableMaxDownloads"
                  class="w-5 h-5 rounded border-slate-700 text-indigo-600 focus:ring-indigo-500 bg-slate-900 cursor-pointer"
                />
              </div>

              @if (form.get('enableMaxDownloads')?.value) {
                <div class="pt-2">
                  <input
                    type="number"
                    min="1"
                    formControlName="maxDownloads"
                    placeholder="e.g. 1, 5, 10"
                    class="w-full px-3.5 py-2 text-sm bg-slate-900 border border-slate-700 rounded-lg text-white placeholder-slate-500 focus:border-indigo-500 focus:outline-none"
                  />
                </div>
              }
            </div>

            <div class="flex justify-end gap-3 pt-2">
              <button
                type="button"
                (click)="close.emit()"
                class="px-4 py-2 text-sm text-slate-400 hover:text-white transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                [disabled]="loading()"
                class="px-5 py-2 text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl shadow-lg shadow-indigo-600/30 transition-all"
              >
                @if (loading()) {
                  <span>Creating...</span>
                } @else {
                  <span>Generate Link</span>
                }
              </button>
            </div>
          </form>
        } @else {
          <!-- Link Created Success State -->
          <div class="my-6 space-y-5 animate-fade-in text-center">
            <div class="w-12 h-12 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 flex items-center justify-center mx-auto">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>
            </div>
            <div>
              <h4 class="text-lg font-bold text-white">Your Share Link is Ready!</h4>
              <p class="text-xs text-slate-400 mt-1">Anyone with this link can view and download this file</p>
            </div>

            <div class="p-3 bg-slate-950 border border-slate-800 rounded-xl flex items-center justify-between gap-3 text-left">
              <span class="text-sm font-mono text-indigo-300 truncate select-all">{{ createdLink()?.publicUrl }}</span>
              <button
                (click)="copyLink(createdLink()!.publicUrl)"
                class="px-3.5 py-1.5 shrink-0 text-xs font-semibold rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white flex items-center gap-1.5 shadow-md shadow-indigo-600/20 transition-colors"
              >
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 5H6a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2v-1M8 5a2 2 0 002 2h2a2 2 0 002-2M8 5a2 2 0 012-2h2a2 2 0 012 2m0 0h2a2 2 0 012 2v3m2 4H10m0 0l3-3m-3 3l3 3"/></svg>
                <span>{{ copied() ? 'Copied!' : 'Copy' }}</span>
              </button>
            </div>

            <div class="grid grid-cols-3 gap-2 text-xs text-slate-400 text-left pt-2">
              <div class="p-2.5 rounded-lg bg-slate-950/60 border border-slate-800/80">
                <span class="text-slate-500 block">Password:</span>
                <span class="font-medium text-slate-300">{{ createdLink()?.passwordProtected ? 'Required' : 'None' }}</span>
              </div>
              <div class="p-2.5 rounded-lg bg-slate-950/60 border border-slate-800/80">
                <span class="text-slate-500 block">Expires:</span>
                <span class="font-medium text-slate-300">{{ createdLink()?.expiresAt ? (createdLink()?.expiresAt | date:'short') : 'Never' }}</span>
              </div>
              <div class="p-2.5 rounded-lg bg-slate-950/60 border border-slate-800/80">
                <span class="text-slate-500 block">Max Limit:</span>
                <span class="font-medium text-slate-300">{{ createdLink()?.maxDownloads ? createdLink()?.maxDownloads + ' downloads' : 'Unlimited' }}</span>
              </div>
            </div>

            <div class="pt-4 flex justify-end">
              <button
                (click)="onFinished()"
                class="px-5 py-2 text-sm font-medium text-slate-200 hover:text-white bg-slate-800 hover:bg-slate-700 rounded-xl transition-colors"
              >
                Done
              </button>
            </div>
          </div>
        }
      </div>
    </div>
  `
})
export class CreateShareModalComponent {
  private fb = inject(FormBuilder);
  private shareService = inject(ShareService);
  private toast = inject(ToastService);

  @Input({ required: true }) fileId!: string;
  @Input({ required: true }) fileName!: string;
  @Output() close = new EventEmitter<void>();
  @Output() linkCreated = new EventEmitter<ShareLink>();

  loading = signal(false);
  createdLink = signal<ShareLink | null>(null);
  copied = signal(false);
  selectedPresetHours = signal<number>(168); // 7 days default

  presets = [
    { label: '1 Day', hours: 24 },
    { label: '7 Days', hours: 168 },
    { label: '30 Days', hours: 720 },
    { label: 'Custom', hours: -1 },
  ];

  form = this.fb.group({
    enablePassword: [false],
    password: [''],
    enableMaxDownloads: [false],
    maxDownloads: [5, [Validators.min(1)]],
    customExpiresAt: ['']
  });

  selectPreset(hours: number) {
    this.selectedPresetHours.set(hours);
  }

  onSubmit() {
    this.loading.set(true);
    const val = this.form.value;

    let expiresAt: string | undefined = undefined;
    if (this.selectedPresetHours() > 0) {
      const date = new Date(Date.now() + this.selectedPresetHours() * 3600 * 1000);
      expiresAt = date.toISOString();
    } else if (this.selectedPresetHours() === -1 && val.customExpiresAt) {
      expiresAt = new Date(val.customExpiresAt).toISOString();
    }

    const payload = {
      password: val.enablePassword && val.password ? val.password : undefined,
      expiresAt,
      maxDownloads: val.enableMaxDownloads && val.maxDownloads ? val.maxDownloads : undefined
    };

    this.shareService.createShare(this.fileId, payload).subscribe({
      next: (link) => {
        this.loading.set(false);
        this.createdLink.set(link);
        this.linkCreated.emit(link);
      },
      error: (err) => {
        this.loading.set(false);
        this.toast.error(err.error?.message || 'Failed to create share link');
      }
    });
  }

  copyLink(url: string) {
    navigator.clipboard.writeText(url).then(() => {
      this.copied.set(true);
      this.toast.success('Link copied to clipboard!');
      setTimeout(() => this.copied.set(false), 2000);
    });
  }

  onFinished() {
    this.close.emit();
  }
}
