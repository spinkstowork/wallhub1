package com.ef;

import com.ef.dao.BatchInsertAdapter;
import com.ef.dao.BizIntelDAO;
import com.ef.dao.HttpLogDAO;
import com.ef.entity.BizIntel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Written By: Scott D. Pinkston Mar, 2018
 *
 * Main application module for WalletHub code challenge.
 * See Java_MySQL_Test_Instructions.txt for more details.
 *
 * Command line parameters:
 *
 * "startDate" is of "yyyy-MM-dd.HH:mm:ss" format
 * "duration" can take only "hourly", "daily" as inputs
 * "threshold" can be an integer
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
        clArgs.interpret( args );

        HttpLogDAO httpLogDAO = ctx.getBean( "httpLogDAO", HttpLogDAO.class );
        int dbRowCount = httpLogDAO.queryRowCount();

        boolean loadLogDataFlag = false;

        if( clArgs.getAccessLogPathname() != null ) { // log file specified on cmd line, force load
            loadLogDataFlag = true;
        }

        if( dbRowCount == 0 ) { // no log data in DB
            log.info( "No access log data exists in DB. Forcing load.");
            loadLogDataFlag = true;
        }

        if( loadLogDataFlag ) { // load the DB
            BatchInsertAdapter fastVisitor = new BatchInsertAdapter( httpLogDAO );
            fastVisitor.setMaxBufferSize( 2000 );

            HttpLogParser parser = ctx.getBean( "accessLogParser", HttpLogParser.class );

            // if log path is specified on the command line, then override whats in the properties file
            if( clArgs.getAccessLogPathname() != null ) {
                log.info( "Overriding access log property: {}", parser.getFilename() );
                parser.setFilename( clArgs.getAccessLogPathname() );
            }
            parser.load( fastVisitor );
            fastVisitor.flush();
            dbRowCount = httpLogDAO.queryRowCount(); // resample
        }

        log.info( "DB is loaded and contains {} rows.", dbRowCount );

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

        log.info( "Task completed. Exiting..." );
    }

    private static List<BizIntel> createBizIntelFromResults( Map<String, Integer> ipMap, String reason ) {
        List<BizIntel> outputList = new ArrayList<>( ipMap.size() );
        ipMap.entrySet().stream().forEach( entry -> { outputList.add( new BizIntel( entry.getKey(), entry.getValue(), reason ) ); });

        return outputList;
    }
}
