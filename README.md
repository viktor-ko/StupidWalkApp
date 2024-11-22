# Stupid Walk App


This simple Android app encourages you to finally take that stupid walk for your stupid physical and mental health, freeing you from the torment of choosing where to go. With just one click, you will instantly get three unique walking routes to randomly selected destinations within a 15-30-minute walk of your current location. This app will be super handy if you find it hard to wander around with no specific purpose: you will get a precise point on the map where you need to go, and a sense of accomplishment when you reach it. It’s as easy as that: stop thinking, start walking!

https://github.com/user-attachments/assets/4722b07d-7d23-4f95-bb17-36eb87130ad4

The app was created for the "Mobile Cartography" course of the Cartography MSc programme at the Technical University of Dresden (TUD) in the WS 2023/24. It is inspired by this classic meme:

<img src="https://github.com/noeigenschaften/StupidWalkApp/assets/36310815/c09f57d7-ef90-490d-9f81-32cd58e3337c" width="500">

## Functionality 
The app uses a custom-style Google basemap to show the user's current location and generate three random points within 1-1.5 km and routes to them using OpenRouteService API. Users can view the routes on a map, see the reversely geocoded address of each point by clicking on the marker, and access a list of generated routes (list cleared when the app is launched).

When a user clicks the `STUPID WALK` button, the app:
- Generates three random points within 1-1.5 km (Euclidian distance) from the user's current location.
- Add markers on each random point.
- Displays walking routes from the user's current location to each random point, using polyline in geojson requested via openrouteservice API.
- Parses each geojson to get the route distance and duration information from its structure.
- Converts route distance from meters to kilometers and duration from seconds to minutes.
- Reverse geocodes the coordinates of random points to get the closest address for each point.
- Inserts start and end coordinates, distance, duration, and address into the SQLite database.
  
When a user clicks the `Routes` button, the `RoutesActivity` is triggered. On the routes screen user can review the details of generated walking routes in a list view, based on the data from the database. Also, a `Show map` button at the bottom of the screen takes the user back to the main map screen when clicked.

Clicking on any item from the list triggers `MapActivity` and takes a user to the third screen. This activity:
- Accesses the database to get the start and end coordinates associated with the item from the clicked list.
- Opens the map again and adds the markers on start and end points.
- Sends another request to the openrouteservice API to display the route between the points.
- Calculates the bearing between the start and end points.
- Rotates the camera position using the calculated bearing, ensuring that the end point is always at the top of the device screen (navigator mode)

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
├── StupidWalk.apk: APK file to install on Android device
└── app
    └── src/main
        ├── java/com/viktor/walkapp
        │   ├── MainActivity.java: app entry point, initializes the map and location services, generates random walking routes
        │   ├── RoutesActivity.java: displays a list of saved walking routes. Users can click on a route to view it individually
        │   ├── MapActivity.java: Displays a selected walking route on the map, adjusts map view like navigator mode
        │   ├── RouteListAdapter.java: Adapter that binds routes data from the database to the list view
        │   └── DatabaseHelper.java: Manages SQLite database: creates a table, inserts route data, clears the table on app launch 
        ├── res: app resources - fonts, icons, XML layouts, etc
        └──  AndroidManifest.xml: app configuration    
```

