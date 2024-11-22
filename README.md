
##  Getting Started

###  Add project to Android Studio 
1. Download the project: Click the green `Code` button, choose Download ZIP and extract it, OR copy the URL for cloning (https://github.com/viktor-ko/StupidWalkApp.git).
2. Open Android Studio: from the welcome screen, select "Get from Version Control" (or go to File > New > Project from Version Control in an existing session). Paste the repository URL into the field, select a folder where the project will be saved, and click `Clone`.
3. Wait for Gradle Sync: After cloning or opening the project, Android Studio will automatically run a Gradle sync. Ensure all dependencies are downloaded without errors.
4. Run the project: Connect an Android device or start an emulator. Select the desired configuration and click `Run` (green ▶️ icon).

###  APK Installation
Alternatively, you can install StupidWalkApp directly on any Android device:

1. Click [this link]( https://github.com/viktor-ko/StupidWalkApp/raw/refs/heads/main/StupidWalk.apk) from Android device to download the APK file.
2. If the link doesn’t work, go to the repository, find the StupidWalk.apk file, and click `Download raw file` icon.
3. Once downloaded, locate the APK file on your device and tap the APK file to begin installation. If prompted, allow installations from unknown sources in your device's settings.

## Project structure

```sh
StupidWalkApp/
├── StupidWalk.apk: APK file to install
└── app
    └── src/main
        ├── java/com/viktor/walkapp
        │   ├── MainActivity.java: app entry point, initializes the map and generates random walking routes
        │   ├── RoutesActivity.java: displays a list of saved walking routes. Users can click on a route to view it on the map
        │   ├── MapActivity.java: Displays a selected walking route on the map (navigator mode)
        │   ├── RouteListAdapter.java: Adapter displays database route information as a list view
        │   └── DatabaseHelper.java: Manages the SQLite database, including creating tables, inserting data, and updating addresses
        ├── res: project resources - fonts, images, XML layouts, etc
        └──  AndroidManifest.xml: app configuration    
```
The app is inspired by this legendary meme:

<img src="https://github.com/noeigenschaften/StupidWalkApp/assets/36310815/c09f57d7-ef90-490d-9f81-32cd58e3337c" width="500">
