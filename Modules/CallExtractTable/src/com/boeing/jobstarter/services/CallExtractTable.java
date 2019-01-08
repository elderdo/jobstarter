package com.boeing.jobstarter.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.boeing.jobstarter.MyAppContext;
import com.boeing.jobstarter.model.StatementParam;
import com.boeing.jobstarter.model.TableMetadata;
import com.boeing.jobstarter.utils.MapTypes;
import com.boeing.jobstarter.utils.XMLHandler;

public class CallExtractTable extends Callable {

    public CallExtractTable(MyAppContext context) {
        myAppContext = context;
    }

    @Override
    public void execute() throws Exception {

        List<StatementParam> tables = getListParameters(String.valueOf(parameters.get("OBJECT")));
        List<StatementParam> listStmtParam = getListParameters(String.valueOf(parameters.get("PARAMETER")));

        for (StatementParam table:tables) {
            
            logger.getLogger().info("Extracting Table: " + table.getValue());
            logger.getLogger().info("Getting metadata");

            List<TableMetadata> metadata = getTableMetadata(this.connection, table.getAlias(), table
                    .getColumnName(),
                    table.getValue());
            // get all parameters for current table (table_name.column_name=value)
            List<StatementParam> stmtParam = getParametersByAlias(table.getValue(), listStmtParam);
            // If no parameters returned for table(alias), get all parameters without alias
            // (column_name=value).
            if (stmtParam == null || stmtParam.isEmpty()) {
                stmtParam = getParametersByNoAlias(listStmtParam);
            }
            logger.getLogger().info("Creating statement");
            String query = prepareStatement(BuildObjetQuery(table), metadata, stmtParam);
            logger.getLogger().info(query);
            PreparedStatement pstmt = (this.connection.getConnection()).prepareStatement(query,
                    ResultSet.CONCUR_READ_ONLY);
            pstmt.setFetchSize(XMLHandler.getBulkSize());
            pstmt.setQueryTimeout(XMLHandler.getConnectionTimeOut());
            pstmt.setFetchDirection(ResultSet.FETCH_FORWARD);
            int pos = 1;
            TableMetadata m = null;
            for (StatementParam p : stmtParam) {
                m = getMetadataByName(p.getColumnName(), metadata);
                if (m != null) {
                    MapTypes.fulFillPreparedStatement(pstmt, pos++, MapTypes.map(m.getColumnType()), p
                            .getValue());
                }
            }
            ResultSet rset = null;
            logger.getLogger().info("Executing query");
            try {
                rset = pstmt.executeQuery();

                logger.getLogger().info("Fetching records");
                fileWriter.InitWriter(table.getValue(), ".csv", String.valueOf(parameters.get("SCHEMADEST")));
                fileWriter.writeResultSetToCSV(rset, (parameters.get("NOHEADER") == null));
                afterExecute(fileWriter.getFile());
            } catch (Exception ex) {
                throw ex;
            } finally {
                connection.close(rset);
                connection.close(pstmt);
            }
        }
    }
}
