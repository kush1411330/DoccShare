# CloudShare

CloudShare is a high-performance, secure file-sharing web platform engineered with Java Spring Boot 3, Angular 18+, PostgreSQL, and S3-compatible cloud object storage. It empowers users to upload files directly to cloud storage via presigned URLs, generate unguessable share links equipped with optional password protection, expiration schedules, and strict download quotas, and monitor comprehensive download analytics—including geographic locations, client browsers, operating systems, and device breakdowns—while preserving visitor privacy through salted cryptographic hashing.

---

## Architecture Overview

CloudShare employs a stateless, horizontally scalable decoupled architecture:

```
┌──────────────────┐           HTTPS          ┌──────────────────────────┐
│   Angular SPA    │ ───────────────────────▶ │   Spring Boot 3 REST     │
│  (Nginx Alpine)  │                          │    (Java 21 / Spring 6)  │
└──────────────────┘                          └─────────────┬────────────┘
         │                                                  │
         │ (Direct Presigned PUT/GET)                       │ JDBC / Flyway
         ▼                                                  ▼
┌──────────────────┐                          ┌──────────────────────────┐
│  Object Storage  │                          │       PostgreSQL 16      │
│ (MinIO / S3 / R2)│                          │      Database Cluster    │
└──────────────────┘                          └──────────────────────────┘
```

### Core Architecture Highlights

1. **Direct-to-Storage Presigned Uploads**: Files never traverse the application server memory during upload. The client requests a cryptographically signed presigned PUT URL from the Spring Boot API, streams the file directly to object storage with realtime progress reporting, and notifies the backend upon completion for validation.
2. **Atomic Concurrency Protection**: High-concurrency downloads are strictly bounded by single-query atomic SQL updates (`UPDATE share_links SET download_count = download_count + 1 WHERE ... AND (max_downloads IS NULL OR download_count < max_downloads)`), eliminating race conditions when download limits are reached.
3. **Privacy-Preserving Telemetry**: Visitor IP addresses are immediately hashed using SHA-256 combined with a server-side salt before persistence. Raw IP addresses are never retained in database records.
4. **Asynchronous Telemetry Enrichment**: User-Agent parsing and MaxMind GeoIP lookups execute on a dedicated background thread pool (`@Async`), guaranteeing sub-millisecond public download response latencies.

---

## Tech Stack

| Domain | Technology & Framework | Purpose |
|---|---|---|
| **Backend** | Java 21, Spring Boot 3.3.5 | Core REST API services |
| **Security** | Spring Security 6, JJWT (HMAC-SHA256), BCrypt | Stateless JWT auth & credential hashing |
| **Persistence** | Spring Data JPA (Hibernate), PostgreSQL 16 | Relational data persistence & aggregation |
| **Migrations** | Flyway Core | Versioned schema evolution |
| **Cloud Storage**| AWS SDK for Java v2 (`S3Client`, `S3Presigner`) | S3 / MinIO presigned URL orchestration |
| **Rate Limiting**| Bucket4j Core | In-memory token bucket request throttling |
| **Telemetry** | `uap-java`, MaxMind GeoLite2 | User-agent parsing & geographic lookup |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) | Interactive OpenAPI 3 documentation |
| **Frontend** | Angular (v20 Standalone), TypeScript Strict | Single-Page Application (SPA) |
| **Styling** | Tailwind CSS | Responsive, modern dark-mode UI |
| **Visualizations**| Chart.js | Interactive timeseries & metric visualizations |
| **Containerization**| Docker, Docker Compose, Nginx Alpine | Multi-stage production container builds |

---

## Getting Started

### Prerequisites
- Docker Engine & Docker Compose (v2.20+)
- Or for local development: JDK 21+, Maven 3.9+, Node.js 20+

### One-Command Quick Start

1. Clone the repository and navigate to the root directory:
   ```bash
   git clone <repo-url> cloudshare
   cd cloudshare
   ```

2. Copy the sample environment file:
   ```bash
   cp .env.example .env
   ```

3. Launch the full environment using Docker Compose:
   ```bash
   docker compose up --build
   ```

4. Access the applications:
   - **Frontend UI**: [http://localhost:4200](http://localhost:4200)
   - **Backend API**: [http://localhost:8080](http://localhost:8080)
   - **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   - **MinIO Console**: [http://localhost:9001](http://localhost:9001) (`minioadmin` / `minioadmin`)

---

## Configuration Reference

The following environment variables configure the platform:

| Variable | Default Value | Description |
|---|---|---|
| `POSTGRES_DB` | `cloudshare` | PostgreSQL database name |
| `POSTGRES_USER` | `cloudshare` | Database master username |
| `POSTGRES_PASSWORD` | `cloudshare123` | Database master password |
| `DB_URL` | `jdbc:postgresql://localhost:5432/cloudshare` | JDBC database connection URL |
| `JWT_SECRET` | 32-byte hex string | HMAC-SHA256 secret key for signing tokens |
| `JWT_EXPIRY_MINUTES`| `60` | JWT token validity lifespan |
| `IP_HASH_SALT` | `cloudshare-salt` | Cryptographic salt for privacy-preserving IP hashing |
| `S3_ENDPOINT` | `http://localhost:9000` | S3-compatible storage endpoint URL |
| `S3_REGION` | `us-east-1` | S3 region identifier |
| `S3_BUCKET` | `cloudshare-files` | Target storage bucket name |
| `S3_ACCESS_KEY` | `minioadmin` | S3 access key / credential |
| `S3_SECRET_KEY` | `minioadmin` | S3 secret access key |
| `S3_PATH_STYLE` | `true` | Enable path-style S3 access (required for MinIO) |
| `MAX_UPLOAD_BYTES` | `104857600` | Maximum upload size threshold (100 MB default) |
| `CORS_ALLOWED_ORIGINS`| `http://localhost:4200,...` | Comma-separated list of permitted CORS origins |
| `PUBLIC_BASE_URL` | `http://localhost:4200` | Public-facing client domain used for generating share links |

---

## REST API Specification

### Authentication Endpoints
- `POST /api/auth/register` — Create a new user profile (`{email, password, displayName}`).
- `POST /api/auth/login` — Authenticate and receive a JWT access token.
- `GET /api/auth/me` — Retrieve the authenticated profile.

### File Management Endpoints (Bearer Auth)
- `POST /api/files/upload-url` — Request a presigned PUT URL and storage key.
- `POST /api/files/{id}/confirm` — Validate upload completion in object storage.
- `GET /api/files` — Paginated search, filter, and file listing.
- `GET /api/files/{id}` — Retrieve file details and metadata.
- `PATCH /api/files/{id}` — Rename an existing file.
- `DELETE /api/files/{id}` — Remove a file from cloud storage and database.

### Share Link Endpoints (Bearer Auth)
- `POST /api/files/{id}/shares` — Generate a new link with custom password/expiry/quota.
- `GET /api/files/{id}/shares` — List all share links for a file.
- `PATCH /api/shares/{shareId}` — Toggle active state, update expiry, or modify password.
- `DELETE /api/shares/{shareId}` — Revoke and permanently deactivate a share link.

### Public Download Endpoints (Unauthenticated)
- `GET /api/public/shares/{token}` — Retrieve public metadata (file name, size, password requirement). Returns 404 for nonexistent or deactivated links.
- `POST /api/public/shares/{token}/download` — Validate credentials, atomically increment download counter, record telemetry, and return short-lived presigned GET URL (60s).

### Analytics Endpoints (Bearer Auth)
- `GET /api/analytics/overview` — Aggregated metrics across all owned files.
- `GET /api/analytics/files/{id}` — Granular analytics: timeseries, country breakdown, browser, OS, device, and recent events.
- `GET /api/analytics/shares/{shareId}` — Analytics filtered to an individual share link.

---

## Security & Engineering Design Decisions

### Why Direct-to-Storage Presigned Uploads?
Traditional architectures upload files through backend web servers, tying up application threads, consuming substantial memory buffers, and degrading API throughput. By delegating file streaming directly to S3/MinIO via presigned PUT URLs, backend compute nodes remain completely stateless and lightweight, handling hundreds of concurrent upload requests without I/O bottlenecks.

### Atomic Concurrency Protection
To guarantee that a link with a limit of 5 downloads cannot be downloaded 6 times even if 20 users submit requests at the exact same millisecond, the application executes atomic conditional SQL updates:
```sql
UPDATE share_links
SET download_count = download_count + 1
WHERE id = :id
  AND active = true
  AND (expires_at IS NULL OR expires_at > :now)
  AND (max_downloads IS NULL OR download_count < max_downloads);
```
If affected rows equal 0, the transaction aborts with a 400 Bad Request before generating the download ticket.

### Privacy-Preserving Telemetry
Under GDPR and privacy best practices, storing raw visitor IP addresses introduces significant compliance and liability risks. CloudShare computes:
$$\text{ip\_hash} = \text{SHA-256}(\text{ip} \mathbin{\Vert} \text{salt})$$
This allows accurate counting of unique daily visitors without storing identifiable personal network addresses.

---

## Automated Testing

### Backend Unit & Service Tests
Run all test suites including authentication, link race conditions, token validation, and telemetry parsing:
```bash
cd backend
mvn test
```

### Frontend Production Build Validation
Verify compilation, type validation, and production bundle optimization:
```bash
cd frontend
npm run build
```

---

## License

This project is released under the [MIT License](LICENSE).
