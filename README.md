# Numora

**Numora** is an Android application developed as part of **Special Project II (CS403)**.  
It provides a structured and user-friendly way to manage and interact with numerical data on mobile devices.

[![Android](https://img.shields.io/badge/Platform-Android-green?style=flat&logo=android)](https://www.android.com/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-34-blue)](https://developer.android.com/about/versions/14)
[![Android Studio](https://img.shields.io/badge/Built%20with-Android%20Studio-3DDC84?style=flat&logo=android-studio)](https://developer.android.com/studio)

## Overview

Numora focuses on simplicity and usability while handling numerical information.  
The application follows Android development best practices, with a clear separation between data handling, business logic, and user interface components.

## Repository Structure

```
.
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/finalproject/
│   │   │   │   ├── database/     # Database and data access layer
│   │   │   │   ├── models/       # Data model classes
│   │   │   │   ├── modules/      # Core application logic
│   │   │   │   ├── ui/           # Activities, Fragments, and UI logic
│   │   │   │   └── utils/        # Utility and helper classes
│   │   │   └── res/
│   │   │       ├── layout/       # XML layout files
│   │   │       ├── drawable/     # Drawable resources
│   │   │       ├── mipmap/       # Application icons
│   │   │       ├── values/       # Colors, styles, and string resources
│   │   │       └── xml/          # Configuration XML files
│   │   ├── test/                 # Unit tests
│   │   └── androidTest/          # Instrumented tests
│   └── build.gradle
├── apk/
│   └── Numora-debug.apk          # Pre-built APK file
├── demo/
│   └── 68-1_CS403_67-2_07_ssr-s1_demo_link.txt # Demo video link
├── gradle/
│   └── wrapper/
└── README.md
```

> Note: Auto-generated directories such as `app/build`, `.gradle`, and `.idea` are omitted for clarity.

## Prerequisites

To build and run the project from source, you need:

- Operating System: Windows 10 or later
- Android Studio (Otter or newer)
- Android SDK (API level 34 or higher recommended)
- Java JDK 17
- Gradle (included via wrapper)

## Installation from Source Code

1. Clone the repository:
   ```bash
   git clone <repository_url>
   ```

2. Open the project in **Android Studio**.

3. Allow Gradle to sync and download dependencies.

4. Select an Android emulator or connect a physical Android device.

5. Build and run the application.

## APK Installation

A pre-built debug APK is provided in the `apk/` directory for convenience.

### On a Physical Device
1. Transfer `Numora-debug.apk` to your Android device.
2. Enable **Install unknown apps** in device settings.
3. Install the APK and launch the application.

### On Android Emulator (in Android Studio)
1. Start your Android emulator from the AVD Manager in Android Studio.
2. Drag and drop the `Numora-debug.apk` file directly onto the emulator window.
3. The installation will begin automatically. Once complete, launch the application from the emulator's app drawer.

## Usage

1. Launch the Numora application.
2. Interact with the main interface.
3. Navigate features using the provided UI components.
4. Data is processed and managed according to the application's logic.

## Demo Video

A demonstration video showing the installation process and application usage is available at:  
`demo/68-1_CS403_67-2_07_ssr-s1_demo_link.txt`

## Team Members

- Punnatut Maneewong (6509650542)

## Notes

- The application was tested using Android Emulator (Pixel series).
- Recommended minimum Android version: Android 14 (API 34).
- The included APK is intended for demonstration and evaluation purposes only.