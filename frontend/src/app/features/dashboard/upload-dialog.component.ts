import { Component, EventEmitter, Output, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { FileService } from '../../core/services/file.service';
import { ToastService } from '../../core/services/toast.service';
import { FileSizePipe } from '../../shared/pipes/file-size.pipe';

@Component({
  selector: 'app-upload-dialog',
  standalone: true,
  imports: [CommonModule, FileSizePipe],
  template: `
    <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-fade-in">
      <div class="w-full max-w-lg bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-2xl relative overflow-hidden">
        <div class="flex items-center justify-between pb-4 border-b border-slate-800">
          <div class="flex items-center gap-2.5">
            <div class="w-8 h-8 rounded-lg bg-indigo-600/20 text-indigo-400 flex items-center justify-center border border-indigo-500/30">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"/></svg>
            </div>
            <h3 class="text-lg font-semibold text-white">Upload File</h3>
          </div>
          @if (!uploading()) {
            <button (click)="close.emit()" class="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition-colors">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
          }
        </div>

        @if (!uploading()) {
          <div
            (dragover)="onDragOver($event)"
            (dragleave)="onDragLeave($event)"
            (drop)="onDrop($event)"
            [class.border-indigo-500]="isDragging()"
            [class.bg-indigo-500/5]="isDragging()"
            class="my-6 border-2 border-dashed border-slate-700 hover:border-slate-600 rounded-2xl p-8 text-center transition-all cursor-pointer flex flex-col items-center justify-center gap-3"
            (click)="fileInput.click()"
          >
            <input #fileInput type="file" (change)="onFileSelected($event)" class="hidden" />
            <div class="w-14 h-14 rounded-2xl bg-slate-800 flex items-center justify-center text-slate-400 group-hover:scale-105 transition-transform">
              <svg class="w-7 h-7 text-indigo-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"/>
              </svg>
            </div>
            <div>
              <p class="text-sm font-medium text-slate-200">
                <span class="text-indigo-400 hover:underline">Click to browse</span> or drag and drop a file
              </p>
              <p class="text-xs text-slate-400 mt-1">Direct upload to cloud storage (up to 100 MB)</p>
            </div>
          </div>
        } @else {
          <div class="my-6 space-y-4">
            <div class="flex items-center justify-between text-sm">
              <span class="font-medium text-slate-200 truncate max-w-[280px]">{{ currentFileName() }}</span>
              <span class="text-xs font-semibold text-indigo-400">{{ progress() }}%</span>
            </div>

            <!-- Progress bar -->
            <div class="w-full bg-slate-800 rounded-full h-3 overflow-hidden p-0.5">
              <div
                class="bg-gradient-to-r from-indigo-500 to-sky-400 h-full rounded-full transition-all duration-200"
                [style.width.%]="progress()"
              ></div>
            </div>

            <div class="flex items-center justify-between text-xs text-slate-400">
              <span>{{ statusMessage() }}</span>
              <span>{{ currentFileSize() | fileSize }}</span>
            </div>

            <div class="pt-3 flex justify-end">
              <button
                (click)="cancelUpload()"
                class="px-4 py-2 text-xs font-medium text-rose-300 hover:text-white bg-rose-500/10 hover:bg-rose-500/20 rounded-lg border border-rose-500/30 transition-colors"
              >
                Cancel Upload
              </button>
            </div>
          </div>
        }

        <div class="flex items-center justify-end gap-3 pt-3 border-t border-slate-800 text-xs text-slate-400">
          <span>Files go straight to cloud storage, never touching our servers directly</span>
        </div>
      </div>
    </div>
  `
})
export class UploadDialogComponent {
  private fileService = inject(FileService);
  private toast = inject(ToastService);

  @Output() close = new EventEmitter<void>();
  @Output() uploadComplete = new EventEmitter<void>();

  isDragging = signal(false);
  uploading = signal(false);
  progress = signal(0);
  statusMessage = signal('Preparing upload...');
  currentFileName = signal('');
  currentFileSize = signal(0);

  private uploadSub?: Subscription;

  onDragOver(e: DragEvent) {
    e.preventDefault();
    e.stopPropagation();
    this.isDragging.set(true);
  }

  onDragLeave(e: DragEvent) {
    e.preventDefault();
    e.stopPropagation();
    this.isDragging.set(false);
  }

  onDrop(e: DragEvent) {
    e.preventDefault();
    e.stopPropagation();
    this.isDragging.set(false);
    if (e.dataTransfer && e.dataTransfer.files.length > 0) {
      this.startUpload(e.dataTransfer.files[0]);
    }
  }

  onFileSelected(e: Event) {
    const input = e.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.startUpload(input.files[0]);
    }
  }

  startUpload(file: File) {
    const maxBytes = 100 * 1024 * 1024;
    if (file.size > maxBytes) {
      this.toast.error('File size cannot exceed 100 MB', 'File Too Large');
      return;
    }

    this.uploading.set(true);
    this.progress.set(0);
    this.currentFileName.set(file.name);
    this.currentFileSize.set(file.size);
    this.statusMessage.set('Requesting presigned upload URL...');

    // Step 1: Request presigned PUT URL
    this.fileService.requestUploadUrl({
      fileName: file.name,
      contentType: file.type || 'application/octet-stream',
      sizeBytes: file.size
    }).subscribe({
      next: (res) => {
        this.statusMessage.set('Uploading directly to cloud storage...');
        // Step 2: Upload directly to S3
        this.uploadSub = this.fileService.uploadToStorage(res.uploadUrl, file, res.headers).subscribe({
          next: (prog) => {
            this.progress.set(prog.progress);
            if (prog.completed) {
              this.statusMessage.set('Verifying upload integrity...');
              // Step 3: Confirm upload to backend
              this.fileService.confirmUpload(res.fileId).subscribe({
                next: () => {
                  this.toast.success(`${file.name} uploaded successfully!`, 'Upload Complete');
                  this.uploadComplete.emit();
                  this.close.emit();
                },
                error: (err) => {
                  this.uploading.set(false);
                  this.toast.error(err.error?.message || 'Failed to verify uploaded file');
                }
              });
            }
          },
          error: (err) => {
            this.uploading.set(false);
            this.toast.error('Cloud storage upload failed. Please check network/CORS settings.');
          }
        });
      },
      error: (err) => {
        this.uploading.set(false);
        this.toast.error(err.error?.message || 'Failed to initiate upload');
      }
    });
  }

  cancelUpload() {
    if (this.uploadSub) {
      this.uploadSub.unsubscribe();
    }
    this.uploading.set(false);
    this.progress.set(0);
    this.toast.info('Upload cancelled.');
  }
}
