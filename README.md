<p align="center">
  <img alt="Stars" src="https://img.shields.io/github/stars/mhss1/mybrain?color=d48600&style=for-the-badge" />
  <a href="https://www.gnu.org/licenses/gpl-3.0"><img alt="License: GPL v3" src="https://img.shields.io/badge/License-GPLv3-blue.svg?style=for-the-badge" /></a>
  <a href="https://github.com/mhss1/MyBrain/releases"><img src="https://img.shields.io/github/downloads/mhss1/MyBrain/total?logo=github&style=for-the-badge" alt="Downloads"></a>
</p>


<h1 align="center">
  <a href="https://github.com/mhss1/MyBrain">
    <img alt="My Brain" src="https://github.com/user-attachments/assets/3a33b973-0686-4ac5-b987-41bfb081ba81" width="180" />
  </a>
  <br>
  My Brain
</h1>
  
<h3 align="center">All-in-one productivity app and AI assistant with Tasks, Notes, Calendar, Diary and Bookmarks.</h3>

![MyBrain banner](https://github.com/user-attachments/assets/4dabc54e-5925-4fd5-a940-fef4b2741787)

[screenshots made by previewed](https://previewed.app/template/00CBF3F6)

[<img src="https://github.com/ImranR98/Obtainium/blob/main/assets/graphics/badge_obtainium.png"
alt="Get it on Obtainium"
height="80">](https://apps.obtainium.imranr.dev/redirect.html?r=obtainium://add/https://github.com/mhss1/MyBrain)
[<img src="https://github.com/mhss1/MyBrain/assets/58703865/94cbf557-b1a9-4339-b6b4-def21dde3c11"
     alt="Get it on GitHub"
     height="80">](https://github.com/mhss1/MyBrain/releases/latest)

> **F-Droid notice:** My Brain is being discontinued on F-Droid because future releases will introduce native dependencies that would add significant build and maintenance complexity, and may use proprietary components such as Google ML Kit that are incompatible with F-Droid's policy. If you currently use the F-Droid version, export your data first, then install the latest version from [GitHub Releases](https://github.com/mhss1/MyBrain/releases/latest) and import your data.
      
## Features

- Local and private, with no data collection and a focus on performance and memory efficiency.
- Encrypted Local device sync for tasks, notes, diary entries, bookmarks, and AI conversations over the same local network.
- Create tasks with priority, sub-tasks, due date and reminders.
- Create Notes that supports markdown which enables you to use Headers, lists, links etc..
- Record your mood daily and view your mood summary with beautiful graphs.
- Save bookmarks for later by using the share menu from any other app.
- View your calendar events in a modern list and monthly views with a beautiful home screen widget.
- Dashboard screen to see your tasks, calendar events, and more to see your day at a glance.
- AI assistant and Agent to help you with your daily workflow. You can chat and attach objects and also ask the AI to create and find items for you.

## Technologies

- Kotlin Multiplatform, currently targeting Android.
- Multi-module project.
- Compose Multiplatform with Material 3.
- Widgets made with Jetpack Glance.
- Following Clean Architecture approach. 
- MVI Design Pattern.
- Room DB
- Jetpack Paging
- Koin
- Ktor Client for API requests and local sync connections.
- Ktor Server with the CIO engine for hosting local sync endpoints.
- Ktor WebSockets for live sync communication.
- Preferences DataStore
- Kotlin coroutines
- Kotlin Flows
- Kotlinx Serialization
- Kotlinx DateTime
- WorkManager
- Alarm Manager
- Content Provider
- Biometric Authentication
- Koog
- Google ML Kit GenAI Prompt API for on-device AI.
- Android App Functions
- Android Network Service Discovery (NSD) for local device discovery.
- AES-GCM encryption for local sync transfers.
- Zstandard compression for sync payloads.
- ZXing for pairing QR codes.

## Translation
Project localisation is managed via [Crowdin](https://crowdin.com/project/my-brain-app)
[![Crowdin](https://badges.crowdin.net/my-brain-app/localized.svg)](https://crowdin.com/project/my-brain-app)

## Contributing
To get started, take a look at [CONTRIBUTING.md](CONTRIBUTING.md).

---
Icons Attribution:
<a href="https://www.flaticon.com/free-icons/document" title="document icons">Document icons created by Freepik - Flaticon</a>
<a href="https://www.flaticon.com/free-icons/list" title="list icons">List icons created by Freepik - Flaticon</a>
<a href="https://www.flaticon.com/free-icons/3d-calendar" title="3d calendar icons">3d calendar icons created by Freepik - Flaticon</a>
<a href="https://www.flaticon.com/free-icons/bookmark" title="bookmark icons">Bookmark icons created by Freepik - Flaticon</a>
<a href="https://www.flaticon.com/free-icons/book" title="book icons">Book icons created by Freepik - Flaticon</a>
<a href="https://www.flaticon.com/free-icons/chatbot" title="chatbot icons">Chatbot icons created by HideMaru - Flaticon</a>
