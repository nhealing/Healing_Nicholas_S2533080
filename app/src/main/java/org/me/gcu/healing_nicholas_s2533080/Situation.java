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
                '}';
    }
}