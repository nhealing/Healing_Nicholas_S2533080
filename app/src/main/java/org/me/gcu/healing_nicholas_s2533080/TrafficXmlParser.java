package org.me.gcu.healing_nicholas_s2533080;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class TrafficXmlParser {

    // Takes the downloaded XML and returns the situations found
    public static ArrayList<Situation> parse(String xml)
            throws XmlPullParserException, IOException {

        // Creating the PullParser
        XmlPullParserFactory factory =
                XmlPullParserFactory.newInstance();


        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        // Give the downloaded XML String to the parser
        xpp.setInput(new StringReader(xml));

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

        return situations;
    }
}
