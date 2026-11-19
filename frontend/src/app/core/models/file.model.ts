export interface StoredFile {
  id: string;
  originalName: string;
  contentType: string;
  sizeBytes: number;
  status: 'PENDING' | 'READY' | 'DELETED';
  downloadCount: number;
  createdAt: string;
}

export interface UploadUrlRequest {
  fileName: string;
  contentType?: string;
  sizeBytes: number;
}

export interface UploadUrlResponse {
  fileId: string;
  uploadUrl: string;
  headers: Record<string, string>;
}

export interface RenameFileRequest {
  fileName: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
