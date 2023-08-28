package com.trafiklab.trafiklab.classes;

import java.util.ArrayList;
import java.util.List;

public class Line implements Comparable<Line> {
    private String LineNumber;
    private String DirectionCode;

    private List<StopPoint> StopPointList;

    public Line() {   StopPointList = new ArrayList<>();
    }
    public Line(String aLineNumber, String aDirectionCode)
    {
        LineNumber = aLineNumber;
        DirectionCode = aDirectionCode;
        StopPointList = new ArrayList<>();
    }

    public int getStopPointSize()
    {
        return StopPointList.size();
    }
    public String getDirectionCode() {
        return DirectionCode;
    }

    public String getLineNumber()
    {
        return LineNumber;
    }


    public void AddStopPoint(StopPoint aStation)
    {
        StopPointList.add(aStation);
    }

    @Override
    public String toString() {
        StringBuilder allStops = new StringBuilder();

        for (var v : StopPointList)
        {
            allStops.append(v.getStopPointName()).append("\n");
        }

        return allStops.toString();
    }

    public String toStringWEB() {
        StringBuilder allStops = new StringBuilder();

        for (var v : StopPointList)
        {
            allStops.append(v.getStopPointName()).append("<br>");
        }

        return allStops.toString();
    }

    @Override
    public int compareTo(Line o) {
        return Integer.compare(o.getStopPointSize(),this.getStopPointSize());
    }
}
