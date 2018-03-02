package com.ef.dao;

import com.ef.entity.HttpLogRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Written By: Scott D. Pinkston Mar, 2018
 *
 * Allows for simple buffering of objects prior to inserting them in the DB.
 */
public class BatchInsertAdapter extends OneOffInsertVisitor implements PersistenceVisitor {

    private static final Logger log = LoggerFactory.getLogger( BatchInsertAdapter.class );

    public static final int DEFAULT_SIZE = 1000;

    private int maxBufferSize = DEFAULT_SIZE; // how big is the batch?

    private final HttpLogDAO httpLogDAO;
    private List<HttpLogRequest> bufferedList;

    public BatchInsertAdapter( HttpLogDAO httpLogDAO ) {
        this.httpLogDAO = httpLogDAO;
        this.bufferedList = new ArrayList<>( maxBufferSize );
    }

    public void setMaxBufferSize( int maxBufferSize ) {
        this.maxBufferSize = maxBufferSize;
        log.debug( "Setting max buffer size to {}", maxBufferSize );
    }

    @Override
    public void visit( String[] columns ) {
        bufferedList.add( createHttpLogRequest( columns ) );

        if( bufferedList.size() >= maxBufferSize ) {
            try {
                flush();
            } catch( SQLException ex ) {
                log.error( "Exception encountered during flush.", ex );
            }
        }
    }

    /**
     * Ensures any remaining elements in the list are committed to the DB.
     */
    public void flush() throws SQLException {
        if( !bufferedList.isEmpty() ) {
            log.info( "Flushing batch of {} inserts", bufferedList.size() );
            httpLogDAO.batchInsert( bufferedList );
            bufferedList.clear();
        }
    }
}
