package com.ef.dao;

import com.ef.entity.HttpLogRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Written By: Scott D. Pinkston Mar, 2018
 *
 * DAO module to encapsulate all the DML associated with table HTTPLOG.
 *
 * Setting autocommit=false doesnt improve performance.
 */
public class HttpLogDAO {

    private static final Logger log = LoggerFactory.getLogger( HttpLogDAO.class );

    public static final String INSERT_SQL = "INSERT INTO HTTPLOG (REQ_TM, IP_ADDR, REQ_TYPE, RESP_CODE, USER_AGENT) VALUES(?,?,?,?,?)";
    public static final String COUNT_SQL = "SELECT COUNT(*) FROM HTTPLOG";

    public static final String IP_FIND_SQL = "SELECT REQ_TM,IP_ADDR,REQ_TYPE,RESP_CODE,USER_AGENT FROM HTTPLOG " +
        "WHERE IP_ADDR=?";

    public static final String DT_RANGE_CNT_SQL = "SELECT IP_ADDR, COUNT(*) AS IP_CNT FROM HTTPLOG " +
        "WHERE REQ_TM >= ? AND REQ_TM < ? GROUP BY IP_ADDR ORDER BY IP_CNT";

    public static final String DT_RANGE_CNT_HAVING_SQL = "SELECT IP_ADDR, COUNT(*) AS IP_CNT FROM HTTPLOG " +
        "WHERE REQ_TM >= ? AND REQ_TM < ? GROUP BY IP_ADDR HAVING IP_CNT > ? ORDER BY IP_CNT";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Simple count of DB rows.
     * @return
     */
    public int queryRowCount() {
        return jdbcTemplate.queryForObject( COUNT_SQL, new Object[] {}, Integer.class );
    }

    public Map<String,Integer> queryCntIpsForDateRange( Timestamp startPoint, Timestamp endPoint ) {
        log.debug( "queryCntIpsForDateRange {}, {}", startPoint, endPoint );

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet( DT_RANGE_CNT_SQL, new Object[] { startPoint, endPoint } );

        Map<String,Integer> outputMap = new HashMap<>();
        while( rowSet.next() ) {
            outputMap.put( rowSet.getString( "IP_ADDR" ), rowSet.getInt( "IP_CNT" ) );
        }

        return outputMap;
    }

    /**
     * Primary DB query operation of the exercise based on requirements.
     * @param startPoint
     * @param endPoint
     * @param threshold
     * @return
     */
    public Map<String,Integer> queryCntIpsForDateRangeHaving( Timestamp startPoint, Timestamp endPoint, int threshold ) {
        log.debug( "queryCntIpsForDateRangeHaving {}, {}, {}", startPoint, endPoint, threshold );

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet( DT_RANGE_CNT_HAVING_SQL, new Object[] { startPoint, endPoint, threshold } );

        Map<String,Integer> outputMap = new HashMap<>();
        while( rowSet.next() ) {
            outputMap.put( rowSet.getString( "IP_ADDR" ), rowSet.getInt( "IP_CNT" ) );
        }

        return outputMap;
    }

    /**
     * Secondary DB query operation based on requirements.
     * @param searchIP
     * @return
     */
    public List<HttpLogRequest> queryIps( String searchIP ) {
        log.debug( "queryIps {}", searchIP );
        return jdbcTemplate.query( IP_FIND_SQL, new Object[] { searchIP }, new HttpLogRowMapper() );
    }

    /**
     * Much could be done to improve performance with batch loading data. There are many
     * different ways to do it like using 'LOAD DATA', twiddling DB server settings (turn DB log off),
     * etc. The solution here is good enough in that it is a huge improvement over loading
     * single rows.
     *
     * ref: https://dev.mysql.com/doc/refman/5.6/en/optimizing-innodb-bulk-data-loading.html
     *
     * @param requestList
     * @return
     * @throws SQLException
     */
    public int[] batchInsert( List<HttpLogRequest> requestList ) throws SQLException {

        if( requestList.isEmpty() ) {
            log.debug( "Ignoring attempt to insert empty request list." );
            int[] empty = { };
            return empty;
        }
        else {
            log.debug( "Attempting to insert batch of {}", requestList.size() );

            return jdbcTemplate.batchUpdate( INSERT_SQL, new BatchPreparedStatementSetter() {
                @Override
                public void setValues( PreparedStatement ps, int row ) throws SQLException {
                    int column = 1;
                    ps.setTimestamp( column++, requestList.get(row).getReqtime() );
                    ps.setString( column++, requestList.get(row).getIpAddr() );
                    ps.setString( column++, requestList.get(row).getReqtype() );
                    ps.setInt( column++, requestList.get(row).getResponseCode() );
                    ps.setString( column++, requestList.get(row).getUserAgent() );
                }

                @Override
                public int getBatchSize() {
                    return requestList.size();
                }
            } );
        }
    }

    /**
     * This is a convenient way, a VERY SLOW WAY, to perform an insert for testing.
     * @param request
     */
    public void oneOffInsert( final HttpLogRequest request ) {
        log.debug( "Attempting one off insert for client: {}", request.getIpAddr() );

        jdbcTemplate.update( INSERT_SQL, new Object[] { request.getReqtime(), request.getIpAddr(),
            request.getReqtype(), request.getResponseCode(), request.getUserAgent() } );

    }

}
