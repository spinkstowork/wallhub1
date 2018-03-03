package com.ef;

import com.ef.dao.BatchInsertAdapter;
import com.ef.dao.BizIntelDAO;
import com.ef.dao.HttpLogDAO;
import com.ef.entity.BizIntel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Written By: Scott D. Pinkston Mar, 2018
 *
 * Main application module for WalletHub code challenge.
 * See Java_MySQL_Test_Instructions.txt for requirements.
 *
 * Command line parameters:
 *
 * REQUIRED "startDate" is of "yyyy-MM-dd.HH:mm:ss" format
 * REQUIRED "duration" can take only "hourly", "daily" as inputs
 * REQUIRED "threshold" can be an integer
 * OPTIONAL "accesslog" is a path/file name
 *
 */
public class Parser { // the name of this class was specified in the requirements from WalletHub

    private static final Logger log = LoggerFactory.getLogger( Parser.class );

    public static void main( String[] args ) throws Exception {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-config.xml");

        // "--accesslog=midsize.log",
//        String[] fakeargs = {
//            "--startDate=2017-01-01.00:00:00",
//            "--duration=hourly",
//            "--threshold=10"
//        };

        CommandLineArgs clArgs = new CommandLineArgs();
        boolean argsValidatedFlag = clArgs.interpret( args );

        boolean loadLogDataFlag = false;

        if( clArgs.getAccessLogPathname() != null ) { //
            log.info( "Access log file specified on command line, forcing load." );
            loadLogDataFlag = true;
        }

        HttpLogDAO httpLogDAO = ctx.getBean( "httpLogDAO", HttpLogDAO.class );
        int dbRowCount = httpLogDAO.queryRowCount();

        if( dbRowCount == 0 ) { // no log data in DB
            log.info( "No access log data exists in DB. Forcing load.");
            loadLogDataFlag = true;
        }

        if( loadLogDataFlag ) { // load the DB
            dbRowCount = loadData( ctx, clArgs, httpLogDAO );
        }

        log.info( "HTTPLOG table is loaded and contains {} rows.", dbRowCount );

        if( argsValidatedFlag ) {
            runQuery( ctx, clArgs, httpLogDAO );
        }
        else {
            log.warn( "Skipping query of HTTPLOG data. Invalid command line arguments." );
        }

        log.info( "Task completed. Exiting..." );
    }

    /**
     * Load data from text file. Data output to DB is additive. Use init.sql script to initialize/clear.
     * @param ctx
     * @param clArgs
     * @param httpLogDAO
     * @return
     * @throws IOException
     * @throws SQLException
     */
    private static int loadData( ApplicationContext ctx, CommandLineArgs clArgs, HttpLogDAO httpLogDAO ) throws IOException, SQLException {
        int dbRowCount;BatchInsertAdapter fastVisitor = new BatchInsertAdapter( httpLogDAO );
        fastVisitor.setMaxBufferSize( 10000 );

        HttpLogParser parser = ctx.getBean( "accessLogParser", HttpLogParser.class );

        // if log path is specified on the command line, then override whats in the properties file
        if( clArgs.getAccessLogPathname() != null ) {
            log.info( "Overriding access log property: {}", parser.getFilename() );
            parser.setFilename( clArgs.getAccessLogPathname() );
        }
        parser.load( fastVisitor );
        fastVisitor.flush();
        dbRowCount = httpLogDAO.queryRowCount(); // resample
        return dbRowCount;
    }

    /**
     * Much of the thrust of this app is evaluating command line parms and running a query for the results.
     * However, this method can not be called without all args present and verified (see above).
     * @param ctx
     * @param clArgs
     * @param httpLogDAO
     * @throws SQLException
     */
    private static void runQuery( ApplicationContext ctx, CommandLineArgs clArgs, HttpLogDAO httpLogDAO ) throws SQLException {
        Map<String, Integer> ipMap = httpLogDAO.queryCntIpsForDateRangeHaving( clArgs.getStartPoint(),
            clArgs.getEndPoint(), clArgs.getThreshold() );

        System.out.println( ipMap.size() + " IP entry(s) found having more than " + clArgs.getThreshold() + " instances" );

        if( !ipMap.isEmpty() ) {
            ipMap.entrySet().stream().forEach( entry -> {
                System.out.println( entry.getKey() + ", " + entry.getValue() );
            } );

            // SDP, per the requirements "AND also load them to another MySQL table with comments on why it's blocked."
            // which I find to be vague, load results to another table
            BizIntelDAO bizIntelDAO = ctx.getBean( "bizIntelDAO", BizIntelDAO.class );
            String reason = String.format( "Exceeded count %s of: %d", clArgs.getDuration().toString(), clArgs.getThreshold() );
            List<BizIntel> bizIntelList = createBizIntelFromResults( ipMap, reason );

            log.info( "Loading {} results to business intel table", ipMap.size() );
            log.info( "Reason: {}", reason );
            bizIntelDAO.batchInsert( bizIntelList );
        }
    }

    /**
     * Small helper method.
     * @param ipMap
     * @param reason
     * @return
     */
    private static List<BizIntel> createBizIntelFromResults( Map<String, Integer> ipMap, String reason ) {
        List<BizIntel> outputList = new ArrayList<>( ipMap.size() );
        ipMap.entrySet().stream().forEach( entry -> { outputList.add( new BizIntel( entry.getKey(), entry.getValue(), reason ) ); });

        return outputList;
    }
}
