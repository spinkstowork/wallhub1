package com.ef.dao;

import com.ef.DateUtility;
import com.ef.entity.HttpLogRequest;

import java.text.ParseException;

/**
 * Written By: Scott D. Pinkston Mar, 2018
 *
 * Used for simple integration testing.
 *
 */
public class OneOffInsertVisitor implements PersistenceVisitor {

    private HttpLogDAO httpLogDAO;

    protected OneOffInsertVisitor() {
    }

    public OneOffInsertVisitor( HttpLogDAO httpLogDAO ) {
        this.httpLogDAO = httpLogDAO;
    }

    @Override
    public void visit( String[] columns ) {
        HttpLogRequest request = createHttpLogRequest( columns );
        httpLogDAO.oneOffInsert( request );
    }

    public HttpLogRequest createHttpLogRequest( String[] columns ) {
        HttpLogRequest request = new HttpLogRequest();

        try {
            request.setReqtime( DateUtility.parse( columns[0] ) );
        } catch( ParseException e ) {
            throw new RuntimeException( "Cannot parse timestamp: " + columns[0] );
        }

        request.setIpAddr( columns[1] );
        request.setReqtype( columns[2] );
        request.setResponseCode( Integer.parseInt( columns[3] ) );
        request.setUserAgent( columns[4] );
        return request;
    }
}
