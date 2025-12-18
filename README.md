# NUMORA

## ข้อมูลโครงงาน

**รหัสโครงงาน:**  
67-2_07_ssr-s1

**ชื่อโครงงาน (ภาษาไทย):**  
ระบบจัดการและแสดงผลข้อมูลเชิงตัวเลขบนอุปกรณ์แอนดรอยด์

**Project Title (English):**  
NUMORA

**อาจารย์ที่ปรึกษาโครงงาน:**  
ผศ.ดร. ทรงศักดิ์ รองวิริยะพานิช

**ผู้จัดทำโครงงาน:**  
นาย ปัณณทัต มณีวงษ์ 6509650542  
สาขาวิชาวิทยาการคอมพิวเตอร์

---

## Manual / Instructions for the Project

### Project Overview

Numora is an Android application developed as part of **Special Project II (CS403)**.  
The application provides a structured and user-friendly way to manage and interact with numerical data on mobile devices.  
It is designed following Android development best practices, with a clear separation between data handling, application logic, and user interface components.

---

### Repository Structure

```
.
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/finalproject/
│   │   │   │   ├── database/     # Database and data access layer
│   │   │   │   ├── models/       # Data model classes
│   │   │   │   ├── modules/      # Core application logic
│   │   │   │   ├── ui/           # Activities and UI logic
│   │   │   │   └── utils/        # Utility classes
│   │   │   └── res/
│   │   │       ├── layout/       # XML layout files
│   │   │       ├── drawable/     # Drawable resources
│   │   │       ├── mipmap/       # Application icons
│   │   │       ├── values/       # Colors, styles, and strings
│   │   │       └── xml/          # Configuration files
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

> **Note:** Auto-generated directories such as `app/build`, `.gradle`, and `.idea` are omitted.

---

### System Requirements

- **Operating System:** Windows 10 or later  
- **Android Studio:** Latest stable version  
- **Android SDK:** API level 34 or higher  
- **Java JDK:** Version 17  
- **Gradle:** Included via Gradle Wrapper  

---

### Installation from Source Code

1. Clone the project repository:
   ```bash
   git clone <repository_url>
   ```

2. Open the project in Android Studio.

3. Allow Gradle to synchronize and download dependencies.

4. Select an Android emulator or connect a physical Android device.

5. Build and run the application.

---

### APK Installation

A pre-built debug APK is provided in the `apk/` directory for convenience.

#### On a Physical Device
- Copy `Numora-debug.apk` to the Android device.
- Enable **Install unknown apps** in device settings.
- Install and launch the application.

#### On Android Emulator
- Start an Android emulator from the AVD Manager in Android Studio.
- Drag and drop `Numora-debug.apk` into the emulator window.
- Wait for installation to complete and launch the application from the app drawer.

---

### Usage Instructions

- Launch the Numora application.
- Interact with the main interface.
- Navigate features using the provided UI components.
- The application processes numerical data according to its designed logic.

---

### Demo Video

A demonstration video showing the installation process and application usage is available at:

```
demo/68-1_CS403_67-2_07_ssr-s1_demo_link.txt
```

---

### Additional Notes

- The application was tested using Android Emulator (Pixel series).
- Recommended minimum Android version: Android 14 (API 34).
- The included APK is intended for demonstration and evaluation purposes only.