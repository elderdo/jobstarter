package com.boeing.jobstarter.services;

import java.io.File;
import java.sql.CallableStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.boeing.jobstarter.MyAppContext;
import com.boeing.jobstarter.action.interfaces.ICaller;
import com.boeing.jobstarter.action.interfaces.IDBConnection;
import com.boeing.jobstarter.model.ConnectionString;
import com.boeing.jobstarter.model.StatementParam;
import com.boeing.jobstarter.model.TableMetadata;

public class CallDDL implements ICaller {

    private IDBConnection connection;

    private MyAppContext myAppContext;

    protected HashMap<String, Object> parameters;

    public void execute() throws Exception {
        List<StatementParam> tables = Callable.getListParameters(String.valueOf(parameters.get("OBJECT")));
        List<String> statements = new ArrayList<String>();

        for (StatementParam table : tables) {

            System.out.print("Extracting Table: " + table.getColumnName() + " metadata...");

            List<TableMetadata> metadata = Callable.getTableMetadata(this.connection, table.getFile(), table
                    .getAlias(), table.getColumnName());
            System.out.println("Ok");
            System.out.print("Building DDL statement...");
            statements.add(this.connection.buildDDL(metadata, table.getValue()));
            System.out.println("Ok");
        }
        System.out.println("Disconnecting from source Database");
        this.connection.disconnect();
        ConnectionString cs = CallConnectionConfig.getConnectionString(String.valueOf(parameters
                .get("SCHEMADEST")));
        if (cs == null) {
            System.out.println("No Connection parameter found with name: "
                    + String.valueOf(parameters.get("CONNECTION")));
            return;
        }

        this.connection = myAppContext.getApplicationContext()
                .getBean(cs.getClassName(), IDBConnection.class);
        this.connection.connect(cs);
        for (String ddl : statements) {

            CallableStatement cstmt = (this.connection.getConnection()).prepareCall(ddl);
            try {
                cstmt.execute();
                System.out
                        .println("Table created in Schema: " + String.valueOf(parameters.get("SCHEMADEST")));
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                System.out.println("---------------------------------------------------------------------");
                System.out.println(ddl);
                System.out.println("---------------------------------------------------------------------");
            } finally {

                this.connection.close(cstmt);
            }
        }
    }

    @Override
    public void initCaller(HashMap<String, Object> parameters) throws Exception {
        this.parameters = parameters;
        ConnectionString cs = CallConnectionConfig.getConnectionString(String.valueOf(parameters
                .get("CONNECTION")));
        if (cs == null) {
            System.out.println("No Connection parameter found with name: "
                    + String.valueOf(parameters.get("CONNECTION")));
            return;
        }
        this.connection = myAppContext.getApplicationContext()
                .getBean(cs.getClassName(), IDBConnection.class);
        this.connection.connect(cs);

    }

    @Override
    public void terminateCaller() throws Exception {
        if (this.connection != null) {
            this.connection.disconnect();
        }

    }

    @Override
    public void errorHandler(Exception ex) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void report(String message, File file) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void setMyAppContext(MyAppContext myAppContext) {
        this.myAppContext = myAppContext;

    }

}
