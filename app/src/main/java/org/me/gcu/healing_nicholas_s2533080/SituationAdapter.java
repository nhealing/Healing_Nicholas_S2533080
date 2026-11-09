package org.me.gcu.healing_nicholas_s2533080;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SituationAdapter extends ArrayAdapter<Situation> {

    // Constructor receives the Activity context and list of situations
    public SituationAdapter(Context context, ArrayList<Situation> situations) {
        super(context, 0, situations);
    }

    // Displaying a row in the ListView
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // To let us reuse an existing row if one is available
        View v = convertView;

        // Checking if a previous row is not null so we can reuse it display information as we scroll through the ListView
        // If it is null we need to create a new row
        if (v == null) {

            // Create the row using our traffic_list_item.xml layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            v = inflater.inflate(R.layout.traffic_list_item, parent, false);
        }

        // Get the Situation object belonging to this list position
        Situation situation = getItem(position);

        // Find the TextViews inside our row layout
        TextView rowTitle = (TextView) v.findViewById(R.id.rowTitle);
        TextView rowComment = (TextView) v.findViewById(R.id.rowComment);
        TextView rowDelay = (TextView) v.findViewById(R.id.rowDelay);

        if (situation != null) {

            // Getting the readable situation type for displaying the list
            String type = situation.getDisplayType();

            if (type == null || type.isEmpty()) {
                type = "Unknown situation";
            }

            // Get the road name if it exists
            String roadName = "Unknown road";

            if (situation.getLocation() != null) {

                String road = situation.getLocation().getRoadName();

                if (road != null && !road.isEmpty()) {
                    roadName = road;
                }
            }

            // Display the road name and situation type
            rowTitle.setText(roadName + " - " + type);

            // Get the public comment
            String comment = situation.getGeneralPublicComment();

            if (comment == null || comment.isEmpty()) {
                comment = "No public comment available";
            }

            rowComment.setText(comment);

            // Get the delay category extracted from the XML
            String delay = situation.getDelaysType();

            if (delay != null && !delay.isEmpty()) {

                // Show the delay label
                rowDelay.setVisibility(View.VISIBLE);

                // Set the label colour depending on the delay type
                if (delay.equalsIgnoreCase("longDelays")
                        || delay.equalsIgnoreCase("veryLongDelays")) {

                    rowDelay.setText("Long delays");
                    rowDelay.setBackgroundColor(Color.RED);
                    rowDelay.setTextColor(Color.WHITE);

                } else if (delay.equalsIgnoreCase("delays")) {

                    // Delays are reported but the duration is not specified
                    rowDelay.setText("Delays reported");
                    rowDelay.setBackgroundColor(Color.YELLOW);
                    rowDelay.setTextColor(Color.BLACK);
                } else {

                    // Display any other category without guessing its meaning
                    rowDelay.setText(delay);
                    rowDelay.setBackgroundColor(Color.LTGRAY);
                    rowDelay.setTextColor(Color.BLACK);
                }

            } else {

                // Hide the label when there is no delay information
                rowDelay.setVisibility(View.GONE);
            }
        }

        // Return the completed row to the ListView
        return v;
    }
}
