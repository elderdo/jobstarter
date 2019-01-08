package com.boeing.jobstarter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionString {

    private String connectionName;

    private String user;

    private String password;

    private String databaseName;

    private String serverHost;

    private String serverPort;

    private String driver;

    private String deflt;

    private String useLDAPServer;

    private String databaseType;

    private String className;

}