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
import android.widget.TextView;
import android.view.View.OnClickListener;
import java.util.ArrayList;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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


public class MainActivity extends AppCompatActivity implements OnClickListener {
    private TextView rawDataDisplay;
    private Button startButton;

    //DOWNLOADING FROM TRAFFIC SCOTLAND
    final String clientId = BuildConfig.TRAFFIC_CLIENT_ID;
    final String clientKey = BuildConfig.TRAFFIC_CLIENT_KEY;
    final String unplannedEventsURL = "https://datex2.trafficscotland.org/rest/2.3/publications/UnplannedEvents/Content.xml";

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
        // Set up the raw links to the graphical components
        rawDataDisplay = (TextView)findViewById(R.id.rawDataDisplay);
        startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        // More Code goes here

    }

    public void onClick(View aview)
    {
        startProgress();
    }

    public void startProgress()
    {
        // Run network access on a separate thread;
        new Thread(new Task(unplannedEventsURL)).start();
    } //

    // Need separate thread to access the internet resource over network
    // Other neater solutions should be adopted in later iterations.
    private class Task implements Runnable
    {
        private String result="";
        private String myurl;
        BufferedReader in = null;
        String inputLine = "";
        public Task(String aurl){
            myurl = aurl;
        }
        @Override
        public void run(){
            Log.d("MyTask","in run");
            try {
                URL url = new URL(myurl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                //ADD AUTHORISATION (string with clientIID:clientKey)
                urlConnection.setRequestProperty("Authorization",Base64.getEncoder().encodeToString((clientId + ":" + clientKey).getBytes()));
                try {
                    in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    while ((inputLine = in.readLine()) != null){
                        result = result + inputLine;
                    }
                    in.close();
                } catch (IOException e) {
                    String error = urlConnection.getErrorStream().toString();
                    Log.e("urlConnection", "ioexception");
                }
                finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                Log.e("URL", "ioexception");
            }

            //Clean up any leading garbage characters
            int i = result.indexOf("<?"); //initial tag
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

                // Find out what event the parser is currently on
                int eventType = xpp.getEventType();

                // Keep moving through the XML until the whole document is finished
                while (eventType != XmlPullParser.END_DOCUMENT) {

                    // Check if the parser has reached a START_TAG such as <situationRecord>
                    if (eventType == XmlPullParser.START_TAG) {

                        // Check whether this is the start of a new situation record
                        if (xpp.getName().equalsIgnoreCase("situationRecord")) {

                            Log.d("Parsing", "New situation record found");

                            // We have reached a new situation, so create a new Java
                            // Situation object to store its data
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
                        }

                        else if (xpp.getName().equalsIgnoreCase("generalPublicComment")) {

                            // Entered into generalPublicComment section
                            insideGeneralPublicComment = true;
                        }

                        else if (xpp.getName().equalsIgnoreCase("value")
                                && insideGeneralPublicComment
                                && currentSituation != null) {

                            // Read the text inside the value tags
                            String comment = xpp.nextText();

                            // Storing the comment in the current Situation object
                            currentSituation.setGeneralPublicComment(comment);

                            Log.d("Parsing", "Comment: " + comment);
                        }

                        else if (xpp.getName().equalsIgnoreCase("overallStartTime")
                                && currentSituation != null) {

                            // Reading the text inside the <overAllStartTime> tags
                            String startTime = xpp.nextText();

                            // Saving the extracted start time in the Situation object
                            currentSituation.setOverallStartTime(startTime);

                            // Display the result in Logcat
                            Log.d("Parsing", "Start time: " + startTime);
                        }

                        else if (xpp.getName().equalsIgnoreCase("impact")
                                && currentSituation != null) {

                            // The parser enters the impact section
                            insideImpact = true;
                        }

                        else if (xpp.getName().equalsIgnoreCase("numberOfLanesRestricted")
                                && insideImpact
                                && currentSituation != null) {

                            // Reading the number of restricted lanes
                            String restrictedLanes = xpp.nextText();

                            // Storing it in the current Situation
                            currentSituation.setNumberOfLanesRestricted(restrictedLanes);

                            Log.d("Parsing", "Restricted lanes: " + restrictedLanes);
                        }

                        else if (xpp.getName().equalsIgnoreCase("numberOfOperationalLanes")
                                && insideImpact
                                && currentSituation != null) {

                            // Reading the number of operational lanes
                            String operationLanes = xpp.nextText();

                            // Storing it in the current Situation
                            currentSituation.setNumberOfOperationalLanes(operationLanes);

                            Log.d("Parsing", "Operational lanes: " + operationLanes);
                        }

                        else if (xpp.getName().equalsIgnoreCase("groupOfLocations")
                                && currentSituation != null) {

                            // Entered the location section of this Situation
                            // Creating a TrafficLocation object to hold its location details
                            currentSituation.setLocation(new TrafficLocation());
                        }

                        else if (xpp.getName().equalsIgnoreCase("tpegDirection")
                                && currentSituation != null
                                && currentSituation.getLocation() != null) {

                            // Read the direction from the text inside <tpegDirection> tags
                            String direction = xpp.nextText();

                            // Save the direction in the TrafficLocation object
                            currentSituation.getLocation().setDirection(direction);

                            Log.d("Parsing", "Direction: " + direction);
                        }

                        // END_TAG means the parser has reach an end tag like </situationRecord>
                    } else if (eventType == XmlPullParser.END_TAG) {

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
                        }

                        else if (xpp.getName().equalsIgnoreCase("impact")) {

                            // Finished reading the impact section
                            insideImpact = false;
                        }
                    }

                    // Move the PullParser forward to the next XML event
                    eventType = xpp.next();
                }

                // After parsing has finished, show how many situationRecords were found
                Log.d("Parsing", "Total situations parsed: " + situations.size());
            }
            catch (XmlPullParserException | IOException e) {

                // Catching either an XML parsing error or a problem reading through the XML input
                Log.e("Parsing", "EXCEPTION: " + e);
            }

            // Now update the TextView to display raw XML data
            // Probably not the best way to update TextView
            // but we are just getting started !

            MainActivity.this.runOnUiThread(new Runnable()
            {
                public void run() {
                    Log.d("UI thread", "I am the UI thread");
                    rawDataDisplay.setText(result);
                }
            });
        }

    }

}