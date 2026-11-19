import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AnalyticsOverview, FileAnalytics } from '../models/analytics.model';

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private http = inject(HttpClient);

  getOverview(): Observable<AnalyticsOverview> {
    return this.http.get<AnalyticsOverview>(`${environment.apiBaseUrl}/analytics/overview`);
  }

  getFileAnalytics(fileId: string, from?: string, to?: string): Observable<FileAnalytics> {
    let url = `${environment.apiBaseUrl}/analytics/files/${fileId}`;
    const params = [];
    if (from) params.push(`from=${encodeURIComponent(from)}`);
    if (to) params.push(`to=${encodeURIComponent(to)}`);
    if (params.length > 0) url += `?${params.join('&')}`;
    return this.http.get<FileAnalytics>(url);
  }

  getShareAnalytics(shareId: string, from?: string, to?: string): Observable<FileAnalytics> {
    let url = `${environment.apiBaseUrl}/analytics/shares/${shareId}`;
    const params = [];
    if (from) params.push(`from=${encodeURIComponent(from)}`);
    if (to) params.push(`to=${encodeURIComponent(to)}`);
    if (params.length > 0) url += `?${params.join('&')}`;
    return this.http.get<FileAnalytics>(url);
  }
}
