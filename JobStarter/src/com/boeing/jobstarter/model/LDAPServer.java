package com.boeing.jobstarter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LDAPServer {

    private String serverAddress;

    private String port;

    private String sslPort;

    private String dc;

}
