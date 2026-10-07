# Spotify MCP Server / Discord Bot

以 Java 21 與 Spring Boot 建立的 Spotify 整合服務。專案目前提供 Streamable HTTP MCP Server，讓 AI Agent 能透過 MCP Tools 執行 Spotify OAuth、搜尋歌曲、讀取播放狀態、管理播放清單、同步最近播放紀錄，以及使用 Ollama embedding 與 PostgreSQL `pgvector` 進行歌詞語意搜尋。


## 目前功能

- Spotify 搜尋：track、artist、album、playlist
- 取得使用者 Top Tracks
- 取得與建立 Playlist
- 新增歌曲到 Playlist
- 取得目前播放狀態
- 暫停與繼續播放
- 取得最近播放紀錄並同步到 PostgreSQL
- 使用 PostgreSQL `pgvector` 搜尋語意相近歌曲
- Spring AI MCP Server（Streamable HTTP）

## 技術棧

| 類別 | 技術 |
| --- | --- |
| Runtime | Java 21 |
| Framework | Spring Boot 3.5.0 |
| MCP | Spring AI 1.1.8 MCP Server WebMVC |
| Reactive HTTP | Spring WebFlux、WebClient、HTTP Interface |
| Spotify | Spotify Web API、spotify-web-api-java 9.4.0 |
| Discord | JDA 6.5.0（依賴已加入，Bot listener 尚未實作） |
| Database | PostgreSQL 16、Spring Data JPA、JdbcClient |
| Vector Search | pgvector、HNSW、cosine distance |
| Embedding | Ollama `/api/embed` |
| Migration | Flyway |
| Deployment | Docker、Kubernetes、Minikube、Istio |

## 架構

```text
MCP Client / AI Agent
          │
          │ Streamable HTTP: /mcp
          ▼
┌───────────────────────────────┐
│ Spring Boot Application       │
│                               │
│ SpotifyTools (MCP Tool Layer) │
│              │                │
│              ▼                │
│ Service Layer                 │
│ ├─ SpotifyService             │
│ ├─ PlayerHistoryService       │
│ └─ SongSemanticSearchService  │
│        │              │       │
│        ▼              ▼       │
│ Spotify API       Ollama      │
│ Client            Client      │
└────────────┬──────────────────┘
             │
             ▼
     PostgreSQL + pgvector
```

程式碼遵循以下呼叫方向：

```text
Discord / MCP → Service Layer → API Client → External API
```

Discord 與 MCP 若使用相同 Spotify 功能，應共用既有 Service Layer，不應各自重新實作 Spotify HTTP request。

## 專案結構

```text
.
├── src/main/java/com/example
│   ├── App.java                         # Spring Boot 入口與 MCP Tool provider
│   ├── Spotify
│   │   ├── SpotifyService.java          # OAuth、token 與 Spotify 應用邏輯
│   │   ├── SpotifyAPIClient.java        # Spotify HTTP Interface
│   │   └── SpotifyApiClientConfiguration.java
│   ├── mcp/SpotifyTools.java            # 提供給 AI Agent 的 MCP Tools
│   ├── controller/CallbackController.java
│   ├── playerhistory                    # 最近播放紀錄持久化
│   ├── song                             # pgvector 語意搜尋
│   ├── ollama                           # Ollama embedding client/service
│   └── DTO                              # Spotify 與 Tool response DTO
├── src/main/resources
│   ├── application.properties
│   └── db/migration                     # Flyway V1～V5
├── src/test                             # 單元與 Spring 整合測試
├── k8s                                  # Kubernetes 與 Istio manifests
├── .github/workflows/docker-ci.yml      # self-hosted CI/CD
├── Dockerfile
└── pom.xml
```

## 執行需求

- JDK 21
- Maven 3.9+
- PostgreSQL，且可啟用 `vector` extension
- Ollama 或相容的 `/api/embed` 服務
- Spotify Developer 應用程式
- 選用：Docker
- 選用：Minikube、kubectl、Istio（Kubernetes 部署時需要）

## 環境變數

專案會讀取系統環境變數，也會選擇性載入專案根目錄的 `.env`。`.env` 已被 `.gitignore` 與 `.dockerignore` 排除，不應提交任何憑證。

### 必要設定

| 變數 | 說明 |
| --- | --- |
| `SPOTIFY_CLIENT_ID` | Spotify Developer App Client ID |
| `SPOTIFY_CLIENT_SECRET` | Spotify Developer App Client Secret |
| `SPOTIFY_REDIRECT_URI` | Spotify OAuth callback，必須與 Spotify Dashboard 完全一致 |
| `OLLAMA_BASE_URL` | Ollama 服務位址，例如 `http://localhost:11434` |
| `OLLAMA_EMBEDDING_MODEL` | 回傳 768 維向量的 embedding model |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL 使用者名稱 |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL 密碼 |

### 選用設定

| 變數 | 預設值 | 說明 |
| --- | --- | --- |
| `SPOTIFY_API_BASE_URL` | `https://api.spotify.com/v1` | Spotify API base URL |
| `OLLAMA_CONNECT_TIMEOUT` | `5s` | Ollama 連線 timeout |
| `OLLAMA_RESPONSE_TIMEOUT` | `60s` | Ollama response timeout |
| `DB_HOST` | `localhost` | 未指定完整 JDBC URL 時使用 |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `spotify` | PostgreSQL database |
| `DB_USERNAME` | `karta40213` | PostgreSQL username fallback |
| `DB_PASSWORD` | 空字串 | PostgreSQL password fallback |
| `DISCORD_BOT_ENABLED` | `false` | 預留的 Discord Bot 開關 |
| `JDA_API` | 空字串 | 預留的 Discord Bot token |
| `DISCORD_OWNER_ID` | 空字串 | 預留的 Discord owner ID |
| `DISCORD_GUILD_ID` | 空字串 | 預留的 Discord guild ID |

本機 `.env` 範例：

```properties
SPOTIFY_CLIENT_ID=<your-client-id>
SPOTIFY_CLIENT_SECRET=<your-client-secret>
SPOTIFY_REDIRECT_URI=http://localhost:8080/callback

OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_EMBEDDING_MODEL=<your-768-dimension-model>
OLLAMA_CONNECT_TIMEOUT=5s
OLLAMA_RESPONSE_TIMEOUT=60s

SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/spotify
SPRING_DATASOURCE_USERNAME=<your-database-user>
SPRING_DATASOURCE_PASSWORD=<your-database-password>
```

請勿將 `.env`、Spotify secret、Discord token、資料庫密碼、TLS private key 或任何 access/refresh token commit 到 Git。

## 本機啟動

### 1. 準備 PostgreSQL

建立 `spotify` database，並確保執行應用程式 migration 的使用者有權啟用 pgvector：

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

應用程式啟動時，Flyway 會依序執行 `src/main/resources/db/migration`：

- 建立 `player_history`
- 啟用 `vector` extension
- 建立 `track_catalog` 與分類表
- 建立 `song_embeddings`（`VECTOR(768)`）
- 建立 cosine distance HNSW index

### 2. 準備 Ollama

啟動 Ollama，並準備一個輸出 768 維向量的 embedding model。應用程式會呼叫：

```text
POST {OLLAMA_BASE_URL}/api/embed
```

### 3. 設定 Spotify App

在 Spotify Developer Dashboard 建立應用程式，並將 Redirect URI 設成與 `SPOTIFY_REDIRECT_URI` 完全相同，例如：

```text
http://localhost:8080/callback
```

目前要求的 OAuth scopes：

- `user-read-email`
- `user-top-read`
- `playlist-read-private`
- `playlist-modify-private`
- `playlist-modify-public`
- `user-read-playback-state`
- `user-modify-playback-state`
- `user-read-recently-played`

部分播放器操作需要 Spotify Premium、可用的播放裝置，以及對應帳號權限。

### 4. 啟動應用程式

```bash
mvn spring-boot:run
```

預設 MCP endpoint：

```text
http://localhost:8080/mcp
```

Spotify OAuth callback：

```text
http://localhost:8080/callback
```

## Spotify 授權流程

1. MCP Client 連線到 `/mcp`。
2. 呼叫 `authorizeSpotify` Tool 取得 Spotify 授權網址。
3. 使用者開啟網址並同意授權。
4. Spotify 將使用者導回 `SPOTIFY_REDIRECT_URI` 的 `/callback?code=...`。
5. 應用程式使用 authorization code 交換 access token 與 refresh token。
6. 後續 Tool 呼叫會重用 token，並在 access token 過期時嘗試刷新。

目前 token 儲存在應用程式記憶體，沒有寫入資料庫；應用程式或 Pod 重啟後需要重新完成 Spotify 授權。現有設計也較適合單一 Spotify 使用者，尚未提供多使用者 token 隔離。

## MCP Tools

| Tool | 功能 |
| --- | --- |
| `authorizeSpotify` | 產生 Spotify OAuth 授權網址 |
| `searchSpotify` | 搜尋 track、artist、album 或 playlist |
| `getTopTracks` | 取得使用者 Top Tracks |
| `getPlaylist` | 取得目前使用者 Playlists |
| `createPlaylist` | 建立私人 Playlist |
| `addTrackToPlaylist` | 將 Spotify track URI 加入 Playlist |
| `getPlaybackState` | 取得播放器、裝置與播放狀態 |
| `pauseSpotifyPlayback` | 暫停播放 |
| `resumeSpotifyPlayback` | 繼續播放 |
| `getRecentlyPlayed` | 取得最近播放並去重同步至 PostgreSQL |
| `searchSongsBySemanticQuery` | 使用 Ollama embedding 與 pgvector 搜尋歌曲候選結果 |

`getRecentlyPlayed` 的 `limit` 預設為 20，允許範圍為 1～50。`searchSongsBySemanticQuery` 的 `topK` 預設為 10，允許範圍為 1～20。

語意搜尋只查詢既有的 `song_embeddings` 資料。Flyway 只建立 schema 與 index，不會自動產生或匯入歌曲 embedding；使用此 Tool 前必須先準備資料。

## 測試與建置

執行測試：

```bash
mvn test
```

建立可執行 JAR：

```bash
mvn clean package
```

輸出位置：

```text
target/DiscordBot-1.0-SNAPSHOT.jar
```

執行 JAR：

```bash
java -jar target/DiscordBot-1.0-SNAPSHOT.jar
```

## Docker

Dockerfile 使用 Maven 與 Eclipse Temurin 21 建立 multi-stage image，runtime container 使用非 root 的 `app` 使用者。

```bash
docker build -t discord-bot:local .
```

執行容器時必須提供 Spotify、Ollama 與 PostgreSQL 設定。容器內的 `localhost` 指向容器本身，因此外部服務位址必須使用容器可解析的 hostname。

```bash
docker run --rm -p 8080:8080 \
  -e SPOTIFY_CLIENT_ID='<your-client-id>' \
  -e SPOTIFY_CLIENT_SECRET='<your-client-secret>' \
  -e SPOTIFY_REDIRECT_URI='http://localhost:8080/callback' \
  -e OLLAMA_BASE_URL='http://host.docker.internal:11434' \
  -e OLLAMA_EMBEDDING_MODEL='<your-768-dimension-model>' \
  -e SPRING_DATASOURCE_URL='jdbc:postgresql://<database-host>:5432/spotify' \
  -e SPRING_DATASOURCE_USERNAME='<your-database-user>' \
  -e SPRING_DATASOURCE_PASSWORD='<your-database-password>' \
  discord-bot:local
```

`host.docker.internal` 的可用性依作業系統與 Docker 環境而異。

## Kubernetes / Minikube

`k8s/` 目前提供：

| 檔案 | 用途 |
| --- | --- |
| `postgresql-deployment.yaml` | PostgreSQL 16、pgvector、5 Gi PVC 與 ClusterIP Service |
| `deployment.yaml` | Spring Boot application Deployment |
| `service.yaml` | 應用程式的 ClusterIP Service |
| `gateway.yaml` | Istio HTTPS Gateway，host 為 `discord.test` |
| `virtual-service.yaml` | 將 `discord.test` 流量導向應用程式 |

部署前必須準備以下 Secrets：

- `postgresql-secret`：包含 `POSTGRES_PASSWORD`
- `discord-bot-secret`：至少包含三個 `SPOTIFY_*` 變數
- `ollama-secret`：包含 `OLLAMA_BASE_URL`、`OLLAMA_EMBEDDING_MODEL`、`OLLAMA_RESPONSE_TIMEOUT`
- `discord-tls`：位於 `istio-system` namespace，供 Istio Gateway 使用

建立 application secrets 的範例：

```bash
kubectl create secret generic postgresql-secret \
  --from-literal=POSTGRES_PASSWORD='<your-database-password>'

kubectl create secret generic discord-bot-secret \
  --from-literal=SPOTIFY_CLIENT_ID='<your-client-id>' \
  --from-literal=SPOTIFY_CLIENT_SECRET='<your-client-secret>' \
  --from-literal=SPOTIFY_REDIRECT_URI='https://discord.test/callback'

kubectl create secret generic ollama-secret \
  --from-literal=OLLAMA_BASE_URL='<ollama-url-reachable-from-the-cluster>' \
  --from-literal=OLLAMA_EMBEDDING_MODEL='<your-768-dimension-model>' \
  --from-literal=OLLAMA_RESPONSE_TIMEOUT='60s'
```

接著套用 manifests：

```bash
kubectl apply -f k8s/postgresql-deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/gateway.yaml
kubectl apply -f k8s/virtual-service.yaml
kubectl apply -f k8s/deployment.yaml
```

目前 `k8s/deployment.yaml` 啟動的是單一 Spring Boot JAR，因此 MCP、Spotify callback、Service Layer 與資料庫整合都在同一個 Pod。Kubernetes 不會依照 Java package 自動將它們拆成不同服務。

若未來完成 Discord listener 並希望獨立部署 Discord Bot，可以先沿用同一個 image，建立 MCP 與 Discord 兩個 Deployment，再以 Spring profile 或 `@ConditionalOnProperty` 控制各自載入的 Bean。Discord Gateway 是由 Bot 主動建立長連線，Discord Deployment 通常不需要公開 Service；MCP 與 OAuth callback 才需要由 Service／Ingress 對外提供。

## CI/CD

推送到 `main` 後，`.github/workflows/docker-ci.yml` 會在標記為 `self-hosted, linux, x64` 的 runner 上：

1. 建置 Docker image。
2. 將 commit SHA 與 `latest` tags 推送到 Docker Hub。
3. 驗證 Kubernetes manifests。
4. 建立或更新 TLS、PostgreSQL 與 Ollama Secrets。
5. 部署 PostgreSQL 並確認 pgvector extension。
6. 套用 Service、Istio Gateway、VirtualService 與 application Deployment。
7. 將 Deployment image 更新成本次 commit SHA。
8. 等待 rollout 並輸出部署狀態。

GitHub Repository 需要設定：

- `DOCKER_USERNAME`
- `DOCKER_TOKEN`
- `DISCORD_TLS_CRT`
- `DISCORD_TLS_KEY`
- `POSTGRES_PASSWORD`
- `OLLAMA_BASE_URL`
- `OLLAMA_EMBEDDING_MODEL`
- `OLLAMA_RESPONSE_TIMEOUT`

目前 workflow 不會建立 `discord-bot-secret`，該 Secret 必須事先存在於目標 Kubernetes namespace，並包含 Spotify 憑證與 redirect URI。

## 已知限制

- Discord JDA dependency 與設定已存在，但 Discord Bot runtime 尚未實作。
- Spotify access token 與 refresh token 只儲存在記憶體，重啟後會遺失。
- OAuth token 尚未按 Discord user 或其他使用者識別資訊隔離。
- `song_embeddings` migration 不會自動匯入資料。
- 語意搜尋固定要求 768 維 embedding；更換模型時必須確認維度一致。
- 目前 Kubernetes Deployment 只有一個 Spring Boot application Pod，尚未拆成 Discord 與 MCP 兩個 Deployment。
- Kubernetes manifests 使用預設 namespace，且 `discord.test`、TLS、DNS／hosts mapping 需要依實際環境調整。

## 安全注意事項

- 不要將 `.env`、access token、refresh token、Discord token、Spotify Client Secret、資料庫密碼或 TLS private key 提交到 Git。
- 正式環境應使用 Kubernetes Secret、External Secrets 或雲端 Secret Manager。
- Log 不應輸出 token 或 secret 內容。
- 對外部署 `/mcp` 與 `/callback` 時應使用 HTTPS，並依使用情境增加適當的存取控制。
