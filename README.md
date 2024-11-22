# Stupid Walk App
https://github.com/user-attachments/assets/4722b07d-7d23-4f95-bb17-36eb87130ad4

This simple Android app encourages you to finally take that stupid walk for your stupid physical and mental health, freeing you from the torment of choosing where to go. With just one click, you will instantly get three unique walking routes to randomly selected destinations within a 15-30-minute walk of your current location. It’s as easy as that: stop thinking, start walking!

The app was created for the "Mobile Cartography" course of the Cartography MSc programme at the Technical University of Dresden (TUD) in the WS 2023/24. Its idea evidently originated from this classic meme:

<img src="https://github.com/noeigenschaften/StupidWalkApp/assets/36310815/c09f57d7-ef90-490d-9f81-32cd58e3337c" width="500">

## Functionality 
The app uses Google Maps to display the user's current location and generate random walking routes within a specified distance. Users can view the routes on a map, see detailed information about each route, and access a list of saved routes.
Activities
MainActivity
Functionality:
Initializes the map and location services.
Fetches the user's current location and displays it on the map.
Generates random walking routes within a specified distance from the current location.
Displays the generated routes on the map and saves them to the database.
Provides buttons to start walking and view the list of saved routes.
RoutesActivity
Functionality:
Displays a list of saved walking routes from the database.
Allows users to click on a route to view it on the map in MapActivity.
MapActivity
Functionality:
Displays a specific walking route on the map.
Shows start and end markers for the route.
Adjusts the camera to fit the route within the view.
Fetches and displays the route using the OpenRouteService API.
Other Important Files
RouteListAdapter
Functionality:
Custom adapter for displaying route information in a list view.
Binds route data (address, distance, duration) to the list item views.
DatabaseHelper
Functionality:
Manages the SQLite database.
Creates the database and tables.
Inserts route data (start and end coordinates, distance, duration) into the database.
Updates the address for a route.
Clears the database.

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

