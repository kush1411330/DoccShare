export interface ShareLink {
  id: string;
  fileId: string;
  token: string;
  publicUrl: string;
  passwordProtected: boolean;
  expiresAt: string | null;
  maxDownloads: number | null;
  downloadCount: number;
  active: boolean;
  expired: boolean;
  createdAt: string;
}

export interface CreateShareRequest {
  password?: string;
  expiresAt?: string;
  maxDownloads?: number;
}

export interface UpdateShareRequest {
  active?: boolean;
  password?: string;
  clearPassword?: boolean;
  expiresAt?: string;
  clearExpiry?: boolean;
  maxDownloads?: number;
  clearMaxDownloads?: boolean;
}

export interface PublicShareMetadata {
  fileName: string;
  sizeBytes: number;
  contentType: string;
  passwordRequired: boolean;
  expired: boolean;
  maxDownloadsReached: boolean;
  expiresAt: string | null;
}

export interface DownloadRequest {
  password?: string;
}

export interface DownloadResponse {
  downloadUrl: string;
  expiresInSeconds: number;
}
