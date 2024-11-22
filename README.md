![inspiration](https://github.com/noeigenschaften/StupidWalkApp/assets/36310815/c09f57d7-ef90-490d-9f81-32cd58e3337c)


## Project structure

```sh
└── StupidWalkApp/
    ├── `StupidWalk.apk`: Installable APK
    └── app
        └── src/main
            ├── java/com/viktor/walkapp
            │   ├── `MainActivity.java`: app entry point, initializes the map, fetches the user's current location, generates random walking routes, and displays them on the map.
            │   ├── `RoutesActivity.java`: displays a list of saved walking routes. Users can click on a route to view it on the map.
            │   ├── `MapActivity.java`: Displays a specific walking route on the map, including start and end markers and a polyline connecting them.
            │   ├── `RouteListAdapter.java`: Adapter for displaying database with routes information in a list view
            │   └── `DatabaseHelper.java`: Manages the SQLite database, including creating tables, inserting data, and updating addresses
            ├── `res`: project resources - fonts, images, xml layouts, etc.
            └──  `AndroidManifest.xml`: app configuration    
```