package com.boeing.jobstarter.services;

import java.sql.CallableStatement;
import java.util.List;

import com.boeing.jobstarter.MyAppContext;
import com.boeing.jobstarter.model.ProcedureMetadata;
import com.boeing.jobstarter.model.StatementParam;
import com.boeing.jobstarter.utils.MapTypes;

public class CallStoredProcedure extends Callable {

    public CallStoredProcedure(MyAppContext context) {
        myAppContext = context;
    }

    @Override
    public void execute() throws Exception {
        List<StatementParam> stmtParameters = getListParameters(String.valueOf(parameters.get("PARAMETER")));
        List<StatementParam> procedures = getListParameters(String.valueOf(parameters.get("OBJECT")));
        for (StatementParam procedure : procedures) {
            logger.getLogger().info("Executing Procedure: " + procedure.getColumnName());
            List<ProcedureMetadata> metadata = getProcedureMetadata(this.connection, procedure.getAlias(),
                    procedure
                    .getColumnName(), procedure.getValue(), null);
            CallableStatement cstmt = (this.connection.getConnection()).prepareCall(prepareStatement(
                    procedure.getValue(), metadata));
            for (ProcedureMetadata m : metadata) {
                StatementParam p = getParameterByFileAliasColumn(null, null, m.getColumnName(),
                        stmtParameters);
                if (p == null) {
                    p = getParameterByPosition(m.getColumnPosition(), stmtParameters);
                }
                MapTypes.fulFillPreparedStatement(cstmt, m.getColumnPosition() + 1, m.getColumnDataType(), p
                        .getValue());
            }
            try {
                cstmt.execute();
            } catch (Exception ex) {
                throw ex;
            } finally {
                connection.showDBInternalLog();
                connection.close(cstmt);
            }
        }
    }
}
