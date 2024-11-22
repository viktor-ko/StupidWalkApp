


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
