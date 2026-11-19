export interface AnalyticsOverview {
  totalFiles: number;
  totalLinks: number;
  totalDownloads: number;
  uniqueVisitors30Days: number;
  downloads30Days: number;
}

export interface TimeSeriesItem {
  date: string;
  count: number;
}

export interface GeoItem {
  countryCode: string;
  countryName: string;
  count: number;
}

export interface BreakdownItem {
  name: string;
  count: number;
}

export interface RecentDownload {
  id: string;
  downloadedAt: string;
  countryCode: string;
  countryName: string;
  city: string;
  browser: string;
  os: string;
  deviceType: string;
  referrer: string;
}

export interface FileAnalytics {
  totalDownloads: number;
  uniqueVisitors: number;
  timeSeries: TimeSeriesItem[];
  countries: GeoItem[];
  browsers: BreakdownItem[];
  operatingSystems: BreakdownItem[];
  devices: BreakdownItem[];
  recent: RecentDownload[];
}
