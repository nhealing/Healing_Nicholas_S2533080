package org.me.gcu.healing_nicholas_s2533080;


import androidx.lifecycle.ViewModel;
import java.util.ArrayList;

public class TrafficViewModel extends ViewModel {

    // Stores all situations from the selected traffic feed
    private ArrayList<Situation> allSituations = new ArrayList<>();

    // Stores the situations currently shown in the ListView
    private ArrayList<Situation> displayedSituations = new ArrayList<>();

    // Remember which Traffic Scotland feed the user selected
    private String selectedFeedUrl;


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
}

