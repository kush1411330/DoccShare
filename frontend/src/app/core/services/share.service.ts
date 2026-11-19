import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CreateShareRequest,
  DownloadRequest,
  DownloadResponse,
  PublicShareMetadata,
  ShareLink,
  UpdateShareRequest
} from '../models/share.model';

@Injectable({
  providedIn: 'root'
})
export class ShareService {
  private http = inject(HttpClient);

  createShare(fileId: string, data: CreateShareRequest): Observable<ShareLink> {
    return this.http.post<ShareLink>(`${environment.apiBaseUrl}/files/${fileId}/shares`, data);
  }

  listShares(fileId: string): Observable<ShareLink[]> {
    return this.http.get<ShareLink[]>(`${environment.apiBaseUrl}/files/${fileId}/shares`);
  }

  updateShare(shareId: string, data: UpdateShareRequest): Observable<ShareLink> {
    return this.http.patch<ShareLink>(`${environment.apiBaseUrl}/shares/${shareId}`, data);
  }

  revokeShare(shareId: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiBaseUrl}/shares/${shareId}`);
  }

  getPublicMetadata(token: string): Observable<PublicShareMetadata> {
    return this.http.get<PublicShareMetadata>(`${environment.apiBaseUrl}/public/shares/${token}`);
  }

  downloadPublicShare(token: string, password?: string): Observable<DownloadResponse> {
    const body: DownloadRequest = { password };
    return this.http.post<DownloadResponse>(`${environment.apiBaseUrl}/public/shares/${token}/download`, body);
  }
}
