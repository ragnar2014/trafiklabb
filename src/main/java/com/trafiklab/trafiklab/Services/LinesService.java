package com.trafiklab.trafiklab.Services;

import com.trafiklab.trafiklab.Interface.LinesInterface;
import com.trafiklab.trafiklab.classes.Line;
import com.trafiklab.trafiklab.classes.StopPoint;
import org.springframework.web.client.RestTemplate;

import java.util.*;

public class LinesService implements LinesInterface {

    private String Key = null;
    private String DefaultTransMode = null;

    public LinesService(final String aKey, final String aDefaultTransMode)
    {
        Key = aKey;
        DefaultTransMode = aDefaultTransMode;
    }

    public ArrayList<Map<String, Object>> GetResponse(Object[] LineObjects,
                                                           String EndPoint,
                                                           Map<String, Object> outerHashMap,
                                                           Map<String, Object> responseDataMap,
                                                           RestTemplate template
                                                           ) throws Exception 
    {
        ArrayList<Map<String, Object>> LinesResponse;
        try
        {
            LineObjects = new Object[]{template.getForObject( EndPoint, Object.class)};
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
        catch (Exception e)
        {
            throw new Exception("Unable to fetch data for Lines");
        }
        
        return LinesResponse;
    }


    public List<Line> getLines(final String LineStopEndPoint,
                               final String StopPoinEndpoint,
                               String DefaultTransMode) throws Exception
    {
        ArrayList<Map<String, Object>> LinesResponse;
        ArrayList<Line> Lines = new ArrayList<>();
        ArrayList<Map<String, Object>> Stops;
        Map<String,String> StopPointsMap =  new HashMap<>();
        Object[] LineObjects = new Object[0];
        Map<String, Object> outerHashMap = null;
        Map<String, Object> responseDataMap = null;
        int index = 0;
        RestTemplate template = new RestTemplate();
        
        try
        {
            LinesResponse = GetResponse(LineObjects,
                                             LineStopEndPoint+DefaultTransMode,
                                             outerHashMap,
                                             responseDataMap,
                                             template);
        }
        
        catch(Exception e)
        {
            throw new Exception("Unable to fetch data for Lines");
        }

        try
        {
            Stops = GetResponse(LineObjects,
                                StopPoinEndpoint,
                                outerHashMap,
                                responseDataMap,
                                template);
        }

        catch(Exception e)
        {
            throw new Exception("Unable to fetch data for Stop points");
        }


        //Store stops in Map in order to find them easier
        for (Map<String, Object> stop : Stops)        {
            StopPointsMap.put((String) stop.get("StopPointNumber"), (String) stop.get("StopPointName"));
        }

        Line tempLine = new Line();

        //Loop through resultset and build complete Lines objects
        for (Map<String, Object> v : LinesResponse)
        {
            //Are we on first Line?
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
                //Are we on same Line, if so then we just add StopPoint
                if(tempLine.getLineNumber().equals(v.get("LineNumber").toString()))
                {
                    //Get name for StopPoint
                    String StopPointName = StopPointsMap.get(v.get("JourneyPatternPointNumber").toString());

                    StopPoint tempStopPoint = new StopPoint(v.get("JourneyPatternPointNumber").toString(), StopPointName);
                    Lines.get(index).AddStopPoint(tempStopPoint);
                }
                //Otherwise we change new Line and add it to the list
                else
                {
                    tempLine = new Line(v.get("LineNumber").toString(), v.get("DirectionCode").toString());
                    Lines.add(tempLine);

                    //Now we look at the next Line
                    index++;
                }
            }
        }
        return Lines;
    }

    public List<Line> getBiggestLines(String LineStopEndPoint, String StopPoinEndpoint, String DefaultTransMode)
    {
        List<Line> Lines = new ArrayList<>();
        try
        {
            Lines = getLines(LineStopEndPoint, StopPoinEndpoint, DefaultTransMode);
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

    public String printtop10(String LineStopEndPoint, String StopPoinEndpoint, String DefaultTransMode)
    {
        List<Line> Lines;
        try
        {
            Lines = getLines(LineStopEndPoint, StopPoinEndpoint, DefaultTransMode);
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
