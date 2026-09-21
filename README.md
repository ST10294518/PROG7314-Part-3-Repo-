# Winx 

Github Link: https://github.com/EMKNDW/prog7314-g1-2026-prog7314-2026-poe-st10294518.git 

Video Link: https://youtu.be/eLnRsHEydFQ 

**Capture places. Cherish moments. Remember every journey.**

Winx is a native Android travel and food journal app built for the **PROG7314 / OPSC7312 (Programming 3D & Open Source Coding)** Portfolio of Evidence — Part 2: App Prototype Development. It pairs a Kotlin + Jetpack Compose Android client with a self-hosted **Winx.API** REST backend (ASP.NET Core) for storing and syncing travel entries and their media.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [1. Run the Winx.API Backend](#1-run-the-winxapi-backend)
  - [2. Run the Android App](#2-run-the-android-app)
- [REST API Reference](#rest-api-reference)
- [Authentication](#authentication)
- [Localisation](#localisation)
- [Known Limitations](#known-limitations)
- [Team](#team)

---

## Overview

Winx lets users log the places they've visited (and the meals they've had along the way), attach photos and videos, rate the experience, and browse everything back through a dashboard, calendar, and library view. All data is persisted through a custom REST API rather than a third-party BaaS, satisfying the POE requirement for a self-hosted backend.

## Features

-  **Authentication** — Email/password login & registration plus Google Sign-In (via Firebase Auth and Credential Manager)
-  **Dashboard** — At-a-glance view of recent entries with quick actions
-  **Entry management** — Create, view, edit, and delete travel entries (title, location, country, date, rating, notes)
-  **Media attachments** — Attach and preview photos/videos per entry (Coil for images, Media3 ExoPlayer for video)
-  **Calendar view** — Browse entries by date
-  **Library** — Central hub linking to entries, calendar, and settings
-  **Country picker** — Attach a country to each entry
-  **Settings suite** — Profile, Language, Notifications, Security, Recently Deleted, About Us
-  **Multi-language support** — In-app English / isiZulu translation toggle
-  **REST-backed persistence** — All entries and media are stored via the Winx.API backend (SQLite + Entity Framework Core)

## Tech Stack

**Android app** (`app/`)
- Kotlin, Jetpack Compose (Material 3), Navigation Compose
- Retrofit 2 + Gson for REST networking
- Coil for image loading, Media3 ExoPlayer for video playback
- Firebase Auth + Credential Manager / Google Identity for Google Sign-In
- Kotlin Coroutines
- Min SDK 24, Target/Compile SDK 36/37

**Backend API** (`Winx.API/`)
- ASP.NET Core Web API (.NET 10)
- Entity Framework Core with SQLite
- Swagger / OpenAPI (Swashbuckle) for API documentation

## Project Structure

```
PROG7314-Part-2-Repo/
├── app/                                # Android application module
│   └── src/main/java/com/winx/app/
│       ├── auth/                       # GoogleAuthManager
│       ├── data/remote/                # Retrofit services, DTOs, repositories
│       ├── navigation/                 # AppNavigation (single-activity NavHost)
│       ├── screens/                    # All Compose screens
│       └── ui/theme/                   # Colors, typography, theme
├── Winx.API/                           # ASP.NET Core backend
│   ├── Controllers/                    # EntriesController, MediaController
│   ├── Models/                         # TravelEntry, MediaItem
│   ├── Data/                           # AppDbContext
│   └── Migrations/                     # EF Core migrations
├── gradle/                             # Gradle wrapper & version catalog
├── build.gradle.kts
└── settings.gradle.kts
```

## Getting Started

### Prerequisites

- Android Studio (latest stable) with an emulator or physical device
- JDK 17
- .NET 10 SDK
- A Firebase project configured for this app (a `google-services.json` is already included in `app/`)

### 1. Run the Winx.API Backend

```bash
cd Winx.API
dotnet restore
dotnet ef database update      # applies migrations to winx.db (SQLite)
dotnet run
```

By default the API listens on `http://localhost:5001` (see `Properties/launchSettings.json`). Once running, Swagger UI is available at:

```
http://localhost:5001/swagger
```

### 2. Run the Android App

1. Open the project root in Android Studio.
2. Let Gradle sync (wrapper is included — no need to install Gradle separately).
3. Confirm the API base URL in `RetrofitClient.kt` matches your backend:
   ```kotlin
   private const val BASE_URL = "http://10.0.2.2:5001/"
   ```
   `10.0.2.2` is the Android Emulator's alias for `localhost` on the host machine. If you're running on a **physical device**, replace it with your machine's LAN IP address (and ensure both devices are on the same network).
4. Run the `app` configuration on an emulator or connected device (min SDK 24).

## REST API Reference

**Base route:** `api/`

| Method | Endpoint                    | Description                          |
|--------|------------------------------|---------------------------------------|
| GET    | `/api/Entries`               | Get all travel entries               |
| GET    | `/api/Entries/{id}`          | Get a single entry by ID             |
| POST   | `/api/Entries`                | Create a new entry                   |
| PUT    | `/api/Entries/{id}`          | Update an existing entry             |
| DELETE | `/api/Entries/{id}`          | Delete an entry                      |
| GET    | `/api/Media/entry/{travelEntryId}` | Get all media for a given entry |
| GET    | `/api/Media/{id}`            | Get a single media item by ID        |
| POST   | `/api/Media`                  | Add a media item (photo/video) to an entry |
| DELETE | `/api/Media/{id}`            | Delete a media item                  |

**Validation notes:**
- `Rating` must be between 1 and 5 (enforced on create and update).
- `MediaType` must be either `"Photo"` or `"Video"`.
- Media items require a valid, existing `TravelEntryId`.

## Authentication

Winx supports two sign-in paths:
- **Email/password** via the in-app Login/Register screens
- **Google Sign-In** via `GoogleAuthManager`, using Android Credential Manager + Google Identity Services backed by Firebase Auth

## Localisation

The app includes a lightweight, in-app translation layer (`WinxLanguage.kt`) that swaps UI strings between **English** and **isiZulu**, toggled from the Settings → Language screen without requiring an app restart.

## Known Limitations

- The Retrofit base URL is currently hardcoded for local development (emulator/LAN) rather than a deployed endpoint.
- Media files are referenced by file name/path rather than uploaded as binary content to the API.


Group project for **PROG7314 / OPSC7312** at The Independent Institute of Education (IIE).
