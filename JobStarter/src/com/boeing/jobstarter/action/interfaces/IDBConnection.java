package com.boeing.jobstarter.action.interfaces;

import java.sql.Connection;
import java.util.List;

import com.boeing.jobstarter.model.ConnectionString;
import com.boeing.jobstarter.model.TableMetadata;

public interface IDBConnection {

    public void connect(ConnectionString connString) throws Exception;

    public void disconnect() throws Exception;

    public Connection getConnection();

    public ConnectionString getConnectionParam();

    public boolean isConnected() throws Exception;

    public void close(Object obj) throws Exception;

    public void showDBInternalLog() throws Exception;

    public String buildDDL(List<TableMetadata> metadata, String tableName) throws Exception;
}
