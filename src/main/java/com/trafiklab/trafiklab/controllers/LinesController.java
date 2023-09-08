package com.trafiklab.trafiklab.controllers;

import com.trafiklab.trafiklab.Services.LinesService;
import com.trafiklab.trafiklab.classes.StopPoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.trafiklab.trafiklab.classes.Line;
import java.util.*;

@RestController
public class LinesController {

    public LinesService service;
    //Nycklar
    final String key = "cce1c0b0c8cc495788b614af6be67184";
    final String DefaultTransMode = "&DefaultTransportModeCode=BUS";

    //Endpoints
    final String LineStopEndPoint = "https://api.sl.se/api2/LineData.json?model=JourneyPatternPointOnLine&key=" + key+ DefaultTransMode;
    final String StopPoinEndpoint = "https://api.sl.se/api2/LineData.json?model=StopPoint&key="+key;

    public LinesController()
    {
        service = new LinesService(key, DefaultTransMode);
    }

    @GetMapping("/listLines")
    public List<Line> getLines() throws Exception
    {
        return service.getLines(LineStopEndPoint, StopPoinEndpoint, DefaultTransMode);
    }

    //Sorterad komplett lista
    @GetMapping("/sortLinesDESC")
    public List<Line> getBiggestLines()
    {
        return service.getBiggestLines(LineStopEndPoint, StopPoinEndpoint, DefaultTransMode);
    }

    //Web-print
    @GetMapping("/top10pretty")
    public String printtop10()
    {
        return service.printtop10(LineStopEndPoint, StopPoinEndpoint, DefaultTransMode);
    }


    //Konsolprint
    public String PrettyPrintConsole(List<Line> Lines)
    {
        return service.PrettyPrintConsole(Lines);
    }

}
