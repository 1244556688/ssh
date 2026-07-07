# MySSH — Professional Android SSH Client

A professional-grade SSH client for Android, designed with a sleek glassmorphic UI matching modern desktop elements (Windows 11 & macOS) and backed by a robust, secure, and offline-first architecture.

---

## 🎨 Design Vision & Theme
MySSH aims to merge high usability with beautiful visuals:
- **Glassmorphic Aesthetic**: Translucent overlay cards, blur gradients, and subtle depth layers built via Jetpack Compose canvas styling.
- **Material You Dynamic Coloring**: Adaptive themes corresponding to device wallpaper colors, defaulting to a gorgeous dark cosmic theme.
- **Responsive Layout**: Designed multi-pane layout structures for tablets, foldables, and standard mobile viewports.

---

## 🏗️ Architecture & Technical Stack
The project adheres to modern Android developer guidance and clean architecture principles:
- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Asynchrony**: Kotlin Coroutines & Flow
- **Local Database**: Room for secure, encrypted persistence
- **Security**: Android Keystore for master-key biometric cryptographic wrappers
- **SSH Library**: `com.github.mwiede:jsch` (highly updated, actively maintained fork of JSch supporting modern secure key formats and algorithms)

---

## 🚦 Roadmap & Phases

### 📦 Phase 1: Project Infrastructure & CI/CD (Current)
- [x] Initial codebase bootstrapping, package-specific structures.
- [x] Version Catalog (`libs.versions.toml`) dependency centralization.
- [x] Manifest setup with internet, network, and biometric security permissions.
- [x] Automated CI/CD build configuration using GitHub Actions to output release-ready debug APKs.

### 🎨 Phase 2: UI Foundation & Connection Manager (Upcoming)
- Setup theme palette, shapes, glassmorphism modifiers, and interactive components.
- Main dashboard showing saved sessions, recent connections, and real-time host reachability (ping/latency checks).
- Multi-column layout configuration for tablets.

### 🔒 Phase 3: Cryptographic Storage & Biometrics
- Implementation of Room Database for connection credentials.
- Android Keystore binding with Fingerprint/Face authentication to safeguard stored secrets and SSH keys.

### ⚡ Phase 4: Core Terminal & SFTP Client
- Building an interactive terminal with ANSI color support, custom cursor, custom keyboard modifiers (Ctrl, Alt, Tab).
- Dual-pane SFTP client for visual file manipulations (upload, download, edit, rename, permissions).

### 📊 Phase 5: Live Monitoring Dashboard
- System health graphs capturing remote host CPU, RAM, Disk, and Network IO in real-time.

---

## ⚙️ How to Build
To build the project locally, ensure you have Android Studio installed:
1. Clone the repository.
2. Open with Android Studio.
3. Sync Gradle and run on an Android 8.0 (API level 26)+ device or emulator.

Alternatively, our **GitHub Actions Workflow** automatically runs a compilation check on every push to the `main` branch, generating a downloadable `app-debug.apk` directly in the workflow artifacts!

---

## 📄 License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
