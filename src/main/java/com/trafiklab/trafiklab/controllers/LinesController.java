package com.trafiklab.trafiklab.controllers;

import com.trafiklab.trafiklab.classes.StopPoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.trafiklab.trafiklab.classes.Line;
import java.util.*;

@RestController
public class LinesController {

    //Nycklar
    final String key = "cce1c0b0c8cc495788b614af6be67184";
    final String DefaultTransMode = "&DefaultTransportModeCode=BUS";

    //Endpoints
    final String LineStopEndPoint = "https://api.sl.se/api2/LineData.json?model=JourneyPatternPointOnLine&key=" + key+ DefaultTransMode;
    final String StopPoinEndpoint = "https://api.sl.se/api2/LineData.json?model=StopPoint&StopPointName=Stadshagsplan&key="+key;

    @GetMapping("/listLines")
    public List<Line> getLines()
    {
        ArrayList<Line> LinesResponse;
        ArrayList<Line> Lines = new ArrayList<>();
        ArrayList<Map<String, String>> Stops;
        Map<String,String> StopPointsMap =  new HashMap<>();
        Object[] LineObjects;
        Map<String, Object> outerHashMap;
        Map<String, Object> responseDataMap;

        int index = 0;
        RestTemplate template = new RestTemplate();

        //Detta är ju inte snyggt men saknar kompetensen för att hantera dataanropen snyggare :(
        try {
            LineObjects = new Object[]{template.getForObject(LineStopEndPoint, Object.class)};
            outerHashMap = (Map<String, Object>) LineObjects[0];
            if(outerHashMap != null && outerHashMap.containsKey("ResponseData"))
            {
                responseDataMap = (Map<String, Object>) outerHashMap.get("ResponseData");
                LinesResponse = (ArrayList<Line>) responseDataMap.get("Result");
            }
            else
            {
                System.out.println("outerHashmap is null");
                return Lines;
            }
            Object[] StopObjects = new Object[]{template.getForObject(StopPoinEndpoint, Object.class)};
            outerHashMap = (Map<String, Object>) StopObjects[0];

            if(outerHashMap != null && outerHashMap.containsKey("ResponseData"))
            {
                responseDataMap = (Map<String, Object>) outerHashMap.get("ResponseData");
                Stops = (ArrayList<Map<String, String>>) responseDataMap.get("Result");
            }
            else
            {
                System.out.println("outerHashmap is null");
                return Lines;
            }


        }
        catch (ClassCastException e)
        {
            System.out.println("Unable to fetch data");
            return Lines;
        }

        //Lagrar alla hållplatser i Map för att lätta kunna hitta rätt
        for (var stop : Stops) {

            StopPointsMap.put(stop.get("StopPointNumber"), stop.get("StopPointName"));
        }


        Line tempLine = new Line();

        for(int i =0; i < LinesResponse.size(); i ++)
        {
            Map<String,String> tempLineMap = (Map<String, String>) LinesResponse.get(i);

            //Om vi är på första linjen
            if(tempLine.getLineNumber()== null)
            {
                tempLine = new Line(tempLineMap.get("LineNumber"), tempLineMap.get("DirectionCode"));
                Lines.add(tempLine);
                String key = tempLineMap.get("JourneyPatternPointNumber");
                String StopPointName = StopPointsMap.get(key);
                StopPoint tempStopPoint = new StopPoint(tempLineMap.get("JourneyPatternPointNumber"), StopPointName);
                Lines.get(index).AddStopPoint(tempStopPoint);
            }

            else
            {
                //Är vi på samma linje? I så fall ska vi bara lägga till ny stoppoint
                if(tempLine.getLineNumber().equals(tempLineMap.get("LineNumber")))
                {
                    //Hämta namn för stoppoint
                    String StopPointName = StopPointsMap.get(tempLineMap.get("JourneyPatternPointNumber"));

                    StopPoint tempStopPoint = new StopPoint(tempLineMap.get("JourneyPatternPointNumber"), StopPointName);
                    Lines.get(index).AddStopPoint(tempStopPoint);
                }

                //Annars byter vi linje och lägger till
                else
                {
                    tempLine = new Line(tempLineMap.get("LineNumber"), tempLineMap.get("DirectionCode"));
                    Lines.add(tempLine);

                    //Nu tittar vi på nästa Line
                    index++;
                }
            }
        }
        return Lines;
    }

    @GetMapping("/sortLinesDESC")
    public List<Line> getBiggestLines()
    {
        List<Line> Lines = getLines();
        Collections.sort(Lines);
        return Lines;
    }

    @GetMapping("/top10pretty")
    public String printtop10()
    {
        List<Line> Lines = getLines();
        Collections.sort(Lines);
        return PrettyPrintTop10(Lines);
    }

    public String PrettyPrintTop10(List<Line> Lines)
    {
        StringBuilder printout = new StringBuilder("<html><head><b>Top 10 Busslinjer</b></head>");
        for (int i = 0; i < 10; i++)
        {
            printout.append("<p>").append(i + 1).append(": Linje: ").append("- ").append(Lines.get(i).getLineNumber()).append(" | Antal hållplatser: ").append(Lines.get(i).getStopPointSize()).append("</p>");
        }
        printout.append("<head><b>Stationer på linje ").append(Lines.get(0).getLineNumber()).append("</b></head><br> <p>").append(Lines.get(0).toStringWEB());
        printout.append("</p></html>");

        return printout.toString();
    }

    public String getNr1LineWithStops(List<Line> Lines)
    {
        StringBuilder printout = new StringBuilder("Top 10 Busslinjer" + "\n");

        for (int i = 0; i < 10; i++)
        {
            printout.append(i + 1).append(" Linje: ").append("- ").append(Lines.get(i).getLineNumber()).append(" | Antal hållplatser: ").append(Lines.get(i).getStopPointSize()).append("\n\n");
        }

        printout.append("Stationer på linje ").append(Lines.get(0).getLineNumber()).append("\n").append(Lines.get(0).toString());

        return printout.toString();
    }


}
