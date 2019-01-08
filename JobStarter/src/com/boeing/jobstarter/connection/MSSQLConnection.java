package com.boeing.jobstarter.connection;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;

import com.boeing.jobstarter.action.interfaces.IDBConnection;
import com.boeing.jobstarter.model.ConnectionString;
import com.boeing.jobstarter.model.TableMetadata;
import com.boeing.jobstarter.utils.Crypto;
import com.boeing.jobstarter.utils.Log;
import com.boeing.jobstarter.utils.XMLHandler;

@Component
public class MSSQLConnection implements IDBConnection {

    private Connection connection;


    private Log logger;

    private ConnectionString connectionParam;

    public MSSQLConnection() {

    }

    public void connect(ConnectionString connString) throws Exception {

        connectionParam = connString;
        if (connectionParam != null) {
            this.doConnect();
        }

    }

    private void doConnect() throws Exception {
        Class.forName(connectionParam.getDriver());

        String url = "jdbc:sqlserver://";

        url += connectionParam.getServerHost() != null && connectionParam.getServerHost().length() != 0 ? connectionParam
                .getServerHost()
                : "" + connectionParam.getServerPort() != null
                        && connectionParam.getServerPort().length() != 0 ? ":"
                        + connectionParam.getServerPort() : "" + ":"
                + ";databaseName=" + connectionParam.getDatabaseName();

        logger.getLogger().info(
                "Connecting to MSSQL Server Database [" + connectionParam.getDatabaseName() + "] using: "
                        + connectionParam.getConnectionName() + " With : " + url);

        this.setConnection(DriverManager.getConnection(url, connectionParam.getUser(), Crypto
                .decrypt(connectionParam.getPassword())));

        this.getConnection().setNetworkTimeout(Executors.newFixedThreadPool(1),
                XMLHandler.getConnectionTimeOut());

        logger.getLogger().info("Database connection established successfuly...");
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void disconnect() throws Exception {
        if (getConnection() != null) {
            this.getConnection().close();
            logger.getLogger().info("Disconnected from Database successfuly...");
        }
    }

    public boolean isConnected() throws SQLException {
        return (this.connection != null && !this.connection.isClosed());
    }

    public Log getLogger() {
        return logger;
    }

    public void setLogger(Log logger) {
        this.logger = logger;
    }

    public ConnectionString getConnectionParam() {
        return connectionParam;
    }

    public void setConnectionParam(ConnectionString connectionParam) {
        this.connectionParam = connectionParam;
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

    @Override
    public String buildDDL(List<TableMetadata> metadata, String tableName) throws Exception {
        StringBuilder ddl = new StringBuilder();
        ddl.append("CREATE TABLE ").append(tableName).append("(").append("\n");
        int colNum = 0;
        for (TableMetadata m : metadata) {

            ddl.append(m.getColumnName().replaceAll("[/@ ]", "_")).append(" ");

            ddl.append(mapToOracle(m));

            colNum++;
            if (colNum < metadata.size())
                ddl.append(", ").append("\n");
        }
        return ddl.append(")").toString();
    }

    private Object mapToOracle(TableMetadata metadata) throws Exception {
        switch (metadata.getColumnType().toUpperCase()) {
            case "UNIQUEIDENTIFIER":
                return "CHAR(36)";
            case "CHAR":
                return "CHAR";
            case "VARCHAR":
            case "VARCHAR2":
                return "VARCHAR2(" + metadata.getColumnSize() + ")";
            case "NCHAR":
            case "NVARCHAR":
                return "NCHAR";
            case "LONG":
                return "LONG";
            case "BIGINT":
                return "NUMBER(19)";
            case "BINARY":
            case "TIMESTAMP":
            case "VARBINARY":
                return "RAW";
            case "REAL":
                return "FLOAT(23)";
            case "FLOAT":
                return "FLOAT(49)";
            case "BIT":
            case "NUMBER":
            case "INT":
            case "DECIMAL":
            case "TINYINT":
            case "BYTEINT":
            case "SMALLINT":
            case "INTEGER":
            case "MONEY":
            case "NUMERIC":
            case "SMALL MONEY":
                return "NUMBER(" + metadata.getColumnSize() + "," + metadata.getColumnPrecision() + ")";
            case "SMALLDATETIME":
            case "DATETIME":
            case "DATE":
                return "DATE";
            case "NTEXT":
                return "CLOB";
            case "TEXT":
                return "LONG";
            case "IMAGE":
                return "LONG RAW";
            case "BLOB":
                return "BLOB";
            case "CLOB":
                return "CLOB";
            default:
                throw new Exception("Invalid or Unmapped SQL type (" + metadata.getColumnType() + ")");
        }
    }

}
