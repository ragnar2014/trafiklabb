package com.trafiklab.trafiklab.Interface;

import com.trafiklab.trafiklab.classes.Line;

import java.util.List;

public interface LinesInterface {
    public List<Line> getLines(final String LineStopEndPoint,
                               final String StopPoinEndpoint,
                               String DefaultTransMode) throws Exception;

    public List<Line> getBiggestLines(String LineStopEndPoint, String StopPoinEndpoint, String DefaultTransMode);

    public String printtop10(String LineStopEndPoint, String StopPoinEndpoint, String DefaultTransMode);


}
