package com.boeing.jobstarter.services;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.boeing.jobstarter.MyAppContext;
import com.boeing.jobstarter.model.StatementParam;
import com.boeing.jobstarter.utils.FileUtils;
import com.boeing.jobstarter.utils.Globals;
import com.boeing.jobstarter.utils.XMLHandler;

public class CallExtractSql extends Callable {

    public CallExtractSql(MyAppContext context) {
        myAppContext = context;
    }

    @Override
    public void execute() throws Exception {
        List<StatementParam> listParam = getListParameters(String.valueOf(parameters.get("PARAMETER")));
        String[] queryFiles = String.valueOf(parameters.get("OBJECT")).split(Globals.PARAM_SPLITER);
        for (int i = 0; i < queryFiles.length; i++) {
            logger.getLogger().info("Reading File: " + queryFiles[i]);
            String query = FileUtils.ReadFile(new File(XMLHandler
                    .getDirectories(Globals.TagNames.SCRIPT_DIR_TAG)
                    + queryFiles[i]));
            if (query == null) {
                throw new Exception("No query Or file found with name: " + queryFiles[i]);
            }
            List<StatementParam> stmtParams = getListBindVariables(query, listParam, queryFiles[i]);
            PreparedStatement pstmt = (this.connection.getConnection()).prepareStatement(query,
                    ResultSet.CONCUR_READ_ONLY);
            pstmt.setFetchSize(XMLHandler.getBulkSize());
            pstmt.setQueryTimeout(XMLHandler.getConnectionTimeOut());
            pstmt.setFetchDirection(ResultSet.FETCH_FORWARD);
            for (StatementParam p : stmtParams) {
                pstmt.setObject(p.getPosition(), p.getValue());
            }

            logger.getLogger().info("Executing query");
            ResultSet rset = null;
            try {
                rset = pstmt.executeQuery();

                logger.getLogger().info("Fetching records");
                fileWriter.InitWriter(queryFiles[i].contains(".") ? queryFiles[i].substring(0, queryFiles[i]
                        .lastIndexOf(".")) : queryFiles[i], ".csv", String.valueOf(parameters
                        .get("SCHEMADEST")));
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
