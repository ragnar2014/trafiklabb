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
    public List<Line> getLines() throws Exception
    {
        ArrayList<Map<String, Object>> LinesResponse;
        ArrayList<Line> Lines = new ArrayList<>();
        ArrayList<Map<String, String>> Stops;
        Map<String,String> StopPointsMap =  new HashMap<>();
        Object[] LineObjects;
        Map<String, Object> outerHashMap;
        Map<String, Object> responseDataMap;
        int index = 0;
        RestTemplate template = new RestTemplate();

        //Detta är ju inte snyggt men tror jag täcker möjliga felen i denna hantering
//////////////////////Lines/////////////////////////////////////////////////////////////////////////////////////////////
        try
        {
            LineObjects = new Object[]{template.getForObject(LineStopEndPoint, Object.class)};
            outerHashMap = (Map<String, Object>) LineObjects[0];
            if(outerHashMap != null && outerHashMap.containsKey("ResponseData"))
            {
                responseDataMap = (Map<String, Object>) outerHashMap.get("ResponseData");
                if(responseDataMap != null && responseDataMap.containsKey("Result"))
                {
                    LinesResponse = (ArrayList<Map<String, Object>>) responseDataMap.get("Result");
                }
                else
                {
                    throw new Exception("No Result in LinesResponse");
                }
            }
            else
            {
                throw new Exception("outerHashmap is null or doesn't contain ResponseData");
            }
        }
        catch (ClassCastException e)
        {
            throw new Exception("Unable to fetch data for Lines");
        }

/////////////////////StopPoints///////////////////////////////////////////////////////////////////////////////
         try
         {
             Object[] StopObjects = new Object[]{template.getForObject(StopPoinEndpoint, Object.class)};
             outerHashMap = (Map<String, Object>) StopObjects[0];

             if (outerHashMap != null && outerHashMap.containsKey("ResponseData"))
             {
                 responseDataMap = (Map<String, Object>) outerHashMap.get("ResponseData");
                 if (responseDataMap != null && responseDataMap.containsKey("Result"))
                 {
                     Stops = (ArrayList<Map<String, String>>) responseDataMap.get("Result");
                 }
                 else
                 {
                     throw new Exception("No Result in Stops");
                 }
             }
             else
             {
                 throw new Exception("outerHashmap is null or doesn't contain ResponseData");
             }
         }
        catch (Exception e)
        {
            throw new Exception("Unable to fetch data for Stop points");
        }
////////////////////////////////////////////////////////////////////////////////////////////////////

        //Lagrar alla hållplatser i Map för att lätta kunna hitta rätt
        for (var stop : Stops)
        {
            StopPointsMap.put(stop.get("StopPointNumber"), stop.get("StopPointName"));
        }

        Line tempLine = new Line();

        //Loopar genom resultatset och bygger kompletta Lines-objekt till Lines-listan
        for (Map<String, Object> v : LinesResponse)
        {
            //Om vi är på första linjen
            if(tempLine.getLineNumber()== null)
            {
                tempLine = new Line(v.get("LineNumber").toString(), v.get("DirectionCode").toString());
                Lines.add(tempLine);
                String key = v.get("JourneyPatternPointNumber").toString();
                String StopPointName = StopPointsMap.get(key);
                StopPoint tempStopPoint = new StopPoint(v.get("JourneyPatternPointNumber").toString(), StopPointName);
                Lines.get(index).AddStopPoint(tempStopPoint);
            }
            else
            {
                //Är vi på samma linje? I så fall ska vi bara lägga till ny stoppoint
                if(tempLine.getLineNumber().equals(v.get("LineNumber").toString()))
                {
                    //Hämta namn för stoppoint
                    String StopPointName = StopPointsMap.get(v.get("JourneyPatternPointNumber").toString());

                    StopPoint tempStopPoint = new StopPoint(v.get("JourneyPatternPointNumber").toString(), StopPointName);
                    Lines.get(index).AddStopPoint(tempStopPoint);
                }
                //Annars byter vi linje och lägger till ny Line
                else
                {
                    tempLine = new Line(v.get("LineNumber").toString(), v.get("DirectionCode").toString());
                    Lines.add(tempLine);

                    //Nu tittar vi på nästa Line
                    index++;
                }
            }
        }
        return Lines;
    }

    //Sorterad komplett lista
    @GetMapping("/sortLinesDESC")
    public List<Line> getBiggestLines()
    {
        List<Line> Lines = new ArrayList<>();
        try
        {
            Lines = getLines();
            if(Lines.isEmpty())
            {
                return Lines;
            }
            else
            {
                Collections.sort(Lines);
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        return Lines;
    }

    //Web-print
    @GetMapping("/top10pretty")
    public String printtop10()
    {
        List<Line> Lines;
        try
        {
            Lines = getLines();
            Collections.sort(Lines);
            return PrettyPrintTop10(Lines);
        }
        catch (Exception e)
        {
            return e.toString();
        }


    }

    //Stränguppbyggnad för Web-print
    public String PrettyPrintTop10(List<Line> Lines)
    {
        StringBuilder printout = new StringBuilder("<html><head><b>Top 10 Busslinjer</b></head>");
        for (int i = 0; i < 10; i++)
        {
            printout.append("<p>").append(i + 1).append(": Linje: ").append("- ").
                     append(Lines.get(i).getLineNumber()).
                     append(" | Antal hållplatser: ").
                     append(Lines.get(i).getStopPointSize()).
                     append("</p>");
        }
        printout.append("<head><b>Stationer på linje ").
                 append(Lines.get(0).getLineNumber()).
                 append("</b></head><br> <p>").
                 append(Lines.get(0).toStringWEB());
        printout.append("</p></html>");

        return printout.toString();
    }

    //Konsolprint
    public String PrettyPrintConsole(List<Line> Lines)
    {
        StringBuilder printout = new StringBuilder("Top 10 Busslinjer" + "\n");

        for (int i = 0; i < 10; i++)
        {
            printout.append(i + 1).append(" Linje: ").
                     append("- ").
                     append(Lines.get(i).getLineNumber()).
                     append(" | Antal hållplatser: ").
                     append(Lines.get(i).getStopPointSize()).
                     append("\n\n");
        }

        printout.append("Stationer på linje ").
                 append(Lines.get(0).getLineNumber()).
                 append("\n").append(Lines.get(0).toString());

        return printout.toString();
    }

}
