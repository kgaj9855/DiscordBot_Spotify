# AGENTS.md

## 專案概述

本專案是一個以 Java 開發的 Discord Bot，整合 Spotify Web API 與 MCP Server，讓 AI Agent 可以透過 MCP Tools 操作 Spotify 功能，並透過 Discord Bot 與使用者互動。

主要技術：

* Java 21
* Spring Boot 3.5.x
* Maven
* JDA
* Spring WebFlux / WebClient
* Spotify Web API
* MCP Server
* Kubernetes / Minikube
* Istio
* Postgresq;
---

## 專案目標

本專案主要目標：

1. 使用 JDA 建立 Discord Bot。
2. 整合 Spotify Web API。
3. 將 Spotify 功能封裝成 MCP Tools。
4. 讓 AI Agent 可以依照使用者需求，自動選擇並呼叫適合的 MCP Tool。
5. Spotify API 邏輯應保持可重複使用，不與 Discord 或 MCP 邏輯過度耦合。
6. 維持簡單、清楚且容易維護的專案架構。

---

## 專案架構

原則上遵循以下呼叫流程：

```text
Discord / MCP
      ↓
Service Layer
      ↓
Spotify API Client
      ↓
Spotify Web API
```

各層應保持職責分離，避免將所有功能集中在單一 Class。

---

## MCP Tool Layer

主要負責：

* 定義 MCP Tools。
* 撰寫清楚的 Tool Description。
* 定義 Tool 所需要的參數。
* 必要時進行基本參數驗證。
* 呼叫 Service Layer。
* 將結果回傳給 AI Agent。

不要直接在 MCP Tool Class 裡實作 Spotify HTTP Request。

例如：

```java
@Tool
public PlaybackStateResponse getCurrentPlayback() {
    return spotifyService
            .getCurrentPlayback()
            .block();
}
```

MCP Tool 應保持簡單，主要負責「提供 AI Agent 可呼叫的功能」。

---

## Discord Bot Layer

主要負責：

* 處理 Discord Events。
* 接收使用者訊息。
* 處理 Discord Interaction。
* 回覆 Discord 訊息。
* 必要時呼叫 Application Service。

不要在 Discord Event Handler 裡重新實作 Spotify API。

如果 Discord 與 MCP 都需要相同 Spotify 功能，應共用相同的 Service Layer。

例如：

```text
Discord Handler ─┐
                 ├─→ SpotifyService → SpotifyAPIClient
MCP Tool ────────┘
```

不要建立兩套 Spotify API 實作。

---

## Service Layer

主要負責：

* Application / Business Logic。
* Spotify Authentication 流程。
* 取得有效 Access Token。
* 必要時 Refresh Access Token。
* 呼叫 Spotify API Client。
* 處理或轉換 API Response。

例如：

```java
public Mono<PlaybackStateResponse> getCurrentPlayback() {

    return Mono.fromFuture(getValidAccessToken())
            .flatMap(accessToken ->
                    spotifyAPIClient.getCurrentPlayback(accessToken)
            );
}
```

可重複使用的邏輯應優先放在 Service Layer。

不要將 Business Logic 放在：

* Controller
* Discord Event Handler
* MCP Tool
* Spotify API Client

---

## Spotify API Client

主要負責直接與 Spotify Web API 溝通。

包含：

* 建立 HTTP Request。
* 設定 Endpoint。
* 設定 Query Parameters。
* 設定 Request Body。
* 設定 Bearer Token。
* 接收 Spotify Response。
* 回傳 `Mono` / `Flux`。

Spotify API Request 統一優先使用 Spring `WebClient`。

例如：

```java
public Mono<Integer> pausePlayer(String accessToken) {

    return client.put()
            .uri("/me/player/pause")
            .headers(headers ->
                    headers.setBearerAuth(accessToken)
            )
            .retrieve()
            .toBodilessEntity()
            .map(response ->
                    response.getStatusCode().value()
            );
}
```

Spotify API Client 不應包含 MCP 或 Discord 邏輯。

---

## Reactive Programming

Spotify API 整合目前使用 Spring WebFlux。

優先使用：

* `Mono<T>`
* `Flux<T>`
* `map`
* `flatMap`

避免在 Service Layer 或 API Client 中隨意使用：

```java
.block()
```

只有在同步邊界真的需要取得結果時才使用，例如 MCP Tool 必須直接回傳同步結果：

```java
return spotifyService
        .getCurrentPlayback()
        .block();
```

如果方法本身已經回傳 `Mono<T>`，不要再次使用 `Mono.just()` 包裝。

錯誤：

```java
Mono.just(
    spotifyService.getCurrentPlayback()
);
```

這樣會變成：

```text
Mono<Mono<T>>
```

正確：

```java
spotifyService.getCurrentPlayback();
```

---

## Spotify Authentication

Spotify 使用 OAuth 2.0 Authorization Code Flow。

基本流程：

```text
Authorization URL
        ↓
Spotify 使用者授權
        ↓
Callback
        ↓
Authorization Code
        ↓
Access Token + Refresh Token
        ↓
Access Token 過期
        ↓
Refresh Token
        ↓
取得新的 Access Token
```

應沿用專案現有的 Token Management 機制。

不要因為新增 Spotify 功能，就另外建立新的 OAuth 流程。

新增 Spotify API 功能前，需要確認該 Endpoint 所要求的 OAuth Scope。

例如：

```text
user-read-email
user-top-read
playlist-modify-private
user-modify-playback-state
user-read-playback-state
```

Authentication 成功不代表所有 Spotify Endpoint 都能使用。

部分功能可能另外要求：

* 特定 OAuth Scope
* Spotify Premium
* 特定 Account Permission

---

## Spotify API 開發規則

Spotify Web API Base URL：

```text
https://api.spotify.com/v1
```

新增 Spotify 功能前，應確認：

1. Spotify 官方 Endpoint。
2. HTTP Method。
3. Required OAuth Scope。
4. Query Parameters。
5. Request Body。
6. Response Body。
7. Success HTTP Status Code。
8. 是否需要 Spotify Premium。

新增功能原則上按照：

```text
Spotify API Client
        ↓
Service
        ↓
MCP Tool / Discord
```

進行實作。

不要在不同 Class 重複撰寫相同 Spotify HTTP Request。

---

## MCP Tool 設計

MCP Tool Description 必須清楚，讓 AI Agent 可以理解：

* 什麼情況應該呼叫這個 Tool。
* 每個參數代表什麼。
* 有哪些限制。
* 哪些參數是 Optional。

例如：

```java
@Tool(description = """
    Search Spotify for tracks, artists, albums, or playlists.

    q:
    Spotify search query.

    type:
    Type of item to search.
    Supported values:
    track, artist, album, playlist.

    limit:
    Maximum number of results.

    offset:
    Index of the first result.
    """)
```

新增 MCP Tool 時：

* 一個 Tool 優先負責一個明確操作。
* 使用清楚的參數名稱。
* Tool Description 應描述重要限制。
* 不要加入不必要的參數。
* 優先重用既有 Service。
* 不要讓 AI Agent 自己提供 Access Token。

例如不建議：

```java
@Tool
public void pausePlayer(String accessToken)
```

建議：

```java
@Tool
public void pausePlayer()
```

Access Token 應由 Service / Authentication Layer 自己管理。

---

## Error Handling

不要忽略 Spotify API Error。

常見 HTTP Status：

```text
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
429 Too Many Requests
```

處理 Error 時：

* 保留有用的 Spotify Error Message。
* 不要暴露 Access Token。
* 不要暴露 Refresh Token。
* 不要暴露 Client Secret。
* Log 應提供足夠的 Debug 資訊。
* 如果 Spotify 已提供明確錯誤原因，不要全部轉換成模糊的「API Error」。

### 403 Forbidden

優先檢查：

* OAuth Scope
* Spotify Premium Requirement
* Account Permission
* Endpoint Permission

### 401 Unauthorized

優先檢查：

* Access Token 是否過期。
* Refresh Token 是否正常。
* Authorization Header 是否正確。

---

## Configuration 與 Secrets

禁止將敏感資訊直接 Hard-code 在 Java Source Code。

包含：

* Discord Bot Token
* Spotify Client ID
* Spotify Client Secret
* Access Token
* Refresh Token
* API Key
* Password

應使用：

* Environment Variables
* Spring Configuration
* 專案既有的 Secret Management 方式

不要將 Secrets Commit 到 Git。

Log 中也不要輸出：

```text
Access Token
Refresh Token
Discord Token
Client Secret
Password
API Key
```

---

## Java 程式碼風格

優先保持程式碼：

* 簡單
* 清楚
* 易讀
* 易維護

建議：

* 使用有意義的 Class Name。
* 使用有意義的 Method Name。
* 優先 Constructor Injection。
* Method 不要過度龐大。
* 外部 API Response 使用 DTO。
* 遵循專案現有命名方式。
* 優先沿用現有架構。

避免：

* 不必要的 Design Pattern。
* 過度抽象化。
* 重複程式碼。
* 過大的 Class。
* 過深的 if / else。
* Hard-coded Configuration。
* 與需求無關的 Refactor。

除非確實有必要，否則不要因為一個簡單功能新增大量 Library 或 Framework。

---

## 修改專案時的原則

修改程式碼前：

1. 先閱讀相關現有程式碼。
2. 理解目前架構。
3. 找出真正需要修改的位置。
4. 優先重用既有 Service / Client / DTO。
5. 確認是否會影響既有功能。

修改時：

* 採取最小必要修改。
* 不要修改與需求無關的檔案。
* 不要隨意重新命名 Class。
* 不要隨意重新命名 Public Method。
* 不要隨意改變 API Contract。
* 不要因為程式碼「看起來可以整理」就進行大規模 Refactor。

如果功能已經存在，優先修改既有實作。

不要另外建立一套平行功能。

---

## Build 與驗證

修改 Java 程式碼後，在適合的情況下執行驗證。

Maven：

```bash
mvn test
```

或：

```bash
mvn clean package
```

如果專案有 Maven Wrapper，優先使用：

Linux / macOS：

```bash
./mvnw test
```

Windows：

```powershell
.\mvnw.cmd test
```

如果沒有實際執行 Build / Test，不要宣稱：

```text
Build 成功
測試全部通過
```

應明確區分：

```text
已修改程式碼
```

與：

```text
已實際執行並通過測試
```

---

## Git 規則

每次修改應盡量集中在單一目的。

不要 Commit：

* Secrets
* Access Token
* Refresh Token
* Build Output
* IDE Temporary Files
* Logs

一般應忽略：

```text
target/
.env
.vscode/
.idea/
*.log
```

不要手動修改 `.git` 資料夾內的內容。

---

## Deployment

專案可能執行於：

* Local
* Kubernetes
* Minikube

部署至 Kubernetes 時，不要假設 Pod 裡面的：

```text
localhost
```

等於 Host Machine。

需要穩定存取 Pod 時，應使用 Kubernetes Service。

基本架構：

```text
Client / AI Agent
        ↓
Kubernetes Service
        ↓
Discord Bot / MCP Server Pod
```

如果專案已存在 Deployment / Service YAML，優先修改現有設定。

不要建立功能重複的 Kubernetes Resource。

除非使用者明確要求，否則不要將 Docker 引入為本專案開發流程的必要條件。

---

## Agent 工作規則

AI Coding Agent 在修改本 Repository 時，必須遵守以下原則：

1. 修改前先閱讀現有程式碼。
2. 先理解現有架構，再進行修改。
3. 優先修改既有檔案，而不是建立重複實作。
4. 只修改與目前需求直接相關的內容。
5. 不要移除正常運作中的功能，除非需求明確要求。
6. 不要進行與需求無關的 Code Cleanup。
7. 不要任意進行大型 Refactor。
8. 不要任意改變 Public API。
9. 不要任意新增 Dependency。
10. 不要暴露任何 Credentials 或 Secrets。

如果需求會造成重大架構改變，應先說明：

* 預計修改哪些部分。
* 為什麼需要修改。
* 可能影響哪些既有功能。

如果只是一般小型功能修改，直接依照現有架構採取最簡單、最小範圍的實作即可。

---

## 本專案特別偏好

針對本 Repository：

* 使用 Java 21。
* 遵循 Spring Boot 現有架構。
* Spotify HTTP Request 優先使用 `WebClient`。
* 保留目前 Reactive Programming 架構。
* MCP、Discord、Service、API Client 應保持職責分離。
* 重用既有 Spotify OAuth / Token Management。
* 優先修改現有程式碼，不建立重複功能。
* 避免不必要的 Dependency。
* 避免過度工程化。
* Agent / MCP 能自動完成的操作，不需要為了操作而額外加入 UI 或按鈕。
* 除非明確要求，否則不要將 Docker 作為本機開發的必要條件。
* 優先選擇簡單、清楚、容易維護的解決方案。

---

## 最重要原則

在進行任何修改時：

> 先理解現有程式碼，再修改。

> 優先採取最小必要修改，不要為了解決單一問題而重構整個專案。

> 新功能應沿用既有架構，而不是建立另一套平行架構。

> MCP、Discord、Service、Spotify API Client 各自負責自己的職責。

> 不要因為「可以改得更漂亮」就修改與目前需求無關的程式碼。
