package com.boeing.jobstarter.connection;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boeing.jobstarter.action.interfaces.IDBConnection;
import com.boeing.jobstarter.model.ConnectionString;
import com.boeing.jobstarter.model.LDAPServer;
import com.boeing.jobstarter.model.TableMetadata;
import com.boeing.jobstarter.services.CallConnectionConfig;
import com.boeing.jobstarter.utils.Crypto;
import com.boeing.jobstarter.utils.Globals;
import com.boeing.jobstarter.utils.Globals.Answer;
import com.boeing.jobstarter.utils.Log;

@Component
public class OrclConnection implements IDBConnection {

    private Connection connection;

    @Autowired
    private Log logger;

    private ConnectionString connectionParam;

    public OrclConnection() {

    }

    public void connect(ConnectionString connString) throws Exception {

        connectionParam = connString;
        if (connectionParam != null) {
            this.doConnect();
        }
        enable(Globals.ORACLE_BUFFER_SIZE);
    }

    private void doConnect() throws Exception {
        

        String url = "jdbc:oracle:thin:@";
        if (Answer.Y.toString().compareToIgnoreCase(connectionParam.getUseLDAPServer()) == 0) {
            List<LDAPServer> servers = (List<LDAPServer>) CallConnectionConfig.getAllServers();

            for (LDAPServer server : servers) {
                if (server.getPort() != null || server.getPort().length() > 0) {
                    url += "ldap://" + server.getServerAddress() + ":" + server.getPort() + "/"
                            + connectionParam.getDatabaseName() + ",cn=OracleContext,dc=" + server.getDc()
                            + " \n";
                }
                if (server.getSslPort() != null || server.getSslPort().length() > 0) {
                    url += "ldaps://" + server.getServerAddress() + ":" + server.getSslPort() + "/"
                            + connectionParam.getDatabaseName() + ",cn=OracleContext,dc=" + server.getDc()
                            + " \n";
                }
            }
            logger.getLogger().info(
                    "Connecting to Oracle Database [" + connectionParam.getDatabaseName() + "] using: "
                            + connectionParam.getConnectionName() + " With LDAP Servers");
        } else {

            url += connectionParam.getServerHost() + ":" + connectionParam.getServerPort() + ":"
                    + connectionParam.getDatabaseName();

            logger.getLogger().info(
                    "Connecting to Oracle Database [" + connectionParam.getDatabaseName() + "] using: "
                            + connectionParam.getConnectionName() + " With : " + url);
        }
        Class.forName(connectionParam.getDriver());
        this.setConnection(DriverManager.getConnection(url, connectionParam.getUser(), Crypto
                .decrypt(connectionParam.getPassword())));


        /*
         * this.getConnection().setNetworkTimeout(Executors.newFixedThreadPool(1),
         * XMLHandler.getConnectionTimeOut());
         */
        logger.getLogger().info("Database connection established successfuly...");
    }

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
        return (this.getConnection() != null && !this.getConnection().isClosed());
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
    public void showDBInternalLog() throws Exception {
        if (isConnected()) {
            try (CallableStatement show_stmt = (this.getConnection()).prepareCall("declare "
                    + "  num integer := 1000;" + "begin " + "  dbms_output.get_lines(?, num);" + "end;")) {
                show_stmt.registerOutParameter(1, Types.ARRAY, "DBMSOUTPUT_LINESARRAY");
                show_stmt.execute();

                Array array = null;
                try {
                    array = show_stmt.getArray(1);

                    for (int i = 0; i < Stream.of((Object[]) array.getArray()).count() - 1; i++) {
                        logger.getLogger().info(((Object[]) array.getArray())[i].toString());
                    }
                } catch (Exception ex) {
                    throw ex;
                } finally {
                    if (array != null) {
                        array.free();
                    }
                    disable();

                }
            }
        }
    }

    private void enable(int size) throws Exception {

        CallableStatement enable_stmt = (this.getConnection())
                .prepareCall("begin dbms_output.enable(:1); end;");
        enable_stmt.setInt(1, size);
        enable_stmt.executeUpdate();
    }

    /*
     * disable only has to execute the dbms_output.disable call
     */
    private void disable() throws Exception {
        CallableStatement disable_stmt = (this.getConnection())
                .prepareCall("begin dbms_output.disable; end;");
        disable_stmt.executeUpdate();
    }

    @Override
    public String buildDDL(List<TableMetadata> metadata, String tableName) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
