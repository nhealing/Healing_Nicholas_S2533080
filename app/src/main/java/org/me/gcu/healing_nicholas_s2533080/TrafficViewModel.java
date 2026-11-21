package org.me.gcu.healing_nicholas_s2533080;


import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import android.location.Location;

public class TrafficViewModel extends ViewModel {

    // Stores all situations from the selected traffic feed
    private ArrayList<Situation> allSituations = new ArrayList<>();

    // Stores the situations currently shown in the ListView
    private ArrayList<Situation> displayedSituations = new ArrayList<>();

    // Remembering which Traffic Scotland feed the user selected
    private String selectedFeedUrl;

    // Remembering whether the current traffic information came from saved XML
    private boolean showingSavedData = false;

    // Remembering when the displayed XML was last downloaded
    private long savedDataTime = 0;

    // Remembering whether a download is running when the screen rotates
    private boolean downloading = false;

    // Remembering whether the download should keep the current search
    private boolean keepSearchOnDownload = false;

    public boolean isDownloading() {
        return downloading;
    }

    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
    }

    public boolean isKeepSearchOnDownload() {
        return keepSearchOnDownload;
    }

    public void setKeepSearchOnDownload(boolean keepSearch) {
        keepSearchOnDownload = keepSearch;
    }

    // Fixed coordinates for Glasgow city centre
    private final double glasgowLatitude = 55.861;
    private final double glasgowLongitude = -4.252;


    // Save the complete list of downloaded situations
    public void setAllSituations(ArrayList<Situation> situations) {
        allSituations = new ArrayList<>(situations);
    }

    // Returning the full list of situations
    public ArrayList<Situation> getAllSituations() {
        return allSituations;
    }

    // Searching the downloaded situations for a matching road name
    public ArrayList<Situation> findSituationsByRoad(String road) {

        // A list to store the matching situations
        ArrayList<Situation> matchingSituations = new ArrayList<>();

        // Checking each situation from the downloaded traffic data
        for (Situation situation : allSituations) {

            // Check as some situations might not have location information
            if (situation.getLocation() != null) {

                String roadName = situation.getLocation().getRoadName();

                // Checking that a road name exists before comparing it
                if (roadName != null) {

                    // Ignore uppercase and lowercase differences
                    if (roadName.toLowerCase().contains(road.toLowerCase())) {
                        matchingSituations.add(situation);
                    }
                }
            }
        }

        // Return the situations that matched the searched road
        return matchingSituations;
    }

    // Calculates the distance between fixed location (Glasgow City Centre) and a traffic location
    // Returns true if the location is within the distance that was searched
    private boolean isWithinDistance(double latitude, double longitude,
                                     double distanceKm) {

        // Check that the coordinates are valid
        if (latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180
                || (latitude == 0 && longitude == 0)) {
            return false;
        }

        // Create a Location for Glasgow city centre
        Location glasgow = new Location("fixed");
        glasgow.setLatitude(glasgowLatitude);
        glasgow.setLongitude(glasgowLongitude);

        // Create a Location for the traffic situation
        Location trafficLocation = new Location("traffic");
        trafficLocation.setLatitude(latitude);
        trafficLocation.setLongitude(longitude);

        // Calculate the distance in metres and convert it to kilometres
        float distanceMetres = glasgow.distanceTo(trafficLocation);
        double calculatedDistance = distanceMetres / 1000.0;

        return calculatedDistance <= distanceKm;
    }


    // Finding situations that are within the chosen distance of Glasgow city centre
    public ArrayList<Situation> findSituationsWithinDistance(double distanceInKm) {

        // List to store situations within the users chosen distance
        ArrayList<Situation> nearbySituations = new ArrayList<>();

        // Checking each situation from the downloaded traffic data
        for (Situation situation : allSituations) {

            TrafficLocation location = situation.getLocation();

            // Check in case situations does not have location information
            if (location != null) {

                boolean nearby = false;

                // Checking the starting coordinates
                Double fromLat = location.getFromLatitude();
                Double fromLon = location.getFromLongitude();

                if (fromLat != null && fromLon != null) {
                    nearby = isWithinDistance(fromLat, fromLon, distanceInKm);
                }

                // Checking the ending coordinates if the starting point was not nearby
                Double toLat = location.getToLatitude();
                Double toLon = location.getToLongitude();

                if (!nearby && toLat != null && toLon != null) {
                    nearby = isWithinDistance(toLat, toLon, distanceInKm);
                }

                // Adding the situation if either point is nearby
                if (nearby) {
                    nearbySituations.add(situation);
                }
            }
        }

        return nearbySituations;
    }

    // Save the situations currently displayed on screen
    public void setDisplayedSituations(ArrayList<Situation> situations) {
        displayedSituations = new ArrayList<>(situations);
    }

    // Return the situations currently displayed on screen
    public ArrayList<Situation> getDisplayedSituations() {
        return displayedSituations;
    }

    // Returning the currently selected URL
    public String getSelectedFeedUrl() {
        return selectedFeedUrl;
    }

    // Updating the selectedURL
    public void setSelectedFeedUrl(String url) {
        selectedFeedUrl = url;
    }

    // Returning whether the data is live or came from internal storage
    public boolean isShowingSavedData() {
        return showingSavedData;
    }

    // Setting whether the data is live or came from internal storage
    public void setShowingSavedData(boolean showingSavedData) {
        this.showingSavedData = showingSavedData;
    }

    // Return the date and time of the saved XML
    public long getSavedDataTime() {
        return savedDataTime;
    }

    // Remember the date and time of the saved XML
    public void setSavedDataTime(long time) {
        savedDataTime = time;
    }
}

