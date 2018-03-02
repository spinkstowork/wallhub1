package com.ef.entity;

import java.sql.Timestamp;

/**
 * Written By: Scott D. Pinkston Mar, 2018
 *
 * Value object for a http log request query result.
 *
 */
public class BizIntel {

    private String ipAddr; // todo: this should be a InetAddress
    private int ipCount = 0;
    private String bizReason;

    public BizIntel( String ipAddr, Integer ipCount, String bizReason ) {
        this.ipAddr = ipAddr;
        this.ipCount = ipCount;
        this.bizReason = bizReason;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr( String ipAddr ) {
        this.ipAddr = ipAddr;
    }

    public int getIpCount() {
        return ipCount;
    }

    public void setIpCount( int ipCount ) {
        this.ipCount = ipCount;
    }

    public String getBizReason() {
        return bizReason;
    }

    public void setBizReason( String bizReason ) {
        this.bizReason = bizReason;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append( ipAddr );
        sb.append( ',' );
        sb.append( ipCount );
        sb.append( ',' );
        sb.append( bizReason );

        return sb.toString();
    }
}
