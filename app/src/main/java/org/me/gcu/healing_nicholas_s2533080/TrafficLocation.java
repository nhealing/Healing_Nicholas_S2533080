package org.me.gcu.healing_nicholas_s2533080;

public class TrafficLocation {

    // Name of the road where the situation occurs
    private String roadName;

    // Direction of travel, such as northBound or southBound
    private String direction;

    // Coordinates for the starting point
    private Double fromLatitude;
    private Double fromLongitude;

    // Coordinates for the ending point
    private Double toLatitude;
    private Double toLongitude;

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public String getDirection() {
        return direction;
    }

    // Converting the XML direction into a more readable description for the user
    public String getDisplayDirection() {

        if (direction == null || direction.isEmpty()) {
            return "Not provided";
        }

        if (direction.equalsIgnoreCase("eastBound")) {
            return "Eastbound";
        } else if (direction.equalsIgnoreCase("westBound")) {
            return "Westbound";
        } else if (direction.equalsIgnoreCase("northBound")) {
            return "Northbound";
        } else if (direction.equalsIgnoreCase("southBound")) {
            return "Southbound";
        } else if (direction.equalsIgnoreCase("bothDirections")) {
            return "Both directions";
        }

        // Return the original direction if it is not one we recognise
        return direction;
    }


    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Double getFromLatitude() {
        return fromLatitude;
    }

    public void setFromLatitude(Double fromLatitude) {
        this.fromLatitude = fromLatitude;
    }

    public Double getFromLongitude() {
        return fromLongitude;
    }

    public void setFromLongitude(Double fromLongitude) {
        this.fromLongitude = fromLongitude;
    }

    public Double getToLatitude() {
        return toLatitude;
    }

    public void setToLatitude(Double toLatitude) {
        this.toLatitude = toLatitude;
    }

    public Double getToLongitude() {
        return toLongitude;
    }

    public void setToLongitude(Double toLongitude) {
        this.toLongitude = toLongitude;
    }

    @Override
    public String toString() {
        return "TrafficLocation{" +
                "roadName='" + roadName + '\'' +
                ", direction='" + direction + '\'' +
                ", fromLatitude=" + fromLatitude +
                ", fromLongitude=" + fromLongitude +
                ", toLatitude=" + toLatitude +
                ", toLongitude=" + toLongitude +
                '}';
    }
}