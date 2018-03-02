package com.ef.dao;

import com.ef.entity.HttpLogRequest;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Written By: Scott D. Pinkston Mar, 2018
 *
 * Row mapper. Streamlines sending HttpLogRequest to/from DB.
 */
public class HttpLogRowMapper implements RowMapper<HttpLogRequest> {

    @Override
    public HttpLogRequest mapRow( ResultSet rs, int rowNum ) throws SQLException {
        HttpLogRequest request = new HttpLogRequest();

        request.setReqtime( rs.getTimestamp( "REQ_TM" ) );
        request.setIpAddr( rs.getString( "IP_ADDR" ) );
        request.setReqtype( rs.getString( "REQ_TYPE" ) );
        request.setResponseCode( rs.getInt( "RESP_CODE" ) );
        request.setUserAgent( rs.getString( "USER_AGENT" ) );
        return request;
    }
}
