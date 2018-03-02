package com.ef;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Written By: Scott D. Pinkston Mar, 2018
 *
 * Simple class. Holds date related routines.
 */
public class DateUtility {

    public static final String ACCESS_LOG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String CMD_LINE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final long SECS_IN_HOUR = 60 * 60;
    public static final long SECS_IN_DAY = SECS_IN_HOUR * 24;

    private static final SimpleDateFormat sdf = new SimpleDateFormat( ACCESS_LOG_DATE_FORMAT );
    private static final SimpleDateFormat sdfCmdLine = new SimpleDateFormat( CMD_LINE_FORMAT );

    public static Timestamp parse( String reqtime ) throws ParseException {
        return new Timestamp( sdf.parse( reqtime ).getTime() );
    }

    public static Timestamp parseCmdLine( String reqtime ) throws ParseException {
        return new Timestamp( sdfCmdLine.parse( reqtime ).getTime() );
    }
}
