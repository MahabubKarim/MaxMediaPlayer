# Max Media Player 🎵

[![Kotlin Version](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org)
[![Android API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://developer.android.com/about/versions)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange.svg)](https://opensource.org/licenses/Apache-2.0)

A modern Android music streaming app with offline capabilities, built using cutting-edge Android technologies following Clean Architecture principles.

<p align="center">
  <img src="screenshots/app_demo.gif" width="300" alt="App Demo">
</p>

## Features ✨

- 🎧 **Audio Streaming** from Jamendo API
- 📥 **Offline Caching** with Room Database
- 📑 **Playlist Management** (Create/Edit/Delete)
- 🎮 **Media Controls** in Notification & Lock Screen
- 🎨 **Material 3** Dynamic Theming
- 🔍 **Search Functionality**
- 🔄 **Background Audio Playback**
- ♻️ **Network State Awareness**

## Tech Stack 🛠️

### Core Components
| Component               | Technology                          |
|-------------------------|-------------------------------------|
| **UI Framework**        | Jetpack Compose                     |
| **Audio Engine**        | Android Media3 (ExoPlayer)          |
| **Database**           | Room + SQLite                       |
| **DI Framework**       | Hilt with KSP                       |
| **Networking**         | Retrofit 2 + OkHttp                 |
| **Image Loading**      | Coil                                |
| **Navigation**         | Compose Navigation                  |

### Architecture
com.mmk.maxmediaplayer/
├── data/ # Data layer (Repositories, DAOs)
├── domain/ # Business logic (Models, UseCases)
├── presentation/ # UI (Composables, ViewModels)
├── di/ # Dependency Injection
└── service/ # Background services

## Screenshots 📱

<div style="display: flex; justify-content: space-between;">
  <img src="screenshots/player_light.png" width="30%" alt="Player Light">
  <img src="screenshots/playlist_view.png" width="30%" alt="Playlist View">
  <img src="screenshots/player_dark.png" width="30%" alt="Player Dark">
</div>

## Getting Started 🚀

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 34
- Jamendo API key (free tier available)

### Installation
1. Clone the repository:
   ```bash
   git clone [https://github.com/yourusername/MaxMediaPlayer.git](https://github.com/MahabubKarim/MaxMediaPlayer.git)
2. Obtain a Jamendo API key from Jamendo Developer Portal

3. Add your API key to local.properties: JAMENDO_API_KEY="your_api_key_here"
4. Build and run the app!
<div align="center"> <p>Made with ❤️ for music lovers</p> <p>Give a ⭐ if you like this project!</p> </div>
