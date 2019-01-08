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
public class DB2Connection implements IDBConnection {

    private Connection connection;

    private Log logger;

    private ConnectionString connectionParam;

    public DB2Connection() {

    }

    public final void connect(ConnectionString connString) throws Exception {

        connectionParam = connString;
        if (connectionParam != null) {
            this.doConnect();
        }
    }

    private void doConnect() throws Exception {
        Class.forName(connectionParam.getDriver());

        String url = "jdbc:db2:";

        url += connectionParam.getServerHost() != null && connectionParam.getServerHost().length() != 0 ? connectionParam
                .getServerHost()
                : "" + connectionParam.getServerPort() != null
                        && connectionParam.getServerPort().length() != 0 ? ":"
                        + connectionParam.getServerPort() : "" + ":"
                + connectionParam.getDatabaseName();

        logger.getLogger().info(
                "Connecting to BD2 Database [" + connectionParam.getDatabaseName() + "] using: "
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
        // TODO Auto-generated method stub
        return null;
    }
}
