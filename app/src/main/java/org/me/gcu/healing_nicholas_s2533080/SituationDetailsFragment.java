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

        args.putString("type", situation.getType());
        args.putString("comment", situation.getGeneralPublicComment());
        args.putString("startTime", situation.getOverallStartTime());
        args.putString("endTime", situation.getOverallEndTime());
        args.putString("delays", situation.getDelaysType());
        args.putString("restrictedLanes", situation.getNumberOfLanesRestricted());
        args.putString("operationalLanes", situation.getNumberOfOperationalLanes());

        // Check in case Situation does not contain location information
        if (situation.getLocation() != null) {
            args.putString("roadName", situation.getLocation().getRoadName());
            args.putString("direction", situation.getLocation().getDirection());
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

        details = details + "Type: " + readable(args.getString("type")) + "\n";
        details = details + "Road: " + readable(args.getString("roadName")) + "\n";
        details = details + "Direction: " + readable(args.getString("direction")) + "\n";
        details = details + "Comment: " + readable(args.getString("comment")) + "\n";
        details = details + "Start time: " + readable(args.getString("startTime")) + "\n";
        details = details + "End time: " + readable(args.getString("endTime")) + "\n";
        details = details + "Delay type: " + readable(args.getString("delays")) + "\n";
        details = details + "Restricted lanes: " + readable(args.getString("restrictedLanes")) + "\n";
        details = details + "Operational lanes: " + readable(args.getString("operationalLanes"));

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
