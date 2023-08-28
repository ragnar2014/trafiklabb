package com.trafiklab.trafiklab.classes;

public final class StringTools {

    private StringTools()
    {

    }
    public static String RemoveQuotes(String aString)
    {
        return aString = aString.substring(1,aString.length()-1);
    }
}


