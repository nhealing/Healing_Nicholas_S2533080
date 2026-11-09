package org.me.gcu.healing_nicholas_s2533080;

public class Situation {

    private String id;
    private String type;
    private String generalPublicComment;

    // Start time of the traffic situation, taken from the XML
    private String overallStartTime;

    // Number of lanes affected by traffic situations
    private String numberOfLanesRestricted;
    private String numberOfOperationalLanes;

    // Location and information belonging to this traffic situation
    private TrafficLocation location;

    // Storing the time the situation is expected to finish
    private String overallEndTime;
    // Storing the expected delay category for a situation
    private String delaysType;

    public String getOverallEndTime() {
        return overallEndTime;
    }

    public void setOverallEndTime(String overallEndTime) {
        this.overallEndTime = overallEndTime;
    }

    public String getDelaysType() {
        return delaysType;
    }

    public void setDelaysType(String delaysType) {
        this.delaysType = delaysType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGeneralPublicComment() {
        return generalPublicComment;
    }

    public void setGeneralPublicComment(String generalPublicComment) {
        this.generalPublicComment = generalPublicComment;
    }

    public String getOverallStartTime() {
        return overallStartTime;
    }

    public void setOverallStartTime(String overallStartTime) {
        this.overallStartTime = overallStartTime;
    }

    public String getNumberOfLanesRestricted() {
        return numberOfLanesRestricted;
    }

    public void setNumberOfLanesRestricted(String numberOfLanesRestricted) {
        this.numberOfLanesRestricted = numberOfLanesRestricted;
    }

    public String getNumberOfOperationalLanes() {
        return numberOfOperationalLanes;
    }

    public void setNumberOfOperationalLanes(String numberOfOperationalLanes) {
        this.numberOfOperationalLanes = numberOfOperationalLanes;
    }

    public TrafficLocation getLocation() {
        return location;
    }

    public void setLocation(TrafficLocation location) {
        this.location = location;
    }


    // Converting the XML situation type into a more readable description for the user
    public String getDisplayType() {

        if (type == null || type.isEmpty()) {
            return "Traffic situation";
        }

        if (type.equalsIgnoreCase("RoadOrCarriagewayOrLaneManagement")) {
            return "Road or lane management";
        } else if (type.equalsIgnoreCase("GeneralObstruction")) {
            return "Road obstruction";
        } else if (type.equalsIgnoreCase("AbnormalTraffic")) {
            return "Abnormal traffic";
        } else if (type.equalsIgnoreCase("MaintenanceWorks")) {
            return "Roadworks";
        }

        // Return the original type if it is not one we recognise
        return type;
    }


    //Including all attributes in the toString() so i can check they're being extracted properly with LogCat
    @Override
    public String toString() {
        return "Situation{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", generalPublicComment='" + generalPublicComment + '\'' +
                ", overallStartTime='" + overallStartTime + '\'' +
                ", numberOfLanesRestricted='" + numberOfLanesRestricted + '\'' +
                ", numberOfOperationalLanes='" + numberOfOperationalLanes + '\'' +
                ", location=" + location +
                ", overallEndTime='" + overallEndTime + '\'' +
                ", delaysType='" + delaysType + '\'' +
                '}';
    }
}