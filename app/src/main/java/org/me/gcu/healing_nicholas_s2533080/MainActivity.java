//
// Name                 Nicholas Healing
// Student ID           S2533080
// Programme of Study   Software Development
//

package org.me.gcu.healing_nicholas_s2533080;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.location.Location;
import android.widget.ProgressBar;

import java.util.ArrayList;

import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.Fragment;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Base64;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;


public class MainActivity extends AppCompatActivity
        implements OnClickListener, OnMapReadyCallback {
    // To display whether data is loading and how many situations were found
    private TextView statusText;
    // Display the situations in a scrollable list
    private ListView trafficListView;
    // Searching for situation widgets
    private EditText roadSearchInput;
    // Shows a spinning wheel while traffic information is loading
    private ProgressBar loadingSpinner;


    // Buttons
    private Button startButton;
    private Button roadworksButton;
    private Button searchButton;
    private Button clearSearchButton;
    // Button to open our map
    private Button mapButton;

    // Each new download gets a number so that we can identify the latest request
    // Older downloads can still finish, but their results will not replace newer requests
    private int latestDownloadId = 0;

    // Container used to show the selected situation's details
    private FrameLayout detailsHolder;


    //Storing the complete parsed objects in an ArrayList for later selection/details screens
    private ArrayList<Situation> displayedSituations = new ArrayList<>();
    // Holds all situations that were downloaded
    private ArrayList<Situation> allSituations = new ArrayList<>();


    // ViewModel to store traffic data so it can be recovered after screen rotation
    private TrafficViewModel viewModel;

    // Widgets used for searching for situations by distance
    private EditText distanceInput;
    private Button nearbyButton;

    // Fixed coordinates for Glasgow city centre
    private final double glasgowLatitude = 55.861;
    private final double glasgowLongitude = -4.252;

    //DOWNLOADING FROM TRAFFIC SCOTLAND
    final String clientId = BuildConfig.TRAFFIC_CLIENT_ID;
    final String clientKey = BuildConfig.TRAFFIC_CLIENT_KEY;

    // URL for the Traffic Scotland Unplanned Events XML feed
    final String unplannedEventsURL = "https://datex2.trafficscotland.org/rest/2.3/publications/UnplannedEvents/Content.xml";
    // URL for the Traffic Scotland Current Roadworks XML feed
    final String currentRoadworksURL = "https://datex2.trafficscotland.org/rest/2.3/publications/CurrentRoadworks/Content.xml";


    // Handler used to schedule automatic parsing of traffic data
    private Handler refreshHandler = new Handler(Looper.getMainLooper());

    // Refresh every hour
    private final long REFRESH_INTERVAL = 60 * 60 * 1000L;

    // Task which downloads the selected feed at regular intervals
    private Runnable refreshTask = new Runnable() {
        @Override
        public void run() {

            // Getting the URL for the curreently seleccted feed
            String url = viewModel.getSelectedFeedUrl();

            // If the user hasn't chosen a feed then use Unplanned Events
            if (url == null) {
                url = unplannedEventsURL;
            }

            // Logging the refresh
            Log.d("AutoRefresh", "Refreshing selected Traffic Scotland feed");


            // Update the download number
            latestDownloadId++;

            // Show the spinner while the automatic update is running
            loadingSpinner.setVisibility(View.VISIBLE);

            // Download the selected feed while preserving the search
            new Thread(new Task(url, true, latestDownloadId)).start();

            // Schedule this Runnable to run itself again later
            refreshHandler.postDelayed(this, REFRESH_INTERVAL);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Connecting the Java variables to the new XML widgets
        statusText = findViewById(R.id.statusText);
        // Connect the loading spinner to the XML layout
        loadingSpinner = findViewById(R.id.loadingSpinner);
        trafficListView = findViewById(R.id.trafficListView);

        startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(this);
        // Connecting to Current Roadworks button in xml file
        roadworksButton = findViewById(R.id.roadworksButton);
        roadworksButton.setOnClickListener(this);

        // Connect the details container to its XML View
        detailsHolder = (FrameLayout) findViewById(R.id.detailsHolder);

        // Connect the search widgets to the Activity
        roadSearchInput = (EditText) findViewById(R.id.roadSearchInput);
        searchButton = (Button) findViewById(R.id.searchButton);
        clearSearchButton = (Button) findViewById(R.id.clearSearchButton);

        // Connecting the distance search widgets to their XML Views
        distanceInput = (EditText) findViewById(R.id.distanceInput);
        nearbyButton = (Button) findViewById(R.id.nearbyButton);

        // Connecting the View Map button to the Activity
        mapButton = (Button) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(this);

        // Get the ViewModel used to store our traffic data
        // ViewModelProvider over creating a new TrafficViewModel as the provider will retain the lifecycle of the TrafficViewModel so we dont lose our data on screen rotation
        ViewModelProvider provider = new ViewModelProvider(this);

        viewModel = provider.get(TrafficViewModel.class);


// Search the downloaded situations when the Search button is clicked
        searchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                searchByRoad();
            }
        });

// Return to the complete list when Clear is clicked
        clearSearchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Remove any text entered by the user
                roadSearchInput.setText("");

                // Clear/remove the distance searched
                distanceInput.setText("");

                // Display all downloaded situations again
                showSituations(allSituations);
            }
        });


        // Listening for the user selecting one of the situations from the traffic list
        trafficListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Checking that the selected position exists inside my ArrayList
                if (position < 0 || position >= displayedSituations.size()) {
                    return;
                }

                // Retrieving the Situation for the row that was selected
                Situation selectedSituation = displayedSituations.get(position);

                // Creating a Fragment which contains the selectedSituations details
                SituationDetailsFragment detailsFragment =
                        SituationDetailsFragment.newInstance((selectedSituation));

                // Getting the FragmentManager and starting a transaction
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();

                // Putting the details Fragment into the FrameLayout
                transaction.replace(R.id.detailsHolder, detailsFragment);

                // Making sure the change is remembered so that we can return back to the list
                transaction.addToBackStack(null);

                // Applying the changes to the Fragment
                transaction.commit();
            }
        });

        // Search for nearby situations when the button is clicked
        nearbyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                searchByDistance();
            }
        });

        // Listens for changes to the Fragment back stack
        FragmentManager manager = getSupportFragmentManager();

        manager.addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {

                        // Updating which part of the activity is visible
                        updateDetailsVisibilty();
                    }
                }
        );

        // Setting the correct visibility when the Activity first opens
        updateDetailsVisibilty();
    }

    // Called when Activity becomes visible and restores traffic data from the ViewModel after rotations
    // or downloads Unplanned Events if no data has been loaded plus schedules the next automatic update
    @Override
    protected void onStart() {
        super.onStart();

        // Getting the previously downloaded situations from the ViewModel
        allSituations = viewModel.getAllSituations();

        // Getting the situations that were being displayed
        displayedSituations = viewModel.getDisplayedSituations();

        // Restoring the ListView if traffic information has already been loaded

        // Restore previously downloaded data after screen rotation
        if (!allSituations.isEmpty()) {

            showSituations(displayedSituations);

        } else {

            // No traffic data loaded yet, so download it automatically
            statusText.setText("Loading Unplanned Events...");

            Log.d("AutoRefresh", "Loading Unplanned Events at startup");

            startProgress(unplannedEventsURL);
        }


        // Making sure on rotation the map and its markers are still loaded into the framelayout
        // Checking which Fragment Android has restored after rotation - asking which fragment is currently in the framelayout
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.detailsHolder);

        // If the restored Fragment is Google Maps, reconnect the callback
        if (currentFragment instanceof SupportMapFragment) {

            SupportMapFragment mapFragment =
                    (SupportMapFragment) currentFragment;

            // Call onMapReady again when the restored map is ready
            mapFragment.getMapAsync(this);
        }


        // Removing any scheduled callbacks so as not to accidentally create multiple timers
        refreshHandler.removeCallbacks(refreshTask);
        // Scheduling one refresh after the chosen interval
        refreshHandler.postDelayed(refreshTask, REFRESH_INTERVAL);


    }

    public void onClick(View aview) {
        // Checking if the Unplanned Events button was clicked
        if (aview.getId() == R.id.startButton) {

            // Letting the user know which feed is loading
            statusText.setText("Loading Unplanned Events...");

            // Passing the Unplanned Events startProgress() to be parsed
            startProgress(unplannedEventsURL);
        }

        // Checking if Current Roadworks button was clicked
        if (aview.getId() == R.id.roadworksButton) {

            // Letting the user know that roadworks are loading
            statusText.setText("Loading Current Roadworks...");

            // Passing Current Roadworks to startProgress() to be parsed
            startProgress(currentRoadworksURL);
        }

        // Checking if the View Map button was clicked
        if (aview.getId() == R.id.mapButton) {

            // Creating a Google Maps Fragment
            SupportMapFragment mapFragment = SupportMapFragment.newInstance();

            // Displaying the map in the same container we use for situation details
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detailsHolder, mapFragment)
                    .addToBackStack(null)
                    .commit();

            // Calling onMapReady when Google Maps has loaded
            mapFragment.getMapAsync(this);
        }

    }

    // Starts download of selected Traffic feed and remember the chosen URL + runes Task on background thread
    public void startProgress(String url) {

        // Remember which traffic feed the user selected
        viewModel.setSelectedFeedUrl(url);

        // Assign a new number to this download
        latestDownloadId++;

        // Log the number so we can check each button press
        Log.d("AutoRefresh", "Starting download: " + latestDownloadId);

        // Show the spinner while the selected feed is downloading
        loadingSpinner.setVisibility(View.VISIBLE);

        // Start the download on the background thread
        new Thread(new Task(url, false, latestDownloadId)).start();
    }

    // Creates a Task for the selected URL. keepSearch decides whether an existing search should be preserved
    // id identifies the download so older results can be ignored
    private class Task implements Runnable {
        private String result = "";
        private String myurl;
        private boolean preserveSearch;
        BufferedReader in = null;
        String inputLine = "";

        // Number assigned when this download started
        private int downloadId;

        // Constructor for building Task, I added a boolean to preserve the users search when parsed data is downloaded
        // such as false - the user manually selected to download traffic events so proivde the full list or true - it was an automatic refresh so maintain the current list that was already loaded
        public Task(String aurl, boolean keepSearch, int id) {

            myurl = aurl;
            preserveSearch = keepSearch;
            downloadId = id;
        }

        // Runs on the worker thread. Downloads XML or reads saved XML from internal file if download fails
        // Parses the data into the Situation objects and saves successful downloads
        // Uses the Handler to display the results on the UI thread
        @Override
        public void run() {
            Log.d("MyTask", "in run");
            try {
                URL url = new URL(myurl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                //ADD AUTHORISATION (string with clientIID:clientKey)
                urlConnection.setRequestProperty("Authorization", Base64.getEncoder().encodeToString((clientId + ":" + clientKey).getBytes()));
                try {
                    in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    while ((inputLine = in.readLine()) != null) {
                        result = result + inputLine;
                    }
                    in.close();
                } catch (IOException e) {
                    Log.e("urlConnection", "Could not download traffic data", e);
                    Log.e("urlConnection", "ioexception");
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                Log.e("URL", "ioexception");
            }

            // Clean up any leading garbage characters
            // Check whether XML was downloaded successfully
            boolean loadedFromFile = false;
            int i = result.indexOf("<?");

            // If the download failed, try reading our saved XML from internal storage
            if (i == -1) {

                // Each feed has its own saved file
                String fileName = "unplanned_events.xml";

                if (myurl.equals(currentRoadworksURL)) {
                    fileName = "current_roadworks.xml";
                }

                // Finding the file in the app's internal storage
                File dir = getApplication().getFilesDir();
                String path = dir.getAbsolutePath();
                File f = new File(path, fileName);

                try {

                    // Opening the saved file for reading
                    FileInputStream fin = new FileInputStream(f);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(fin));

                    // Reading the saved XML into the result String
                    result = "";
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result = result + line;
                    }

                    // Close the reader
                    reader.close();

                    // Check whether the saved file contains XML
                    i = result.indexOf("<?");

                    if (i != -1) {
                        loadedFromFile = true;

                        Log.d("OfflineCache", "Loaded saved XML from " + fileName);
                    }

                } catch (IOException e) {

                    result = "";
                    i = -1;

                    Log.e("OfflineCache", "Could not read saved XML file", e);
                }
            }


            if (i == -1) {

                // No valid XML was received
                Log.e("AutoRefresh", "No XML received - keeping existing traffic data");

                // Tell the UI thread that the download failed
                refreshHandler.post(new Runnable() {

                    @Override
                    public void run() {

                        // Ignore this error if a newer download has started
                        if (downloadId != latestDownloadId) {
                            return;
                        }

                        // Ignore failures from a previously selected feed
                        if (!myurl.equals(viewModel.getSelectedFeedUrl())) {
                            return;
                        }

                        // Stop the spinner because no traffic information could be loaded
                        loadingSpinner.setVisibility(View.GONE);

                        // Check whether previously downloaded data exists
                        if (allSituations.isEmpty()) {
                            statusText.setText("Unable to load traffic data. Check your connection.");

                        } else {

                            // Keep the old results and show an error message
                            statusText.setText("Refresh failed - showing " + displayedSituations.size()
                                    + " previously loaded situations");
                        }
                    }
                });

                return;
            }

            result = result.substring(i);


            // Now that you have the xml data into result, you can parse it
            // Creating a PullParser so we can extract useful information from it
            try {

                // Creating the PullParser
                XmlPullParserFactory factory =
                        XmlPullParserFactory.newInstance();


                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                // Give the downloaded XML String to the parser
                xpp.setInput(new StringReader(result));

                // ArrayList to contain all of the Situation objects found in the XML
                ArrayList<Situation> situations = new ArrayList<>();

                // Temporarily stores the Situation we are currently reading from the XML
                Situation currentSituation = null;

                // Tracking whether the parser is currently inside a generalPublicComment
                boolean insideGeneralPublicComment = false;

                // True while PullParser is inside the <impact> section
                boolean insideImpact = false;

                // True while we are reading the location section
                boolean insideGroupOfLocations = false;

                // Tracking whether we are in From or To coordinate section
                boolean insideFrom = false;
                boolean insideTo = false;

                // True while reading a <name> or <ilc> element inside a location
                boolean insideRoadName = false;
                // Temporarily holds a possible road name until we know its descriptor type
                String tempRoadName = null;

                // Find out what event the parser is currently on
                int eventType = xpp.getEventType();

                // Keep moving through the XML until the whole document is finished
                while (eventType != XmlPullParser.END_DOCUMENT) {

                    // Check if the parser has reached a START_TAG such as <situationRecord>
                    if (eventType == XmlPullParser.START_TAG) {

                        // Check whether this is the start of a new situation record
                        if (xpp.getName().equalsIgnoreCase("situationRecord")) {

                            Log.d("Parsing", "New situation record found");

                            // We have reached a new situation, so create a new Situation object to store its data
                            currentSituation = new Situation();

                            // Attribute 0 contains the type of situation
                            String type = xpp.getAttributeValue(0);

                            // Store that type inside the current Situation object
                            currentSituation.setType(type);

                            // Get the unique ID from the situationRecord opening tag
                            String id = xpp.getAttributeValue(null, "id");

                            // Store the ID in our current Situation object
                            currentSituation.setId(id);

                            Log.d("Parsing", "Situation type: " + type);
                        } else if (xpp.getName().equalsIgnoreCase("generalPublicComment")) {

                            // Entered into generalPublicComment section
                            insideGeneralPublicComment = true;
                        } else if (xpp.getName().equalsIgnoreCase("value")
                                && insideGeneralPublicComment
                                && currentSituation != null) {

                            // Read the text inside the value tags
                            String comment = xpp.nextText();

                            // Storing the comment in the current Situation object
                            currentSituation.setGeneralPublicComment(comment);

                            Log.d("Parsing", "Comment: " + comment);
                        } else if (xpp.getName().equalsIgnoreCase("overallStartTime")
                                && currentSituation != null) {

                            // Reading the text inside the <overAllStartTime> tags
                            String startTime = xpp.nextText();

                            // Saving the extracted start time in the Situation object
                            currentSituation.setOverallStartTime(startTime);

                            // Display the result in Logcat
                            Log.d("Parsing", "Start time: " + startTime);
                        } else if (xpp.getName().equalsIgnoreCase("overallEndTime")
                                && currentSituation != null) {

                            //Reading the text inside overEndTime XML tags
                            String endTime = xpp.nextText();

                            // Saving the extracted end time in the Situation object
                            currentSituation.setOverallEndTime(endTime);

                            // Checking the value that was extracted with LogCat
                            Log.d("Parsing", "End time: " + endTime);
                        } else if (xpp.getName().equalsIgnoreCase("impact")
                                && currentSituation != null) {

                            // The parser enters the impact section
                            insideImpact = true;
                        } else if (xpp.getName().equalsIgnoreCase("numberOfLanesRestricted")
                                && insideImpact
                                && currentSituation != null) {

                            // Reading the number of restricted lanes
                            String restrictedLanes = xpp.nextText();

                            // Storing it in the current Situation
                            currentSituation.setNumberOfLanesRestricted(restrictedLanes);

                            Log.d("Parsing", "Restricted lanes: " + restrictedLanes);
                        } else if (xpp.getName().equalsIgnoreCase("numberOfOperationalLanes")
                                && insideImpact
                                && currentSituation != null) {

                            // Reading the number of operational lanes
                            String operationLanes = xpp.nextText();

                            // Storing it in the current Situation
                            currentSituation.setNumberOfOperationalLanes(operationLanes);

                            Log.d("Parsing", "Operational lanes: " + operationLanes);
                        } else if (xpp.getName().equalsIgnoreCase("delaysType")
                                && insideImpact
                                && currentSituation != null) {

                            // Reading and storing delay category from the XML
                            String delayType = xpp.nextText();

                            // Storing the delay category in the current Situation
                            currentSituation.setDelaysType(delayType);

                            // Displaying extracted delay type with LogCat
                            Log.d("Parsing", "Delay type: " + delayType);
                        } else if (xpp.getName().equalsIgnoreCase("groupOfLocations")
                                && currentSituation != null) {

                            // Entered the location section of this Situation
                            insideGroupOfLocations = true;

                            // Creating a TrafficLocation object to hold its location details
                            currentSituation.setLocation(new TrafficLocation());
                        } else if (xpp.getName().equalsIgnoreCase("tpegDirection")
                                && currentSituation != null
                                && currentSituation.getLocation() != null) {

                            // Read the direction from the text inside <tpegDirection> tags
                            String direction = xpp.nextText();

                            // Save the direction in the TrafficLocation object
                            currentSituation.getLocation().setDirection(direction);

                            Log.d("Parsing", "Direction: " + direction);
                        } else if (xpp.getName().equalsIgnoreCase("from")
                                && insideGroupOfLocations) {

                            // Now entered into the location starting point
                            insideFrom = true;
                        } else if (xpp.getName().equalsIgnoreCase("to")
                                && insideGroupOfLocations) {

                            // Now entered into the location end point section
                            insideTo = true;
                        } else if (xpp.getName().equalsIgnoreCase("latitude")
                                && insideGroupOfLocations
                                && (insideFrom || insideTo)
                                && currentSituation != null
                                && currentSituation.getLocation() != null) {

                            // Reading the latitude text from the XML
                            String latitudeText = xpp.nextText();

                            try {
                                // Converting the text to a number
                                double latitude = Double.parseDouble(latitudeText);

                                if (insideFrom) {

                                    // Storing the latitude for the starting point
                                    currentSituation.getLocation().setFromLatitude(latitude);

                                    Log.d("Parsing", "From latitude: " + latitude);
                                } else if (insideTo) {

                                    // Storing the latitude for the end point
                                    currentSituation.getLocation().setToLatitude(latitude);

                                    Log.d("Parsing", "To latitude: " + latitude);
                                }
                            } catch (NumberFormatException e) {

                                // If the XML contains an invalid number, don't crash the app
                                Log.w("Parsing", "Invalid latitude: " + latitudeText);
                            }
                        } else if (xpp.getName().equalsIgnoreCase("longitude")
                                && insideGroupOfLocations
                                && currentSituation != null
                                && (insideFrom || insideTo)
                                && currentSituation.getLocation() != null) {

                            // Reading the longitude from the XML
                            String longitudeText = xpp.nextText();

                            try {
                                // Converting the longitude text into a double
                                double longitude = Double.parseDouble(longitudeText);

                                if (insideFrom) {

                                    // Setting the longitude starting point
                                    currentSituation.getLocation().setFromLongitude(longitude);

                                    Log.d("Parsing", "From longitude: " + longitude);
                                } else if (insideTo) {

                                    // Setting the longitude end point
                                    currentSituation.getLocation().setToLongitude(longitude);

                                    Log.d("Parsing", "To longitude: " + longitude);
                                }
                            } catch (NumberFormatException e) {

                                // Log a warning in case a value is not a number
                                Log.w("Parsing", "Invalid longitude: " + longitudeText);
                            }
                        } else if ((xpp.getName().equalsIgnoreCase("name")
                                || xpp.getName().equalsIgnoreCase("ilc"))
                                && currentSituation != null
                                && currentSituation.getLocation() != null
                                && (insideFrom || insideTo)
                                && insideGroupOfLocations) {

                            // Entered inside an section which might contain a possible road name
                            insideRoadName = true;

                            // Clearing any temp names from the previous element
                            tempRoadName = null;

                        } else if (xpp.getName().equalsIgnoreCase("value")
                                && insideRoadName
                                && currentSituation != null) {

                            // Reading the text inside the <value> tag
                            String possibleName = xpp.nextText();

                            // Holding the name until we can confirm what kind of name it is
                            tempRoadName = possibleName;
                        } else if ((xpp.getName().equalsIgnoreCase("tpegIlcPointDescriptorType")
                                || xpp.getName().equalsIgnoreCase("tpegOtherPointDescriptorType"))
                                && currentSituation != null
                                && insideRoadName
                                && currentSituation.getLocation() != null) {

                            // Checking what kind of road name we read earlier
                            String descriptorType = xpp.nextText();

                            // Only storing names that are identified as primary road or link names
                            if (descriptorType.equalsIgnoreCase("tpegIlcName1")
                                    || descriptorType.equalsIgnoreCase("linkName")) {

                                // Checking that a name was found and isn't empty
                                if (tempRoadName != null
                                        && !tempRoadName.trim().isEmpty()) {

                                    // Only setting the name if we haven't set one already
                                    if (currentSituation.getLocation().getRoadName() == null) {

                                        currentSituation.getLocation().setRoadName(tempRoadName);

                                        Log.d("Parsing", "Road name: " + tempRoadName);
                                    }
                                }
                            }
                        }
                    }

                    // END_TAG meaning the parser has reach an end tag like </situationRecord>
                    else if (eventType == XmlPullParser.END_TAG) {

                        // We've left the generalPublicComment section
                        if (xpp.getName().equalsIgnoreCase("generalPublicComment")) {

                            insideGeneralPublicComment = false;
                        }

                        // We have finished reading this entire situation record
                        else if (xpp.getName().equalsIgnoreCase("situationRecord")
                                && currentSituation != null) {

                            // Add the completed situation to our collection
                            situations.add(currentSituation);

                            // Logcat to show its stored information
                            Log.d("Parsing", currentSituation.toString());

                            // Ready for the next Situation
                            currentSituation = null;
                        } else if (xpp.getName().equalsIgnoreCase("impact")) {

                            // Finished reading the impact section
                            insideImpact = false;
                        } else if (xpp.getName().equalsIgnoreCase("from")) {

                            // Setting from boolean to false as we've finished reading the starting point
                            insideFrom = false;
                        } else if (xpp.getName().equalsIgnoreCase("to")) {

                            // Setting the to boolean to false as we've finished reading the end point
                            insideTo = false;
                        } else if (xpp.getName().equalsIgnoreCase("groupOfLocations")) {

                            // Finished reading the location information
                            insideGroupOfLocations = false;

                            // Setting from and to also to false incase locations end tag before we
                            // get a chance to turn them off
                            insideFrom = false;
                            insideTo = false;
                        } else if (xpp.getName().equalsIgnoreCase("name")
                                || xpp.getName().equalsIgnoreCase("ilc")) {

                            // Finished reading the possible road name section
                            insideRoadName = false;

                            // Clear its temporary text
                            tempRoadName = null;
                        }
                    }

                    // Move the PullParser forward to the next XML event
                    eventType = xpp.next();
                }

                // After parsing has finished, show how many situationRecords were found
                Log.d("Parsing", "Total situations parsed: " + situations.size());


                // This allows the application to use the data without internet later
                // Save only new live downloads, not XML read from storage
                if (downloadId == latestDownloadId && !loadedFromFile) {

                    // Each traffic feed has a separate saved file
                    String fileName = "unplanned_events.xml";

                    if (myurl.equals(currentRoadworksURL)) {
                        fileName = "current_roadworks.xml";
                    }

                    // Get the application's private internal storage folder
                    File dir = getApplication().getFilesDir();
                    String path = dir.getAbsolutePath();

                    // Create a File object representing the XML file
                    File f = new File(path, fileName);

                    try {

                        // Open the file for writing
                        FileOutputStream fos = new FileOutputStream(f);

                        // Writing the downloaded XML String into the file
                        fos.write(result.getBytes());

                        // Close the file after writing
                        fos.close();

                        Log.d("OfflineCache", "Saved " + situations.size() + " situations to " + fileName);

                    } catch (IOException e) {

                        // Logging the error but still display the downloaded data
                        Log.e("OfflineCache", "Error saving XML file", e);
                    }
                }

                // To remember whether the displayed information came from internal storage
                final boolean showingSavedData = loadedFromFile;

                // Update the ListView after the downloaded XML has been parsed
                // Posts from worker to the main UI making it safe to update widgets here
                refreshHandler.post(new Runnable() {

                    @Override
                    public void run() {

                        // Only allow the newest download to update the screen
                        if (downloadId != latestDownloadId) {

                            Log.d("AutoRefresh", "Ignoring older download: " + downloadId);

                            return;
                        }

                        // Ignoring a download if the user has switched feeds
                        if (!myurl.equals(viewModel.getSelectedFeedUrl())) {
                            Log.d("AutoRefresh", "Ignoring result from previously selected feed");
                            return;
                        }

                        // Hide the spinner now that the information has loaded
                        loadingSpinner.setVisibility(View.GONE);

                        // Storing the newly downloaded situations
                        allSituations = new ArrayList<>(situations);
                        viewModel.setAllSituations(allSituations);

                        // Remember whether this traffic data came from saved XML
                        viewModel.setShowingSavedData(showingSavedData);

                        if (preserveSearch) {

                            // Reapply the road search if one is entered
                            String road = roadSearchInput.getText().toString().trim();
                            String distance = distanceInput.getText().toString().trim();

                            // Reapply an existing road search
                            if (!road.isEmpty()) {

                                searchByRoad();

                            } else if (!distance.isEmpty()) {

                                // Reapply the distance search
                                searchByDistance();

                            } else {

                                // User manually selected a different feed
                                showSituations(allSituations);
                            }

                        } else {

                            // A new feed was manually selected
                            roadSearchInput.setText("");
                            distanceInput.setText("");

                            showSituations(allSituations);
                        }


                        Log.d("AutoRefresh", "Traffic list updated");
                    }

                });
            } catch (XmlPullParserException | IOException e) {

                // Catching either an XML parsing error or a problem reading through the XML input
                Log.e("Parsing", "EXCEPTION: " + e);
            }

        }

    }

    // Switches between the traffic list and the details/map container
    private void updateDetailsVisibilty() {

        FragmentManager manager = getSupportFragmentManager();

        // Checking whether a details transaction is in the back stack
        if (manager.getBackStackEntryCount() > 0) {

            // Hiding the traffic list and displaying the details container
            trafficListView.setVisibility(View.GONE);
            detailsHolder.setVisibility(View.VISIBLE);
        } else {

            // Show the traffic list again when details are closed
            trafficListView.setVisibility(View.VISIBLE);
            detailsHolder.setVisibility(View.GONE);
        }
    }

    // Displays situations in ListView using custom Adapter. Also saves the displayed list in the ViewModel
    private void showSituations(ArrayList<Situation> situationsToShow) {

        // Storing the situations currently displayed on the screen
        displayedSituations = new ArrayList<>(situationsToShow);

        // Remember the displayed list in case the screen rotates
        viewModel.setDisplayedSituations(displayedSituations);

        // Creating the Adapter for the situations I want to show
        SituationAdapter adapter =
                new SituationAdapter(this, displayedSituations);

        // Updating the ListView
        trafficListView.setAdapter(adapter);


        // Display the correct status depending on where the data came from
        if (viewModel.isShowingSavedData()) {

            statusText.setText("Offline - showing " + displayedSituations.size()
                    + " saved situations (may be out of date)");

        } else {

            statusText.setText("Showing " + displayedSituations.size()
                    + " situations");
        }

    }

    // Searching the traffic situations for a particular road entered by user. Displays matching situations in ListView
    private void searchByRoad() {

        // Get the road name entered by the user
        String road = roadSearchInput.getText().toString().trim();

        // Checking whether any situation information has been loaded
        if (allSituations.isEmpty()) {
            statusText.setText("Load a road situation before searching");
            return;
        }

        // Checking that the user entered a road name
        if (road.isEmpty()) {
            statusText.setText("Please enter a road name");
            return;
        }

        // Clear the previous distance search when searching by road
        distanceInput.setText("");

        // Creating a list to store matching situations
        ArrayList<Situation> matchingSituations = new ArrayList<>();

        // Looping through/checking each Situation
        for (Situation situation : allSituations) {

            // Some situations might not contain location data
            if (situation.getLocation() != null) {

                String roadName = situation.getLocation().getRoadName();

                // Making sure the road name exists before comparing it
                if (roadName != null) {

                    // Comparing the searched road name with road names in our Situation
                    if (roadName.toLowerCase().contains(road.toLowerCase())) {
                        // Adding the matching situations to our results/matchingSituations arrayList
                        matchingSituations.add(situation);
                    }
                }
            }
        }

        // Displaying only the situations matching the road name
        showSituations(matchingSituations);

        // Inform the user if no matching road was found
        if (matchingSituations.isEmpty()) {

            String message = "There were no situations matching " + road;

            if (viewModel.isShowingSavedData()) {
                message = message + " (offline - saved data may be out of date)";
            }

            statusText.setText(message);
        }

    }

    // Calculates the distance between fixed location (Glasgow City Centre) and a traffic location
    // Returns true if the location is within the distance that was searched
    private boolean isWithinDistance(double latitude, double longitude,
                                     double distanceKm) {

        // Check that the entered coordinates are valid
        if (latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180
                || (latitude == 0 && longitude == 0)) {
            return false;
        }

        // Create a Location object for Glasgow city centre
        Location glasgow = new Location("fixed");
        glasgow.setLatitude(glasgowLatitude);
        glasgow.setLongitude(glasgowLongitude);

        // Create a Location object for the traffic situation
        Location trafficLocation = new Location("traffic");
        trafficLocation.setLatitude(latitude);
        trafficLocation.setLongitude(longitude);

        // Calculating the distance between the two locations in metres using distanceTo method
        float distanceMetres = glasgow.distanceTo(trafficLocation);

        // Convert from metres in to kilometres
        double calculatedDistance = distanceMetres / 1000.0;

        // Checking whether the situation is close enough
        return calculatedDistance <= distanceKm;
    }

    // Searches for traffic situations within a distance of Glasgow. Checks from and to coordinates and displays matching results
    private void searchByDistance() {

        // Checking that the user has loaded some traffic data
        if (allSituations.isEmpty()) {
            statusText.setText("Load some traffic data before searching");
            return;
        }

        // Getting the distance entered by the user
        String distanceText = distanceInput.getText().toString().trim();

        if (distanceText.isEmpty()) {
            statusText.setText("Please enter distance");
            return;
        }

        // Converting the entered distance from text into a number
        double distanceInKm;

        try {
            distanceInKm = Double.parseDouble(distanceText);
        } catch (NumberFormatException e) {
            statusText.setText("Please enter a valid distance");
            return;
        }

        // Distance must be a positive number
        if (distanceInKm <= 0) {
            statusText.setText("Distance must be greater than zero");
            return;
        }

        // Clear the previous road search when searching by distance
        roadSearchInput.setText("");

        // Creating an ArrayList to store situations within the distance
        ArrayList<Situation> nearbySituations = new ArrayList<>();

        // Checking each situation from the loaded traffic dadta
        for (Situation situation : allSituations) {

            TrafficLocation location = situation.getLocation();

            // Some situations might not have location information
            if (location != null) {

                boolean nearby = false;

                // Checking the starting coordinates if they exists
                Double fromLat = location.getFromLatitude();
                Double fromLon = location.getFromLongitude();

                if (fromLat != null && fromLon != null) {

                    nearby = isWithinDistance(fromLat, fromLon, distanceInKm);
                }

                // Also checking the ending coordinates
                Double toLat = location.getToLatitude();
                Double toLon = location.getToLongitude();

                if (!nearby && toLat != null && toLon != null) {

                    nearby = isWithinDistance(toLat, toLon, distanceInKm);
                }

                // Adding this Situation if either location is nearby
                if (nearby) {
                    nearbySituations.add(situation);
                }
            }
        }

        // Displaying the situations that matched the distance search
        showSituations(nearbySituations);

        // Display how many situations matched the distance search
        String resultMessage = "Found " + nearbySituations.size()
                + " situations within " + distanceInKm + " km of Glasgow City Centre";

        // Inform the user if the search used saved information
        if (viewModel.isShowingSavedData()) {
            resultMessage = resultMessage + " (offline - saved data may be out of date)";
        }

        statusText.setText(resultMessage);

    }


    // Called when Google Maps is ready to use. Adds markers for the currently displayed situations
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Clearing any previous markers before displaying the current situations
        googleMap.clear();

        // Starting the map with an overview of Scotland
        LatLng scotland = new LatLng(56.5, -4.2);

        // Count how many traffic markers we add
        int markerCount = 0;

        // Check every Situation currently displayed in the ListView
        for (Situation situation : displayedSituations) {

            // Get the location information for this Situation
            TrafficLocation location = situation.getLocation();

            // Some situations might not have location information
            if (location == null) {
                continue;
            }

            // First try the starting coordinates
            Double latitude = location.getFromLatitude();
            Double longitude = location.getFromLongitude();

            // If the starting coordinates are missing or invalid,
            // try the ending coordinates instead
            if (latitude == null || longitude == null
                    || latitude < -90 || latitude > 90
                    || longitude < -180 || longitude > 180
                    || (latitude == 0 && longitude == 0)) {

                latitude = location.getToLatitude();
                longitude = location.getToLongitude();
            }

            // Skip situations that still have invalid coordinates
            if (latitude == null || longitude == null
                    || latitude < -90 || latitude > 90
                    || longitude < -180 || longitude > 180
                    || (latitude == 0 && longitude == 0)) {

                continue;
            }

            // Get the road name for the marker
            String roadName = location.getRoadName();

            if (roadName == null || roadName.isEmpty()) {
                roadName = "Unknown road";
            }

            // Get the type of incident or roadwork
            String type = situation.getType();

            if (type == null || type.isEmpty()) {
                type = "Traffic situation";
            }

            // Choosing an icon based on the situation type and delay information
            // Default icon for an unknown traffic situation
            int markerIcon = R.drawable.map_obstruction;

            // Text describing the situation when a map marker is selected to make it more readable for the user
            String markerDescription = type;


            if (type.equalsIgnoreCase("RoadOrCarriagewayOrLaneManagement")) {

                markerIcon = R.drawable.map_lane;
                markerDescription = "Road or lane management";

            } else if (type.equalsIgnoreCase("GeneralObstruction")) {

                markerIcon = R.drawable.map_obstruction;
                markerDescription = "Road obstruction";

            } else if (type.equalsIgnoreCase("AbnormalTraffic")) {

                markerIcon = R.drawable.map_traffic;
                markerDescription = "Abnormal traffic";

            } else if (type.equalsIgnoreCase("MaintenanceWorks")) {

                String delaysType = situation.getDelaysType();

                if ("veryLongDelays".equalsIgnoreCase(delaysType)) {

                    markerIcon = R.drawable.map_very_long_delays;
                    markerDescription = "Roadworks - Very long delays";

                } else if ("longDelays".equalsIgnoreCase(delaysType)) {

                    markerIcon = R.drawable.map_long_delays;
                    markerDescription = "Roadworks - Long delays";

                } else if ("delays".equalsIgnoreCase(delaysType)) {

                    markerIcon = R.drawable.map_delays;
                    markerDescription = "Roadworks - Delays";

                } else {

                    markerIcon = R.drawable.map_roadworks;
                    markerDescription = "Roadworks - Delays not specified";
                }
            }


            // Create a map position from the XML coordinates
            LatLng position = new LatLng(latitude, longitude);

            // Add a marker for this Situation
            // Add a marker with the colour chosen for this situation type

            googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(roadName + " - " + markerDescription)
                    .icon(BitmapDescriptorFactory.fromResource(markerIcon)));


            markerCount++;
        }

        // Centre the map on Scotland
        googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(scotland, 6f));

        // Showing how many markers were created in Logcat
        Log.d("TrafficMap", "Added " + markerCount + " markers");
    }

    // Called when Activity is no longer visible and stops the automatic data refreshes until Activity starts again
    @Override
    protected void onStop() {

        // Stop scheduling updates when the Activity is no longer visible
        refreshHandler.removeCallbacks(refreshTask);

        super.onStop();
    }


}