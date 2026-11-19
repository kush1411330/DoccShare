import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpEvent, HttpEventType, HttpHeaders, HttpRequest } from '@angular/common/http';
import { Observable, filter, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page, RenameFileRequest, StoredFile, UploadUrlRequest, UploadUrlResponse } from '../models/file.model';

export interface UploadProgress {
  progress: number;
  completed: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class FileService {
  private http = inject(HttpClient);

  requestUploadUrl(data: UploadUrlRequest): Observable<UploadUrlResponse> {
    return this.http.post<UploadUrlResponse>(`${environment.apiBaseUrl}/files/upload-url`, data);
  }

  uploadToStorage(uploadUrl: string, file: File, headersMap: Record<string, string> = {}): Observable<UploadProgress> {
    let headers = new HttpHeaders();
    for (const [key, val] of Object.entries(headersMap)) {
      headers = headers.set(key, val);
    }
    if (!headers.has('Content-Type') && file.type) {
      headers = headers.set('Content-Type', file.type);
    }

    const req = new HttpRequest('PUT', uploadUrl, file, {
      headers,
      reportProgress: true
    });

    return this.http.request(req).pipe(
      filter(event => event.type === HttpEventType.UploadProgress || event.type === HttpEventType.Response),
      map((event: HttpEvent<unknown>) => {
        if (event.type === HttpEventType.UploadProgress) {
          const total = event.total || file.size;
          const progress = Math.round((100 * event.loaded) / total);
          return { progress, completed: false };
        } else if (event.type === HttpEventType.Response) {
          return { progress: 100, completed: true };
        }
        return { progress: 0, completed: false };
      })
    );
  }

  confirmUpload(fileId: string): Observable<StoredFile> {
    return this.http.post<StoredFile>(`${environment.apiBaseUrl}/files/${fileId}/confirm`, {});
  }

  listFiles(page: number = 0, size: number = 10, sort: string = 'createdAt,desc', query?: string): Observable<Page<StoredFile>> {
    let url = `${environment.apiBaseUrl}/files?page=${page}&size=${size}&sort=${sort}`;
    if (query && query.trim()) {
      url += `&q=${encodeURIComponent(query.trim())}`;
    }
    return this.http.get<Page<StoredFile>>(url);
  }

  getFile(id: string): Observable<StoredFile> {
    return this.http.get<StoredFile>(`${environment.apiBaseUrl}/files/${id}`);
  }

  renameFile(id: string, fileName: string): Observable<StoredFile> {
    const body: RenameFileRequest = { fileName };
    return this.http.patch<StoredFile>(`${environment.apiBaseUrl}/files/${id}`, body);
  }

  deleteFile(id: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiBaseUrl}/files/${id}`);
  }
}
