package com.ef.entity;

import java.sql.Timestamp;

/**
 * Written By: Scott D. Pinkston Mar, 2018
 *
 * Value object for a http log request.
 *
 */
public class HttpLogRequest {

    // todo: might we need an id at some point here?
    // private long id;

    private Timestamp reqtime;
    private String ipAddr; // todo: this should be a InetAddress
    private String reqtype;
    private int responseCode = 0;
    private String userAgent;

    public HttpLogRequest() {
    }

    public Timestamp getReqtime() {
        return reqtime;
    }

    public void setReqtime( Timestamp reqtime ) {
        this.reqtime = reqtime;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr( String ipAddr ) {
        this.ipAddr = ipAddr;
    }

    public String getReqtype() {
        return reqtype;
    }

    public void setReqtype( String reqtype ) {
        this.reqtype = reqtype;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode( int responseCode ) {
        this.responseCode = responseCode;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent( String userAgent ) {
        this.userAgent = userAgent;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append( reqtime.toString() );
        sb.append( ',' );
        sb.append( ipAddr );
        sb.append( ',' );
        sb.append( reqtype );
        sb.append( ',' );
        sb.append( responseCode );
        sb.append( ',' );
        sb.append( userAgent );

        return sb.toString();
    }
}
