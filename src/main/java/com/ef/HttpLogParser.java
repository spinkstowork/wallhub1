package com.ef;

import com.ef.dao.PersistenceVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Written By: Scott D. Pinkston Mar, 2018
 *
 * Read a text file containing http access log lines.
 */
public class HttpLogParser {

    private static final Logger log = LoggerFactory.getLogger( HttpLogParser.class );

    // expected number of columns per line in access log
    public static final int EXPECTED_COLUMNS = 5;

    private int linesCounter = 0; // raw lines parsed
    private int rowsLoadedCounter = 0; // rows actually loaded (attempted)
    private PersistenceVisitor lineVisitor;

    // vars below are set from values in jdbc.properties
    private String filename;

    @Autowired
    public void setFilename( String filename ) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void load( PersistenceVisitor visitor) throws IOException {
        File file = new File( filename );
        if( !file.exists() ) {
            log.warn( "File specified for loading: {} doesnt exist", filename );
        }
        else {
            log.info( "Loading HTTP access log rows from file: {}", filename );

            lineVisitor = visitor;

            Stream<String> stream = Files.lines( Paths.get( filename ) );
            stream.forEach( (this)::digestLine );

            log.debug( "{} lines processed", linesCounter );
            log.info( "{} rows from {} were loaded", rowsLoadedCounter, filename );
        }
    }

    /**
     * Break each line into its columns. We are expecting these columns:
     *
     * Date, IP, Request, Status, User Agent
     *
     * @param line
     */
    private void digestLine( String line ) {
        linesCounter++;
        String[] columns = line.split( "\\|" );

        if( columns.length == EXPECTED_COLUMNS ) {
            rowsLoadedCounter++;

            // req type and user agent needs to have dbl quotes removed first and last
            for( int x=0; x < columns.length; x++ ) {
                if( columns[x].startsWith( "\"" ) ) {
                    columns[x] = columns[x].substring( 1, columns[x].length() - 1 );
                }
            }
            lineVisitor.visit( columns );
        }
        else {
            String msg = String.format( "Line number: %d doesnt contain the expected amount of columns.", linesCounter );
            log.warn( msg );
        }
    }
}
