<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Set" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mini Redis Dashboard</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-primary: #0f172a;
            --bg-secondary: #1e293b;
            --bg-card: #182234;
            --accent-red: #ef4444;
            --accent-red-hover: #dc2626;
            --accent-blue: #3b82f6;
            --accent-green: #10b981;
            --text-main: #f8fafc;
            --text-muted: #94a3b8;
            --border-color: #334155;
            --code-bg: #090d16;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Inter', sans-serif;
            background-color: var(--bg-primary);
            color: var(--text-main);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 2rem 1rem;
        }

        .container {
            width: 100%;
            max-width: 900px;
        }

        header {
            text-align: center;
            margin-bottom: 2rem;
        }

        .logo {
            display: inline-flex;
            align-items: center;
            gap: 0.75rem;
            font-size: 2rem;
            font-weight: 700;
            color: var(--accent-red);
            margin-bottom: 0.5rem;
        }

        .subtitle {
            color: var(--text-muted);
            font-size: 1rem;
        }

        .status-bar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: var(--bg-secondary);
            border: 1px solid var(--border-color);
            padding: 1rem 1.5rem;
            border-radius: 12px;
            margin-bottom: 2rem;
        }

        .badge {
            background: rgba(16, 185, 129, 0.15);
            color: var(--accent-green);
            border: 1px solid rgba(16, 185, 129, 0.3);
            padding: 0.25rem 0.75rem;
            border-radius: 9999px;
            font-size: 0.875rem;
            font-weight: 500;
        }

        .grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 1.5rem;
            margin-bottom: 2rem;
        }

        @media (max-width: 768px) {
            .grid {
                grid-template-columns: 1fr;
            }
        }

        .card {
            background: var(--bg-card);
            border: 1px solid var(--border-color);
            border-radius: 16px;
            padding: 1.5rem;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
        }

        .card-title {
            font-size: 1.15rem;
            font-weight: 600;
            margin-bottom: 1.25rem;
            color: var(--text-main);
            border-bottom: 1px solid var(--border-color);
            padding-bottom: 0.75rem;
        }

        .form-group {
            margin-bottom: 1rem;
        }

        label {
            display: block;
            font-size: 0.875rem;
            font-weight: 500;
            color: var(--text-muted);
            margin-bottom: 0.375rem;
        }

        input[type="text"], input[type="number"] {
            width: 100%;
            padding: 0.75rem 1rem;
            border-radius: 8px;
            border: 1px solid var(--border-color);
            background: var(--bg-primary);
            color: var(--text-main);
            font-family: 'JetBrains Mono', monospace;
            font-size: 0.9rem;
            outline: none;
            transition: border-color 0.2s;
        }

        input[type="text"]:focus, input[type="number"]:focus {
            border-color: var(--accent-blue);
        }

        .btn {
            display: inline-flex;
            justify-content: center;
            align-items: center;
            width: 100%;
            padding: 0.75rem 1.25rem;
            border-radius: 8px;
            font-weight: 600;
            font-size: 0.9rem;
            cursor: pointer;
            border: none;
            transition: all 0.2s;
        }

        .btn-red {
            background: var(--accent-red);
            color: white;
        }
        .btn-red:hover {
            background: var(--accent-red-hover);
        }

        .btn-blue {
            background: var(--accent-blue);
            color: white;
        }
        .btn-blue:hover {
            background: #2563eb;
        }

        .btn-outline-danger {
            background: transparent;
            border: 1px solid #f87171;
            color: #f87171;
        }
        .btn-outline-danger:hover {
            background: rgba(239, 68, 68, 0.15);
        }

        .result-box {
            background: var(--code-bg);
            border: 1px solid var(--border-color);
            border-radius: 12px;
            padding: 1.25rem;
            margin-bottom: 2rem;
        }

        .result-title {
            font-size: 0.85rem;
            color: var(--text-muted);
            text-transform: uppercase;
            letter-spacing: 0.05em;
            margin-bottom: 0.5rem;
        }

        .result-content {
            font-family: 'JetBrains Mono', monospace;
            font-size: 1rem;
            color: #38bdf8;
            word-break: break-all;
        }

        .keys-list {
            display: flex;
            flex-wrap: wrap;
            gap: 0.5rem;
            margin-top: 1rem;
        }

        .key-tag {
            background: var(--bg-secondary);
            border: 1px solid var(--border-color);
            padding: 0.35rem 0.75rem;
            border-radius: 6px;
            font-family: 'JetBrains Mono', monospace;
            font-size: 0.85rem;
            color: #e2e8f0;
        }
    </style>
</head>
<body>

<div class="container">
    <header>
        <div class="logo">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
            Mini Redis Dashboard
        </div>
        <p class="subtitle">Running on Tomcat Localhost & TCP Port 6379</p>
    </header>

<%
    com.example.mini_redis.Database pageDb = (com.example.mini_redis.Database) request.getAttribute("dbInstance");
    if (pageDb == null) {
        pageDb = (com.example.mini_redis.Database) application.getAttribute(com.example.mini_redis.RedisServerListener.DATABASE_ATTRIBUTE);
    }
    int totalKeysCount = (pageDb != null) ? pageDb.size() : (request.getAttribute("dbSize") != null ? (Integer)request.getAttribute("dbSize") : 0);
    Set<String> activeKeysSet = (pageDb != null) ? pageDb.keys() : (Set<String>) request.getAttribute("keys");
%>
    <div class="status-bar">
        <div>
            <span style="color: var(--text-muted);">Status:</span>
            <span class="badge">● Online (Tomcat / Jetty + TCP Server)</span>
        </div>
        <div>
            <span style="color: var(--text-muted);">Total Keys:</span>
            <strong style="color: var(--text-main); margin-left: 0.25rem;"><%= totalKeysCount %></strong>
        </div>
    </div>

    <% 
        String result = (String) request.getAttribute("result");
        if (result != null) {
    %>
    <div class="result-box">
        <div class="result-title">Execution Result</div>
        <div class="result-content"><%= result %></div>
    </div>
    <% } %>

    <div class="grid">
        <!-- SET Command Card -->
        <div class="card">
            <div class="card-title">SET Key-Value</div>
            <form action="redis" method="POST">
                <input type="hidden" name="action" value="set">
                <div class="form-group">
                    <label for="set-key">Key</label>
                    <input type="text" id="set-key" name="key" placeholder="e.g. user:100" required>
                </div>
                <div class="form-group">
                    <label for="set-value">Value</label>
                    <input type="text" id="set-value" name="value" placeholder="e.g. John Doe" required>
                </div>
                <div class="form-group">
                    <label for="set-ttl">TTL in seconds (Optional)</label>
                    <input type="number" id="set-ttl" name="ttl" placeholder="e.g. 60">
                </div>
                <button type="submit" class="btn btn-red">Execute SET</button>
            </form>
        </div>

        <!-- GET & DELETE Command Card -->
        <div class="card">
            <div class="card-title">GET / DELETE Key</div>
            <form action="redis" method="GET" style="margin-bottom: 1.5rem;">
                <input type="hidden" name="action" value="get">
                <div class="form-group">
                    <label for="get-key">Key to Query</label>
                    <input type="text" id="get-key" name="key" placeholder="e.g. user:100" required>
                </div>
                <button type="submit" class="btn btn-blue">Execute GET</button>
            </form>

            <form action="redis" method="POST">
                <input type="hidden" name="action" value="delete">
                <div class="form-group">
                    <label for="del-key">Key to Delete</label>
                    <input type="text" id="del-key" name="key" placeholder="e.g. user:100" required>
                </div>
                <button type="submit" class="btn btn-outline-danger">Execute DELETE</button>
            </form>
        </div>
    </div>

    <!-- Active Keys Section -->
    <div class="card">
        <div class="card-title" style="display: flex; justify-content: space-between; align-items: center;">
            <span>Active Keys in Database</span>
            <form action="redis" method="POST" style="margin: 0;">
                <input type="hidden" name="action" value="flush">
                <button type="submit" class="btn btn-outline-danger" style="padding: 0.35rem 0.75rem; font-size: 0.8rem; width: auto;" onclick="return confirm('Flush entire database?')">Flush DB</button>
            </form>
        </div>
        <div class="keys-list">
            <%
                if (activeKeysSet != null && !activeKeysSet.isEmpty()) {
                    for (String k : activeKeysSet) {
            %>
                <span class="key-tag"><%= k %></span>
            <%
                    }
                } else {
            %>
                <span style="color: var(--text-muted); font-size: 0.9rem;">No keys stored in memory yet.</span>
            <% } %>
        </div>
    </div>
</div>

</body>
</html>
