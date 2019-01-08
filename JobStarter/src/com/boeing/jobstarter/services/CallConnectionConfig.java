package com.boeing.jobstarter.services;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.boeing.jobstarter.MyAppContext;
import com.boeing.jobstarter.action.interfaces.ICaller;
import com.boeing.jobstarter.action.interfaces.IDBConnection;
import com.boeing.jobstarter.model.ConnectionString;
import com.boeing.jobstarter.model.Driver;
import com.boeing.jobstarter.model.LDAPServer;
import com.boeing.jobstarter.utils.Crypto;
import com.boeing.jobstarter.utils.Globals;
import com.boeing.jobstarter.utils.Globals.Answer;
import com.boeing.jobstarter.utils.Globals.AskType;
import com.boeing.jobstarter.utils.XMLHandler;


public class CallConnectionConfig implements ICaller {

    private ConnectionString connString;

    private IDBConnection connection;

    private MyAppContext myAppContext;

    protected HashMap<String, Object> parameters;

    private static final boolean required = true;

    public static List<LDAPServer> getAllServers() throws Exception {
        List<LDAPServer> ldapList = null;
        NodeList nl = XMLHandler.getAllNodesByTag(XMLHandler.SETTINGS_FILE, "server");

        if (nl != null && nl.getLength() > 0) {
            ldapList = new ArrayList<LDAPServer>();
            for (int temp = 0; temp < nl.getLength(); temp++) {
                Node nNode = nl.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) nNode;
                    LDAPServer newServer = new LDAPServer();
                    newServer.setServerAddress(e.getAttribute("id"));
                    newServer.setPort(e.getElementsByTagName("ldap-port").item(0).getTextContent());
                    newServer.setSslPort(e.getElementsByTagName("ldap-ssl-port").item(0).getTextContent());
                    newServer.setDc(e.getElementsByTagName("dc").item(0).getTextContent());
                    ldapList.add(newServer);
                }
            }
        }
        return ldapList;
    }

    public void updateConnectionParam(String connName) throws Exception {
        this.setConnString(getConnectionString(connName));
        if (this.getConnString() != null) {
            modifyConnectionParam(connName);
        } else {
            if (connName != null) {
                if (Globals.askConfirmation("Connection Parameter <" + connName
                        + "> doesn't exists. Do you you want to create it? (y/n): ", Globals.AskType.YN,
                        required) == Globals.Answer.Y) {
                    createConnectionParam(connName);
                }
            }
        }
    }

    public void printConnectionParam() throws Exception {
        List<ConnectionString> lcs = getAllConnectionString();
        if (!lcs.isEmpty()) {
            lcs.forEach(c -> {
                System.out.println("*********************************************");
                System.out.println("Connection Name: " + c.getConnectionName());
                System.out.println("---------------------------------------------");
                System.out.println("User    : " + c.getUser());
                System.out.println("Password: ********************");
                System.out.println("Database: " + c.getDatabaseName());
                System.out.println("Database Type: " + c.getDatabaseType());
                if (Answer.Y.toString().compareToIgnoreCase(c.getUseLDAPServer()) == 0) {
                    System.out.println("Connecting using LDAP Servers");
                } else {
                    System.out.println("Server Address: " + c.getServerHost());
                    System.out.println("Server Port   : " + c.getServerPort());
                }
                try {
                    System.out.println("Driver  : " + getDriver(c.getDatabaseType()).getDriver());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Default : " + c.getDeflt());
            });
            System.out.println("*********************************************");
        } else {
            if (Globals.askConfirmation(
                    "No connection parameters configured. Do you want to create one? (y/n): ", AskType.YN,
                    required) == Globals.Answer.Y) {
                createConnectionParam(null);
            }
        }
    }

    public void deleteConnectionParam(String connName) throws Exception {
        if (Globals.askConfirmation("Do you want to DELETE Connection <" + connName + "> (y/n): ",
                Globals.AskType.YN, required) == Globals.Answer.Y) {
            try {
                XMLHandler.DeleteConnectionString(connName);
            } catch (Exception e) {
                throw e;
            }
            this.setConnString(null);
        }
    }

    private void modifyConnectionParam(String connName) throws Exception {
        if (Globals.askConfirmation("Do you want to UPDATE Connection <" + connName + "> (y/n): ",
                Globals.AskType.YN, required) == Globals.Answer.Y) {
            // User
            this.getConnString().setUser(
                    (String) Globals.nvl(Globals.ask("Enter User [" + this.getConnString().getUser() + "]: ",
                            !required), this.getConnString().getUser()));
            // Password
            this.getConnString().setPassword(
                    (String) Globals.nvl(Crypto.encrypt(Globals.askPassword("Enter Password [***]: ",
                            !required)), this.getConnString().getPassword()));
            // Database Name
            this.getConnString().setDatabaseName(
                    (String) Globals.nvl(Globals.ask("Enter Database name ["
                            + this.getConnString().getDatabaseName() + "]: ", !required), this
                            .getConnString().getDatabaseName()));
            // Database type
            System.out.println(listDrivers());
            List<Driver> drivers = getAllDrivers();
            String opt = Globals.ask("Database type. Enter a number from the list, between [1.."
                    + drivers.size() + "]: ", !required);
            if (opt != null && Integer.valueOf(opt) <= drivers.size()) {
                this.getConnString().setDatabaseType(drivers.get(Integer.valueOf(opt) - 1).getDbType());
            }

            // Use LDAP servers
            if (Answer.Y.toString().compareToIgnoreCase(this.getConnString().getUseLDAPServer()) == 0) {
                if (Globals.askConfirmation(
                        "Currently using LDAP authentication. Do you want to change it? (y/n): ", AskType.YN,
                        required) == Answer.Y) {
                    this.getConnString().setServerHost(Globals.ask("Enter Server Address: ", !required));
                    this.getConnString().setServerPort(Globals.ask("Enter Server Port   : ", !required));
                    this.getConnString().setUseLDAPServer(Answer.N.toString());
                }
            } else {
                if (Globals.askConfirmation("Do you want to use LDAP authentication? (y/n): ", AskType.YN,
                        required) == Answer.Y) {
                    this.getConnString().setServerHost(null);
                    this.getConnString().setServerPort(null);
                    this.getConnString().setUseLDAPServer(Answer.Y.toString());
                } else {
                    this.getConnString().setServerHost(
                            (String) Globals.nvl(Globals.ask("Enter Server Address ["
                                    + this.getConnString().getServerHost() + "]: ", !required), this
                                    .getConnString().getServerHost()));
                    this.getConnString().setServerPort(
                            (String) Globals.nvl(Globals.ask("Enter Server Port ["
                                    + this.getConnString().getServerPort() + "]: ", !required), this
                                    .getConnString().getServerPort()));
                }

            }
            if (this.getConnString().getDeflt() != Globals.Answer.Y.toString()) {
                if (Globals.askConfirmation("Do you want this connection as default? (y/n): ",
                        Globals.AskType.YN, !required) == Globals.Answer.Y) {
                    this.getConnString().setDeflt(Globals.Answer.Y.toString());
                } else {
                    this.getConnString().setDeflt(Globals.Answer.N.toString());
                }
            }
            if (this.getConnString().getDeflt() == Globals.Answer.Y.toString()) {
                XMLHandler.clearDefault();
            }
            XMLHandler.DeleteConnectionString(connName);
            insertParameterXML();
        }
    }

    public void createConnectionParam(String connName) throws Exception {
        this.setConnString(new ConnectionString());
        if (connName == null || connName.length() == 0) {
            connName = Globals.ask("Enter a Name for this Connection: ", required);
        }

        this.getConnString().setConnectionName(connName);

        this.getConnString().setUser(Globals.ask("Enter User          : ", required));
        this.getConnString().setPassword(
                Crypto.encrypt(Globals.askPassword("Enter Password      : ", required)));
        this.getConnString().setDatabaseName(Globals.ask("Enter Database Name : ", required));
        System.out.println(listDrivers());
        List<Driver> drivers = getAllDrivers();
        int opt = 0;
        while (true) {
            opt = Integer.valueOf(Globals.ask("Database type. Enter a number from the list, between [1.."
                    + drivers.size() + "]: ", required));
            if (opt <= drivers.size()) {
                break;
            }
        }

        this.getConnString().setDatabaseType(drivers.get(opt - 1).getDbType());

        if (Globals.askConfirmation("Use LDAP Servers for this connection? (y/n): ", AskType.YN, required) == Answer.Y) {
            this.getConnString().setUseLDAPServer(Answer.Y.toString());
        } else {
            this.getConnString().setServerHost(Globals.ask("Enter Server Address: ", !required));
            this.getConnString().setServerPort(Globals.ask("Enter Server Port   : ", !required));
            this.getConnString().setUseLDAPServer(Answer.N.toString());
        }

        if (XMLHandler.countConnectionString() == 0) {
            this.getConnString().setDeflt(Globals.Answer.Y.toString());
        } else if (Globals.askConfirmation("Do you want this connection as default? (y/n): ",
                Globals.AskType.YN, !required) == Globals.Answer.Y) {
            this.getConnString().setDeflt(Globals.Answer.Y.toString());
        } else {
            this.getConnString().setDeflt(Globals.Answer.N.toString());
        }
        if (this.getConnString().getDeflt() == Globals.Answer.Y.toString()) {
            XMLHandler.clearDefault();
        }
        insertParameterXML();
    }

    public static ConnectionString getConnectionString(String connectionName) throws Exception {
        return fillConnectionString(XMLHandler.getConnectionString(connectionName));
    }

    public static ConnectionString getConnectionString() throws Exception {
        return fillConnectionString(XMLHandler.getConnectionString());
    }

    public static List<ConnectionString> getAllConnectionString() throws Exception {
        NodeList nl = XMLHandler.getAllNodesByTag(XMLHandler.SETTINGS_FILE, "connection");
        List<ConnectionString> lcs = new ArrayList<ConnectionString>();
        if (nl != null && nl.getLength() > 0) {
            for (int temp = 0; temp < nl.getLength(); temp++) {
                lcs.add(fillConnectionString((Element) nl.item(temp)));
            }
        }
        return lcs;
    }

    private static ConnectionString fillConnectionString(Element e) throws Exception {
        ConnectionString cs = null;

        if (e != null) {
            cs = new ConnectionString();
            cs.setConnectionName(e.getAttribute("id"));
            cs.setUser(e.getElementsByTagName("user").item(0).getTextContent());
            cs.setPassword(e.getElementsByTagName("password").item(0).getTextContent());
            cs.setDatabaseName(e.getElementsByTagName("database-name").item(0).getTextContent());
            cs.setDatabaseType(e.getElementsByTagName("database-type").item(0).getTextContent());
            cs.setServerHost(e.getElementsByTagName("server-host").item(0).getTextContent());
            cs.setServerPort(e.getElementsByTagName("server-port").item(0).getTextContent());
            cs.setDriver(getDriver(cs.getDatabaseType()).getDriver());
            cs.setClassName(getDriver(cs.getDatabaseType()).getClassName());
            cs.setUseLDAPServer(e.getElementsByTagName("use-ldap").item(0).getTextContent());
            cs.setDeflt(e.getAttribute("default"));

        }
        return cs;
    }

    public static Driver getDriver(String databaseType) throws Exception {
        List<Driver> drivers = getAllDrivers();
        return drivers.stream().filter(p -> (p.getDbType().compareToIgnoreCase(databaseType) == 0))
                .findFirst().orElse(null);
    }

    public static List<Driver> getAllDrivers() throws Exception {
        List<Driver> drivers = new ArrayList<Driver>();
        NodeList nl = XMLHandler.getAllNodesByTag(XMLHandler.SETTINGS_FILE, "db-type");
        if (nl != null && nl.getLength() > 0) {
            for (int temp = 0; temp < nl.getLength(); temp++) {
                Node node = nl.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    drivers.add(new Driver(((Element) node).getAttribute("id"), ((Element) node)
                            .getAttribute("driver"), ((Element) node).getAttribute("class")));
                }
            }
        }
        return drivers;
    }

    public static String listDrivers() throws Exception {
        StringBuilder sb = new StringBuilder();
        List<Driver> list = getAllDrivers();
        for (int i = 1; i <= list.size(); i++) {
            sb.append(i + ". ").append(list.get(i - 1).getDbType()).append("[").append(
                    list.get(i - 1).getDriver()).append("]").append("\n");
        }
        return sb.toString();
    }

    private void insertParameterXML() throws Exception {

        Document xmlDoc = XMLHandler.getXMLDocument(XMLHandler.SETTINGS_FILE);

        Element node = xmlDoc.createElement("connection");
        Element element;

        node.setAttribute("id", this.getConnString().getConnectionName());
        node.setAttribute("default", this.getConnString().getDeflt());

        element = xmlDoc.createElement("user");
        element.setNodeValue(this.getConnString().getUser());
        node.appendChild(element);

        element = xmlDoc.createElement("password");
        element.setNodeValue(this.getConnString().getPassword());
        node.appendChild(element);

        element = xmlDoc.createElement("database-name");
        element.setNodeValue(this.getConnString().getDatabaseName());
        node.appendChild(element);

        element = xmlDoc.createElement("database-type");
        element.setNodeValue(this.getConnString().getDatabaseType());
        node.appendChild(element);

        element = xmlDoc.createElement("server-host");
        element.setNodeValue(this.getConnString().getServerHost());
        node.appendChild(element);

        element = xmlDoc.createElement("server-port");
        element.setNodeValue(this.getConnString().getServerPort());
        node.appendChild(element);

        element = xmlDoc.createElement("use-ldap");
        element.setNodeValue(this.getConnString().getUseLDAPServer());
        node.appendChild(element);

        XMLHandler.addNodeToParent("connection-string", node);
    }

    public ConnectionString getConnString() {
        return this.connString;
    }

    public void setConnString(ConnectionString connString) {
        this.connString = connString;
    }

    @Override
    public void execute() throws Exception {

        if (parameters.get("LIST") != null) {
            printConnectionParam();
        } else if (parameters.get("MODIFY") != null) {
            updateConnectionParam(String.valueOf(parameters.get("MODIFY")));
        } else if (parameters.get("ADD") != null) {
            createConnectionParam(null);
        } else if (parameters.get("DELETE") != null) {
            deleteConnectionParam(String.valueOf(parameters.get("DELETE")));
        } else if (parameters.get("TEST") != null) {
            testConnection(String.valueOf(parameters.get("TEST")));
        }
    }

    private void testConnection(String connName) {
        ConnectionString cs = null;
        try {
            cs = getConnectionString(connName);
            if (cs == null) {
                System.out.println("None Connection parameter were found with name: " + connName);
                return;
            }
            this.connection = myAppContext.getApplicationContext().getBean(cs.getClassName(),
                    IDBConnection.class);
            this.connection.connect(cs);
            if (connection.isConnected()) {
                System.out.println("Connected to Database: " + cs.getDatabaseName() + " using : "
                        + cs.getConnectionName());
            } else {
                System.out.println("Connection to Database: " + cs.getDatabaseName() + " using : "
                        + cs.getConnectionName() + "  Failed");
            }
        } catch (Exception e) {
            System.out.println("Connection to Database: " + cs.getDatabaseName() + " using : "
                    + cs.getConnectionName() + "  Failed [" + e.getMessage() + "]");
        }
    }

    @Override
    public void initCaller(HashMap<String, Object> parameters) throws Exception {
        this.parameters = parameters;
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
