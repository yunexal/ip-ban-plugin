# IPBanPlugin

A lightweight, high-performance IP ban system for Velocity Proxy servers.

## 🚀 Features

- **Ultra Lightweight**: Weighs only ~350KB (no external database drivers required).
- **JSON Storage**: Simple, human-readable storage in `banned_ips.json`. No database setup needed.
- **High Performance**: 
  - Instant ban checks (0ms latency) using in-memory caching.
  - Asynchronous file I/O to prevent server lag.
- **Full UTF-8 Support**: Correctly handles Cyrillic and special characters in ban reasons and messages.
- **Instant Kick**: Automatically kicks players who are online when their IP is banned.

## 📥 Installation

1. Download the latest `ipbanplugin-x.x.x-all.jar` from the releases page (or build it yourself).
2. Place the jar file into your Velocity `plugins/` folder.
3. Restart the proxy.

## 🛠 Commands & Permissions

| Command | Usage | Description | Permission |
|---------|-------|-------------|------------|
| `/ipban` | `/ipban` | Lists all banned IPs. | `ipbanplugin.ban` |
| `/ipban` | `/ipban <ip> [reason]` | Bans a specific IP address. | `ipbanplugin.ban` |
| `/unbanip` | `/unbanip <ip>` | Unbans a specific IP address. | `ipbanplugin.unban` |

## ⚙️ Configuration

The plugin generates a configuration folder at `plugins/ipbanplugin/`.

### `config.yml`
Basic configuration settings.

### `messages.properties`
Fully customizable messages with support for color codes (`&`) and placeholders.
*Note: The file is read as UTF-8, so you can use any language.*

### `banned_ips.json`
Stores the ban data.
```json
[
  {
    "ip": "127.0.0.1",
    "bannedBy": "Admin",
    "bannedAt": "2026-01-01 12:00:00"
  }
]
```

## 🏗 Building from Source

Requirements:
- Java 17 (JDK)
- Gradle

```bash
./gradlew build
```
The artifact will be located in `build/libs/ipbanplugin-1.0.1-all.jar`.

## 📋 Requirements

- Velocity 3.3.0 or higher
- Java 17 or higher

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

