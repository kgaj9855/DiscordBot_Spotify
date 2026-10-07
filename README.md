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

