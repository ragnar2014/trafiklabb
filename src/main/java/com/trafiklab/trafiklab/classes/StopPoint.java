package com.trafiklab.trafiklab.classes;

public class StopPoint {
    private String JourneyPatternPointNumber;

    private String StopPointName;

    public StopPoint(String aJourneyPointnumber, String stopPointName)
    {
        JourneyPatternPointNumber = aJourneyPointnumber;
        StopPointName = stopPointName;
    }

    public String getStopPointName() {
        return StopPointName;
    }

    public String getJourneyPatternPointNumber()
    {
        return JourneyPatternPointNumber;
    }
}
