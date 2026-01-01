# Changelog

## [1.0.1] - 2026-01-01

### Added
- **JSON Storage**: Replaced SQLite with a lightweight JSON-based storage system.
- **Async I/O**: All file operations (save/load) are now performed asynchronously to prevent server lag.
- **Cyrillic Support**: Enforced UTF-8 encoding for `messages.properties` and `banned_ips.json` to correctly handle Cyrillic characters in ban reasons and messages.

### Changed
- **Plugin ID**: Renamed plugin ID to `ipbanplugin` and main class to `IPBanPlugin`.
- **Dependency Removal**: Removed `sqlite-jdbc` dependency to drastically reduce file size.
- **File Size**: Optimized plugin size from ~13MB to ~350KB.
- **Velocity API**: Updated to target Velocity 3.4.0-SNAPSHOT.

### Fixed
- **Encoding Issues**: Fixed issues where Cyrillic characters might appear as question marks or garbage text.
- **Java 9+ Compatibility**: Removed complex classloader hacks required for SQLite, ensuring better compatibility with modern Java versions (Java 17+).

## [1.0.0] - Initial Release
- Basic IP Ban functionality.
- SQLite support (Deprecated).
- Command system (`/ipban`, `/unbanip`).
- Configurable messages.
