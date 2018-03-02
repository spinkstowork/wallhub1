package com.ef.dao;

import com.ef.entity.BizIntel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Written By: Scott D. Pinkston Mar, 2018
 *
 * DAO module to encapsulate all the DML associated with table BIZINTEL.
 *
 */
public class BizIntelDAO {

    private static final Logger log = LoggerFactory.getLogger( BizIntelDAO.class );

    public static final String INSERT_SQL = "INSERT INTO BIZINTEL (IP_ADDR, IP_CNT, BIZ_REASON) VALUES(?,?,?)";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * This is a convenient way, a VERY SLOW WAY, to perform an insert for testing.
     * @param vo
     */
    public void oneOffInsert( final BizIntel vo ) {
        log.debug( "Attempting one off insert for BizIntel: {}", vo.getBizReason() );

        jdbcTemplate.update( INSERT_SQL, new Object[] { vo.getIpAddr(), vo.getIpCount(), vo.getBizReason() } );
    }

    public int[] batchInsert( List<BizIntel> requestList ) throws SQLException {
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
                    ps.setString( column++, requestList.get(row).getIpAddr() );
                    ps.setInt( column++, requestList.get(row).getIpCount() );
                    ps.setString( column++, requestList.get(row).getBizReason() );
                }

                @Override
                public int getBatchSize() {
                    return requestList.size();
                }
            } );
        }

    }
}
