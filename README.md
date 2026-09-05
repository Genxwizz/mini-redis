# Mini Redis

A lightweight Redis-compatible in-memory key-value database built from scratch in Java.

Mini Redis implements a custom TCP server using the RESP 2.0 protocol, AOF persistence, command execution, TTL support, and a web-based monitoring dashboard.

## 🚀 Live Demo

**Dashboard:** https://mini-redis-zerd.onrender.com

> Hosted on Render's free tier. The service may spin down after inactivity, so the first request after idle time can take longer to respond.

---

## ✨ Features

- 🗄️ In-memory key-value database
- ⚡ Native TCP socket server
- 🔌 RESP 2.0 protocol support
- 💾 Append-Only File (AOF) persistence
- ⏱️ TTL / key expiration
- 🔢 Atomic increment operations
- 🌐 Web dashboard
- 🖥️ Built-in Redis CLI-style terminal
- 📊 Server statistics and key monitoring
- 🐳 Docker support
- ☁️ Cloud deployment with Render
- 🔄 Automatic deployment from GitHub

---

## 🧰 Supported Commands

| Command | Description | Example |
|---------|-------------|---------|
| `PING` | Check server availability | `PING` |
| `SET` | Store a key-value pair | `SET name John` |
| `GET` | Retrieve a value | `GET name` |
| `DEL` | Delete a key | `DEL name` |
| `KEYS` | List keys | `KEYS *` |
| `TTL` | Check remaining key lifetime | `TTL name` |
| `INCR` | Increment an integer value | `INCR counter` |

---

## 🏗️ Architecture

```text
                    ┌─────────────────────────┐
                    │        Client           │
                    │   Redis CLI / Browser   │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │     Mini Redis Server   │
                    │                         │
                    │  ┌───────────────────┐  │
                    │  │ ClientHandler     │  │
                    │  │ RESP Reader/Writer │  │
                    │  └─────────┬─────────┘  │
                    │            │            │
                    │  ┌─────────▼─────────┐  │
                    │  │ CommandExecutor   │  │
                    │  └─────────┬─────────┘  │
                    │            │            │
                    │  ┌─────────▼─────────┐  │
                    │  │     Database      │  │
                    │  │ ConcurrentHashMap │  │
                    │  └─────────┬─────────┘  │
                    │            │            │
                    │  ┌─────────▼─────────┐  │
                    │  │ AOF Persistence    │  │
                    │  │    append.aof      │  │
                    │  └───────────────────┘  │
                    └─────────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │    HTTP Dashboard       │
                    │   Stats + Commands      │
                    └─────────────────────────┘

## 🧰 Tech Stack

### Backend

- Java 21
- Java ServerSocket
- Java HttpServer
- ConcurrentHashMap
- RESP 2.0

### Build Tools

- Apache Maven
- Maven Shade Plugin

### Web

- HTML
- CSS
- JavaScript
- Java HTTP Server API

### DevOps & Deployment

- Docker
- Docker Compose
- Git
- GitHub
- Render

---

## 📁 Project Structure

```text
mini-redis/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/mini_redis/
│       │       ├── AofPersistence.java
│       │       ├── ClientHandler.java
│       │       ├── CommandExecutor.java
│       │       ├── Database.java
│       │       ├── DataType.java
│       │       ├── HttpDashboardServer.java
│       │       ├── MiniRedisServer.java
│       │       ├── RedisServerListener.java
│       │       ├── RedisServlet.java
│       │       ├── RedisValue.java
│       │       ├── RespReader.java
│       │       └── RespWriter.java
│       │
│       └── resources/
│           └── static/
│               └── index.html
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── README.md
└── .gitignore

---

## 🏗️ Architecture

```text
                    ┌─────────────────────────┐
                    │        Client           │
                    │   Redis CLI / Browser   │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │     Mini Redis Server   │
                    │                         │
                    │  ┌───────────────────┐  │
                    │  │ ClientHandler     │  │
                    │  │ RESP Reader/Writer │  │
                    │  └─────────┬─────────┘  │
                    │            │            │
                    │  ┌─────────▼─────────┐  │
                    │  │ CommandExecutor   │  │
                    │  └─────────┬─────────┘  │
                    │            │            │
                    │  ┌─────────▼─────────┐  │
                    │  │     Database      │  │
                    │  │ ConcurrentHashMap │  │
                    │  └─────────┬─────────┘  │
                    │            │            │
                    │  ┌─────────▼─────────┐  │
                    │  │ AOF Persistence    │  │
                    │  │    append.aof      │  │
                    │  └───────────────────┘  │
                    └─────────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │    HTTP Dashboard       │
                    │   Stats + Commands      │
                    └─────────────────────────┘
```

---

## ▶️ Run Locally

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker Desktop (optional)

### Build

```bash
mvn clean package
```

### Run

```bash
java -jar target/mini-redis.jar
```

The server starts with:

```text
Redis TCP: 6379
HTTP Dashboard: 8080
```

Open the dashboard:

```text
http://localhost:8080
```

---

## 🐳 Run with Docker

Build and start the application:

```bash
docker compose up --build
```

Check the container:

```bash
docker compose ps
```

Open the dashboard:

```text
http://localhost:8080
```

Stop the application:

```bash
docker compose down
```

Restart it:

```bash
docker compose restart
```

---

## 🔌 Redis Protocol

Mini Redis implements the basic Redis Serialization Protocol (RESP 2.0).

Example request:

```text
*1
$4
PING
```

Example response:

```text
+PONG
```

The server accepts native TCP connections on port `6379` when running locally or in an environment that exposes the TCP port.

---

## 💾 AOF Persistence

Mini Redis uses an Append-Only File to record write operations.

Example operations:

```text
SET name John
SET counter 10
INCR counter
DEL name
```

These operations are written to:

```text
append.aof
```

The database can replay the AOF file when the server starts.

> **Deployment note:** The Render free tier uses ephemeral storage. Therefore, `append.aof` should be considered temporary in the cloud deployment and is not guaranteed to survive a service restart or spin-down.

---

## 🌐 Web Dashboard

The dashboard provides:

- Server status
- Server uptime
- Total keys
- AOF persistence information
- Redis protocol information
- Redis CLI-style command terminal
- Quick command builder
- GET / SET / DEL operations
- Key monitoring

### Dashboard API

```text
GET  /api/stats
POST /api/exec
```

---

## ☁️ Deployment

The project is deployed using:

```text
GitHub
   │
   ▼
Render
   │
   ▼
Docker
   │
   ▼
Java 21
   │
   ▼
Mini Redis
```

### Deployment Configuration

- **Platform:** Render
- **Runtime:** Docker
- **Branch:** `main`
- **Compute:** Free
- **HTTP:** Render-provided `PORT`
- **Redis:** TCP port `6379` internally

The application reads the `PORT` environment variable when running in a cloud environment while retaining port `8080` for local development.

---

## 🔄 CI/CD Flow

Every push to the GitHub `main` branch can trigger a new Render deployment.

```text
Developer
    │
    ▼
Git Commit
    │
    ▼
Git Push
    │
    ▼
GitHub main
    │
    ▼
Render Build
    │
    ▼
Docker Image
    │
    ▼
Mini Redis Deployment
```

---

## 🧪 Example Usage

### SET

```text
SET user:100 "John Doe"
```

Response:

```text
OK
```

### GET

```text
GET user:100
```

Response:

```text
John Doe
```

### INCR

```text
SET counter 10
INCR counter
```

Response:

```text
11
```

### TTL

```text
SET session abc 60
TTL session
```

Response:

```text
59
```

### DELETE

```text
DEL user:100
```

Response:

```text
1
```

---

## 🎯 Learning Goals

This project was built to understand how Redis-like systems work internally rather than simply using an existing Redis server.

Key concepts explored:

- TCP networking
- Client/server architecture
- Socket programming
- RESP protocol parsing
- Concurrent data structures
- Command parsing and execution
- Key expiration
- Persistence
- HTTP APIs
- Web dashboards
- Docker containerization
- Cloud deployment
- Git/GitHub workflow

---

## 🚧 Current Limitations

This project is intended as a learning and portfolio project rather than a production Redis replacement.

Current limitations include:

- Data is stored in memory
- No replication
- No clustering
- No authentication
- No TLS
- Limited Redis command compatibility
- Single-node architecture
- Render free-tier storage is ephemeral
- Public deployment exposes the HTTP dashboard rather than a production Redis TCP endpoint

---

## 🔮 Future Improvements

- [ ] More Redis commands
- [ ] Hashes
- [ ] Lists
- [ ] Sets
- [ ] Sorted sets
- [ ] Pub/Sub
- [ ] Transactions
- [ ] Authentication
- [ ] TLS support
- [ ] Replication
- [ ] Snapshot persistence
- [ ] Persistent cloud storage
- [ ] Automated tests
- [ ] Performance benchmarking
- [ ] Metrics and monitoring

---

## 👨‍💻 Author

**Vishwash**

GitHub: https://github.com/Genxwizz

Project: https://github.com/Genxwizz/mini-redis

---

## ⭐ Project

If you find this project interesting, consider giving it a ⭐ on GitHub.
