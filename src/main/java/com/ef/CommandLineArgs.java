package com.ef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Written By: Scott D. Pinkston Mar, 2018
 *
 * Encapsulate logic needed to process command line args.
 */
public class CommandLineArgs {

    public enum Durations { HOURLY, DAILY };

    public static final String START_DATE_ARG = "--startDate";
    public static final String DURATION_ARG = "--duration";
    public static final String THRESHOLD_ARG = "--threshold";
    public static final String ACCESS_LOG_ARG = "--accesslog";

    private static final Logger log = LoggerFactory.getLogger( CommandLineArgs.class );

    // todo: these vars should probably be located in a separate value object
    private Timestamp startPoint;
    private Timestamp endPoint; // calculated below
    private Durations duration;
    private int threshold;
    private String accessLogPathname;

    public Timestamp getStartPoint() {
        return startPoint;
    }

    public Timestamp getEndPoint() {
        return endPoint;
    }

    public Durations getDuration() {
        return duration;
    }

    public int getThreshold() {
        return threshold;
    }

    public String getAccessLogPathname() {
        return accessLogPathname;
    }

    public boolean interpret( String[] args ) {
        boolean retVal = true;

        Map<String,String> cmdLineSwitchesMap = new HashMap<>();

        log.info( "Interpreting {} actual command line arguments", args.length );
        for( String arg : args ) {
            // ok, take a hit on loop interations to promote code clarity
            boolean anyFlag = evaluateForSwitch( START_DATE_ARG, arg, cmdLineSwitchesMap ) ||
                evaluateForSwitch( DURATION_ARG, arg, cmdLineSwitchesMap ) ||
                evaluateForSwitch( THRESHOLD_ARG, arg, cmdLineSwitchesMap ) ||
                evaluateForSwitch( ACCESS_LOG_ARG, arg, cmdLineSwitchesMap );
        }

        // This means ACCESS_LOG_ARG was specified, which is an optional arg
        if( cmdLineSwitchesMap.size() == 4 ) {
            accessLogPathname = cmdLineSwitchesMap.get( ACCESS_LOG_ARG );
            log.info( "Found command line switch for access log pathname: {}", accessLogPathname );
            cmdLineSwitchesMap.remove( ACCESS_LOG_ARG );
        }

        // if we were successful, we will have 3 args in the cmdLineMap
        if( cmdLineSwitchesMap.size() != 3 ) {
            log.error( "Command line switches are missing or incorrect." );
            log.info( "Example usage: --startPoint=2017-01-01.13:00:00 --duration=hourly --threshold=100" );

            String[] requiredSwitches = { START_DATE_ARG, DURATION_ARG, THRESHOLD_ARG };
            for( String reqArg : requiredSwitches ) {
                if( !cmdLineSwitchesMap.containsKey( reqArg ) ) {
                    log.error( "[{}] command line switch not found", reqArg );
                }
            }

            retVal = false;
        }
        else {
            log.info( "All required command line switches are present" );

            try {
                // little gotcha here: based on the requirements, there is a difference from how this is
                // specified on the command line verses the timestamps in the access log data file.
                // Its an extra dot, filter it out
                String startDateRawValue = cmdLineSwitchesMap.get( START_DATE_ARG );

                if( startDateRawValue.indexOf( '.' ) > 0 ) {
                    log.debug("Cleaning up startDateRawValue. Removing extraneous period character.");
                    startDateRawValue = startDateRawValue.replace(  '.', ' '  );
                }
                startPoint = DateUtility.parseCmdLine( startDateRawValue );
                log.info( "Start point for date range to: {}", startPoint.toString() );

            } catch( ParseException e ) {
                log.error( "Error parsing start date: {}", cmdLineSwitchesMap.get( START_DATE_ARG ) );
                return false;
            }

            try {
                duration = Durations.valueOf( cmdLineSwitchesMap.get( DURATION_ARG ).toUpperCase() );

                log.info( "Duration is: " + duration.toString() );
                if( duration.equals( Durations.HOURLY ) ) {
                    endPoint = Timestamp.from( startPoint.toInstant().plusSeconds( DateUtility.SECS_IN_HOUR ) );
                }
                else {
                    endPoint = Timestamp.from( startPoint.toInstant().plusSeconds( DateUtility.SECS_IN_DAY ) );
                }
                log.info( "Setting end point for date range to: {}", endPoint.toString() );
            }
            catch( Exception ex ) {
                log.error( "Duration must be [hourly] or [daily]. Invalid duration: {}", cmdLineSwitchesMap.get( DURATION_ARG ) );
                return false;
            }

            try {
                threshold = Integer.parseInt( cmdLineSwitchesMap.get( THRESHOLD_ARG ) );
                log.info( "Threshold is: {}", threshold );
            }
            catch( Exception ex ) {
                log.error( "Threshold must be an integer. Error parsing threshold: " + cmdLineSwitchesMap.get( THRESHOLD_ARG ) );
                return false;
            }

            log.info( "All required command line switch values have been validated" );
        }

        return retVal;
    }

    private boolean evaluateForSwitch( final String argToMatch, final String argToken, Map<String, String> targetMap ) {
        boolean retVal = false;
        int pos;

        if( (pos = argToken.indexOf( argToMatch )) >= 0 ) {
            pos = argToken.substring( pos ).indexOf( "=" );

            if( pos == -1 ) {
                log.debug( "Argument {} has a missing value.", argToMatch );
                return false;
            }
            targetMap.put( argToMatch, argToken.substring( pos+1 ) );
            log.debug( "Found argument {}", argToMatch );
            retVal = true;
        }

        return retVal;
    }
}
