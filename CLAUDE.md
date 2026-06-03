# CLAUDE.md

## Project Overview

Kotlin/JVM library for DNS over HTTPS (DoH). Publishes to GitHub Packages. Supports Cloudflare (1.1.1.1) and Google (8.8.8.8) resolvers.

## Common Commands

```bash
# Build
./gradlew build

# Run tests (makes real network calls to Cloudflare/Google)
./gradlew test

# Build fat/shadow JAR with shaded dependencies
./gradlew shadowJar

# Publish to GitHub Packages (requires USERNAME and TOKEN env vars)
./gradlew publish
```

## Architecture

```
DnsResolver (abstract)        ← HTTP + JSON parsing logic
  ├── CloudFlareDnsResolver   ← URL: https://1.1.1.1/dns-query
  └── GoogleDnsResolver       ← URL: https://dns.google/resolve
```

- `DnsQuery` — input: `name` (String) + `type` (RecordType enum)
- `DnsResponse` — mirrors the DNS-over-HTTPS JSON response fields
- `Constants` — resolver URLs and HTTP header strings

## Key Constraints

- **Java 8 source compatibility** — do not use APIs above Java 8
- **Shadow JAR relocates** `okhttp3`, `okio`, and `moshi` under `org.valarpirai.shaded.*` to avoid classpath conflicts for consumers
- **Tests are integration tests** — they hit real DNS servers; do not convert them to unit tests with mocks unless asked
- **`DnsResponse` is intentionally not a `data class`** — it implements `Serializable`; keep it as a regular class

## Gotchas

- `resolve()` uses `!!` on `toHttpUrlOrNull()` — any subclass overriding `getResolverUrl()` must return a valid URL or it will throw NPE
- Google DNS appends a trailing dot to names in responses (e.g. `"google.com."`) — CloudFlare does not; tests reflect this difference
- `RecordType` enum holds numeric DNS type codes (A=1, NS=2, CNAME=5, etc.)

## Publishing

CI publishes via GitHub Actions on release creation (`.github/workflows/gradle-publish.yml`). No CI runs on push or PRs.

Group: `org.valarpirai`, artifactId: `dns-over-https`, current version in `build.gradle.kts`.
