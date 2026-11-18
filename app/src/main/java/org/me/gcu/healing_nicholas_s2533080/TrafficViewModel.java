package org.me.gcu.healing_nicholas_s2533080;


import androidx.lifecycle.ViewModel;
import java.util.ArrayList;

public class TrafficViewModel extends ViewModel {

    // Stores all situations from the selected traffic feed
    private ArrayList<Situation> allSituations = new ArrayList<>();

    // Stores the situations currently shown in the ListView
    private ArrayList<Situation> displayedSituations = new ArrayList<>();

    // Remembering which Traffic Scotland feed the user selected
    private String selectedFeedUrl;

    // Remembering whether the current traffic information came from saved XML
    private boolean showingSavedData = false;

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


    // Save the complete list of downloaded situations
    public void setAllSituations(ArrayList<Situation> situations) {
        allSituations = new ArrayList<>(situations);
    }

    // Return the complete list of situations
    public ArrayList<Situation> getAllSituations() {
        return allSituations;
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
}

