package com.boeing.jobstarter.connection;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import com.boeing.jobstarter.action.interfaces.IDBConnection;
import com.boeing.jobstarter.model.ConnectionString;
import com.boeing.jobstarter.model.TableMetadata;
import com.boeing.jobstarter.utils.Crypto;
import com.boeing.jobstarter.utils.Log;


public class TDConnection implements IDBConnection {

    private Connection connection;

    private Log logger;

    private ConnectionString connectionParam;

    @Override
    public void connect(ConnectionString connString) throws Exception {
        connectionParam = connString;
        if (connectionParam != null) {
            this.doConnect();
        }

    }

    private void doConnect() throws Exception {
        Class.forName(connectionParam.getDriver());

        String url = "jdbc:teradata://";

        url += (connectionParam.getServerHost() != null && connectionParam.getServerHost().length() != 0 ? connectionParam
                .getServerHost()
                : "")
                + (connectionParam.getServerPort() != null && connectionParam.getServerPort().length() != 0 ? ":"
                        + connectionParam.getServerPort()
                        : "")
                + (connectionParam.getDatabaseName() != null
                        && connectionParam.getDatabaseName().length() != 0 ? "/database="
                        + connectionParam.getDatabaseName() : "");

        logger.getLogger().info(
                "Connecting to Teradata Database [" + connectionParam.getDatabaseName() + "] using: "
                        + connectionParam.getConnectionName() + " With : " + url);

        this.setConnection(DriverManager.getConnection(url, connectionParam.getUser(), Crypto
                .decrypt(connectionParam.getPassword())));

        logger.getLogger().info("Database connection established successfuly...");
    }

    @Override
    public void disconnect() throws Exception {
        if (getConnection() != null) {
            this.getConnection().close();
            logger.getLogger().info("Disconnected from Database successfuly...");
        }

    }

    @Override
    public Connection getConnection() {

        return connection;
    }

    @Override
    public ConnectionString getConnectionParam() {
        return connectionParam;
    }

    @Override
    public boolean isConnected() throws Exception {
        return (this.getConnection() != null && !this.getConnection().isClosed());
    }

    @Override
    public void close(Object obj) throws Exception {
        if (obj == null) {
            return;
        }
        if (obj instanceof ResultSet) {
            ((ResultSet) obj).close();
        } else if (obj instanceof Statement) {
            ((Statement) obj).close();
        } else if (obj instanceof Connection) {
            ((Connection) obj).close();
        } else if (obj instanceof CallableStatement) {
            ((CallableStatement) obj).close();
        } else if (obj instanceof PreparedStatement) {
            ((PreparedStatement) obj).close();
        } else {
            throw new Exception("Close not implemented for object type" + obj.getClass().getName());
        }

    }

    @Override
    public void showDBInternalLog() throws Exception {
        // TODO Auto-generated method stub

    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setConnectionParam(ConnectionString connectionParam) {
        this.connectionParam = connectionParam;
    }

    public Log getLogger() {
        return logger;
    }

    public void setLogger(Log logger) {
        this.logger = logger;
    }

    @Override
    public String buildDDL(List<TableMetadata> metadata, String tableName) throws Exception {

        StringBuilder ddl = new StringBuilder();
        ddl.append("CREATE TABLE ").append(tableName).append("(").append("\n");
        int colNum = 0;
        for (TableMetadata m : metadata) {

            ddl.append(m.getColumnName()).append(" ");

            ddl.append(mapToOracle(m));

            colNum++;
            if (colNum < metadata.size())
                ddl.append(", ").append("\n");
        }
        return ddl.append(")").toString();
    }

    public static String mapToOracle(TableMetadata metadata) throws Exception {
        switch (metadata.getColumnType().toUpperCase()) {
            case "CHAR":
            case "VARCHAR2":
            case "VARCHAR":
            case "NCHAR":
                return "VARCHAR2(" + metadata.getColumnSize() + ")";
            case "LONG":
                return "LONG";
            case "NUMBER":
            case "DECIMAL":
            case "TINYINT":
            case "BYTEINT":
            case "SMALLINT":
            case "INTEGER":
                return "NUMBER(" + metadata.getColumnSize() + "," + metadata.getColumnPrecision() + ")";
            case "TIMESTAMP":
            case "DATE":
                return "DATE";
            case "BLOB":
                return "BLOB";
            case "CLOB":
                return "CLOB";
            default:
                throw new Exception("Invalid or Unmapped SQL type (" + metadata.getColumnType() + ")");
        }
    }
}
