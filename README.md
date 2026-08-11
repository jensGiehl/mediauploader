# Media Uploader

[![Publish Docker image](https://github.com/jensGiehl/mediauploader/actions/workflows/docker-publish.yml/badge.svg)](https://github.com/jensGiehl/mediauploader/actions/workflows/docker-publish.yml)

Media Uploader is a mobile-friendly Spring Boot web application for collecting photos and videos. Visitors authenticate with a single shared password and can then upload one or more media files through a full-screen, responsive interface with drag-and-drop support and upload progress.

The application stores a signed authentication token in an `HttpOnly` cookie, so a visitor normally signs in only once per device. The password itself is never written to the cookie. Uploaded files are stored on disk and receive the current date as a `yyyyMMdd_` prefix. Existing files are not overwritten; a numeric suffix is added when necessary.

## Features

- Responsive photo and video upload page
- Automatic English/German interface based on the browser language
- Shared-password authentication using Spring Security
- Signed, persistent, `HttpOnly` authentication cookie
- Multiple-file selection and upload progress
- Disk-backed multipart processing for large files
- Configurable upload directory and size limits
- Optional weekly Telegram reminder when files are waiting on the server
- Docker image publishing to GitHub Container Registry on pushes to `main` or `master`

## Run with Docker

Build the image locally:

```bash
docker build -t media-uploader .
```

Create a directory on the host and start the container:

```bash
mkdir -p ./uploads

docker run -d \
  --name media-uploader \
  --restart unless-stopped \
  -p 8080:8080 \
  -v "$(pwd)/uploads:/data/uploads" \
  -e UPLOAD_PASSWORD="choose-a-strong-password" \
  -e UPLOAD_COOKIE_SECRET="replace-with-a-long-random-secret" \
  media-uploader
```

Open <http://localhost:8080>. The `-p 8080:8080` option exposes the application on host port `8080`. Change the first number, for example to `-p 9090:8080`, to use a different host port.

The `-v "$(pwd)/uploads:/data/uploads"` option maps the local `uploads` directory to the container. Uploaded files therefore remain on the host when the container is replaced or removed.

PowerShell equivalent:

```powershell
New-Item -ItemType Directory -Force uploads

docker run -d `
  --name media-uploader `
  --restart unless-stopped `
  -p 8080:8080 `
  -v "${PWD}/uploads:/data/uploads" `
  -e UPLOAD_PASSWORD="choose-a-strong-password" `
  -e UPLOAD_COOKIE_SECRET="replace-with-a-long-random-secret" `
  media-uploader
```

## Use the image from GitHub Container Registry

Every push to `main` or `master` in [jensGiehl/mediauploader](https://github.com/jensGiehl/mediauploader) publishes these tags:

- `ghcr.io/jensgiehl/mediauploader:latest`
- `ghcr.io/jensgiehl/mediauploader:sha-COMMITSHA`

Pull and run the published image:

```bash
docker pull ghcr.io/jensgiehl/mediauploader:latest

docker run -d \
  --name media-uploader \
  -p 8080:8080 \
  -v "$(pwd)/uploads:/data/uploads" \
  -e UPLOAD_PASSWORD="choose-a-strong-password" \
  -e UPLOAD_COOKIE_SECRET="replace-with-a-long-random-secret" \
  ghcr.io/jensgiehl/mediauploader:latest
```

The first package version can be private depending on the repository and organization settings. Change the package visibility in GitHub Packages if anonymous pulls should be allowed.

## Configuration

| Environment variable | Default | Description |
|---|---|---|
| `UPLOAD_PASSWORD` | required | Shared password used to access the upload page |
| `UPLOAD_COOKIE_SECRET` | required | Secret key used to sign authentication cookies; use a long random value |
| `UPLOAD_DIRECTORY` | `Upload` | Directory in which uploaded files are stored; the Docker image sets this to `/data/uploads` |
| `UPLOAD_SECURE_COOKIE` | `false` | Set to `true` when the application is served over HTTPS |
| `TELEGRAM_ENABLED` | `false` | Enables the weekly Telegram upload reminder |
| `TELEGRAM_BOT_TOKEN` | empty | Bot token from BotFather; required when Telegram is enabled |
| `TELEGRAM_CHAT_ID` | empty | Target user, group, supergroup, or channel chat ID |
| `TELEGRAM_API_BASE_URL` | `https://api.telegram.org` | Telegram Bot API base URL |
| `TELEGRAM_CRON` | `0 0 20 * * WED` | Spring cron expression for Wednesday at 20:00 |
| `TELEGRAM_ZONE` | `Europe/Berlin` | Time zone used to evaluate the reminder schedule |
| `TELEGRAM_MESSAGE_TEMPLATE` | `Es gibt {0} Dateien auf dem Server.` | Message template; `{0}` is replaced with the file count |
| `MAX_FILE_SIZE` | `10GB` | Maximum size of one uploaded file |
| `MAX_REQUEST_SIZE` | `10GB` | Maximum size of the complete multipart request |

The reminder only sends a message if the upload directory contains at least one regular file. For example:

```bash
-e TELEGRAM_ENABLED=true \
-e TELEGRAM_BOT_TOKEN="123456:replace-with-your-token" \
-e TELEGRAM_CHAT_ID="-1001234567890"
```

## Run from source

Requirements: Java 17 or newer. The Maven Wrapper is included, so a separate Maven installation is not required.

Clone the repository:

```bash
git clone https://github.com/jensGiehl/mediauploader.git
cd mediauploader
```

```bash
export UPLOAD_PASSWORD="choose-a-strong-password"
export UPLOAD_COOKIE_SECRET="replace-with-a-long-random-secret"
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
$env:UPLOAD_PASSWORD = "choose-a-strong-password"
$env:UPLOAD_COOKIE_SECRET = "replace-with-a-long-random-secret"
.\mvnw.cmd spring-boot:run
```

Run tests and create the executable JAR:

```bash
./mvnw clean test
./mvnw package
```
