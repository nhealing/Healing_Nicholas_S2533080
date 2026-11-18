# Traffic Scotland Application

**Project:** Healing_Nicholas_S2533080  
**Module:** Mobile Platform Development  
**Language:** Java and XML  
**Minimum Android version:** Android 8.0 (API 26)

## About the application

For this coursework I created an Android application that downloads Unplanned Events and Current Roadworks information from Traffic Scotland.

The application displays the traffic situations in a list. The user can select a situation to view more details, search for a road name, or search for situations within a chosen distance of Glasgow City Centre. There is also a View Map button which displays markers for the situations currently shown in the list.

Unplanned Events are loaded when the application starts, and the selected feed is refreshed automatically every hour. The application also saves the downloaded XML so that previously downloaded traffic information can still be displayed without an internet connection.

## Opening the project

1. Open the `Healing_Nicholas_S2533080` folder in Android Studio.
2. Allow Gradle to sync and download any required dependencies.
3. Add the API credentials to `local.properties` as explained below.
4. Run the application on an emulator or Android device running Android 8.0 or later.

## API credentials

The application uses a Traffic Scotland client ID and client key to download the XML data. It also uses a Google Maps API key to display the map.

These values are read from `local.properties` in the main project folder. If the file is not already there, Android Studio can create it when the project is opened.

Add the following lines using valid credentials:

```properties
TRAFFIC_CLIENT_ID=YOUR_TRAFFIC_SCOTLAND_CLIENT_ID
TRAFFIC_CLIENT_KEY=YOUR_TRAFFIC_SCOTLAND_CLIENT_KEY
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
```

Keep the existing `sdk.dir` entry if there is one.

The real keys should not be included in the GitHub repository or the project ZIP. Without valid credentials, a fresh installation will not be able to download live Traffic Scotland information or use Google Maps correctly.

## How the code is organised

- `MainActivity.java` handles the buttons, searches, downloads, automatic refreshes and updating the screen.
- `TrafficXmlParser.java` uses `XmlPullParser` to read the XML and create `Situation` objects.
- `TrafficStorage.java` saves downloaded XML and reads it back from internal storage.
- `TrafficViewModel.java` keeps the traffic data when the screen is rotated.
- `Situation.java` and `TrafficLocation.java` store the information from the XML.
- `SituationAdapter.java` displays the situations in the ListView.
- `SituationDetailsFragment.java` displays the details of a selected situation.

The XML parsing and file storage were moved into separate classes to keep `MainActivity` more organised.

## Offline data

Unplanned Events and Current Roadworks are saved in separate XML files. The application can use these saved files if a download fails, but the relevant feed must have been downloaded successfully at least once on that installation.

## Map icons

I used seven icons from Google's Material Symbols to represent different types of traffic situations on Google Maps and in the ListView.

The icons are used for road or lane management, road obstructions, abnormal traffic and roadworks with different reported delays. I resized the icons to 64 × 64 pixels so they could be displayed clearly in the application.

**Source:** https://fonts.google.com/icons
