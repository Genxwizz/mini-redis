# Mini Redis Server & Web Dashboard

A lightweight, self-contained Redis server implementation written in Java with an embedded dark-themed Single-Page Web Dashboard, REST API, and TCP RESP protocol support.

---

## Architecture Overview

- **TCP Server (`:6379`)**: Native RESP 2.0 protocol endpoint compatible with `redis-cli` and standard Redis client libraries.
- **Web Dashboard (`:8080`)**: Embedded single-page dashboard with real-time stats, key explorer, and interactive web CLI terminal.
- **Persistence (`append.aof`)**: Write-Ahead AOF log engine that automatically replays state on boot and records write operations (`SET`, `DEL`, `EXPIRE`, `INCR`, `FLUSHDB`).

---

## Deployment & Running Guide

### 1. Run via Executable JAR (Standalone)

Build the executable fat JAR using Maven:

```bash
mvn clean package
```

Launch the application:

```bash
java -jar target/mini-redis.jar
```

Access:
- **Web Dashboard**: `http://localhost:8080`
- **Redis TCP**: `localhost:6379`

---

### 2. Run via Docker

Build and launch the Docker container:

```bash
docker build -t mini-redis .
docker run -p 6379:6379 -p 8080:8080 -v $(pwd)/append.aof:/app/append.aof mini-redis
```

---

### 3. Run via Docker Compose

```bash
docker compose up -d
```

---

## REST API Endpoints

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `GET /` | `GET` | Serves static SPA Web Dashboard (`index.html`) |
| `GET /api/stats` | `GET` | Returns server stats, uptime, total key count, and active keys array (JSON) |
| `POST /api/exec` | `POST` | Executes raw command payload e.g. `{"command": "SET user:100 Alice"}` and returns structured JSON output |

---

## Connecting via Redis CLI

```bash
redis-cli -h 127.0.0.1 -p 6379
127.0.0.1:6379> PING
PONG
127.0.0.1:6379> SET user:100 "John Doe" EX 60
OK
127.0.0.1:6379> GET user:100
"John Doe"
127.0.0.1:6379> TTL user:100
(integer) 58
```
