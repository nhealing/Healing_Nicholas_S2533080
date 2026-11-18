package org.me.gcu.healing_nicholas_s2533080;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class SituationDetailsFragment extends Fragment {

    // Creating a Fragment containing the selected Situation's details
    public static SituationDetailsFragment newInstance(
            Situation situation) {

        SituationDetailsFragment fragment =
                new SituationDetailsFragment();

        // Packaging data so it can be used in the fragment
        Bundle args = new Bundle();

        args.putString("type", situation.getDisplayType());
        args.putString("comment", situation.getGeneralPublicComment());
        args.putString("startTime", situation.getOverallStartTime());
        args.putString("endTime", situation.getOverallEndTime());
        args.putString("delays", situation.getDelaysType());
        args.putString("restrictedLanes", situation.getNumberOfLanesRestricted());
        args.putString("operationalLanes", situation.getNumberOfOperationalLanes());

        // Check in case Situation does not contain location information
        if (situation.getLocation() != null) {
            args.putString("roadName", situation.getLocation().getRoadName());
            args.putString("direction", situation.getLocation().getDisplayDirection());
        }

        fragment.setArguments(args);

        return fragment;
    }

    // Handling missing or empty values
    private String readable(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "Not provided";
        }

        return value;
    }

    // Making the dates from the XML easier to read
    private String formatDate(String value) {

        // If no date was provided, display the usual message
        if (value == null || value.length() < 16 || value.charAt(10) != 'T') {
            return readable(value);
        }

        // Separate the day, month, year and time from the XML date
        String year = value.substring(0, 4);
        String month = value.substring(5, 7);
        String day = value.substring(8, 10);
        String time = value.substring(11, 16);

        // Keep the original time zone offset if one was provided
        String offset = "";

        if (value.endsWith("Z")) {
            offset = " (UTC+00:00)";
        } else if (value.length() >= 22) {

            String lastSix = value.substring(value.length() - 6);

            if (lastSix.startsWith("+") || lastSix.startsWith("-")) {
                offset = " (UTC" + lastSix + ")";
            }
        }

        // Return the date in a more readable format
        return day + "/" + month + "/" + year + " " + time + offset;
    }



    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_situation_details, container, false);

        TextView detailsText = (TextView) v.findViewById(R.id.detailsText);

        Button backButton = (Button) v.findViewById(R.id.backToListButton);

        // Retrieve the values passed into this Fragment
        Bundle args = requireArguments();

        String details = "";

        // Heading for the road information
        details = details + "ROAD INFORMATION\n";

        details = details + "Type: " + readable(args.getString("type")) + "\n";
        details = details + "Road: " + readable(args.getString("roadName")) + "\n";
        details = details + "Direction: " + readable(args.getString("direction")) + "\n\n";

        // Heading for the public comment
        details = details + "DESCRIPTION\n";

        details = details + "Comment: " + readable(args.getString("comment")) + "\n\n";

        // Heading for the dates and delays
        details = details + "DATES AND DELAYS\n";

        // Displaying the start and end dates in a readable format
        details = details + "Start time: " + formatDate(args.getString("startTime")) + "\n";
        details = details + "End time: " + formatDate(args.getString("endTime")) + "\n";

        // Converting the delay type from the XML into a more readable format
        String delayType = readable(args.getString("delays"));

        if (delayType.equalsIgnoreCase("veryLongDelays")) {
            delayType = "Very long delays";
        } else if (delayType.equalsIgnoreCase("longDelays")) {
            delayType = "Long delays";
        } else if (delayType.equalsIgnoreCase("delays")) {
            delayType = "Delays reported";
        }

        // Adding the readable delay type to the details
        details = details + "Delay type: " + delayType + "\n\n";

        // Heading for the lane information
        details = details + "LANE INFORMATION\n";

        details = details + "Restricted lanes: " + readable(args.getString("restrictedLanes")) + "\n";
        details = details + "Operational lanes: " + readable(args.getString("operationalLanes")) + "\n";


        // Displaying all the information
        detailsText.setText(details);

        // Return to the previous screen
        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View clickedView) {

                getParentFragmentManager().popBackStack();
            }
        });
        return v;
    }
}
