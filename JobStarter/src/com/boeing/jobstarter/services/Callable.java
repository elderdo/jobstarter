package com.boeing.jobstarter.services;

import java.io.File;
import java.nio.file.Files;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.boeing.jobstarter.MyAppContext;
import com.boeing.jobstarter.action.interfaces.ICaller;
import com.boeing.jobstarter.action.interfaces.IDBConnection;
import com.boeing.jobstarter.model.ConnectionString;
import com.boeing.jobstarter.model.ProcedureMetadata;
import com.boeing.jobstarter.model.StatementParam;
import com.boeing.jobstarter.model.TableMetadata;
import com.boeing.jobstarter.utils.CustomException;
import com.boeing.jobstarter.utils.FileUtils;
import com.boeing.jobstarter.utils.FileWriterUtil;
import com.boeing.jobstarter.utils.Globals;
import com.boeing.jobstarter.utils.Log;
import com.boeing.jobstarter.utils.MailUtils;
import com.boeing.jobstarter.utils.MapTypes;
import com.boeing.jobstarter.utils.XMLHandler;
import com.boeing.jobstarter.utils.ZipUtils;

public abstract class Callable implements ICaller {

    protected IDBConnection connection;

    protected MyAppContext myAppContext;

    protected Log logger;

    protected FileWriterUtil fileWriter;

    protected HashMap<String, Object> parameters;

    public static List<TableMetadata> getTableMetadata(IDBConnection connection, String catalogName,
            String schemaName, String tableName)
            throws SQLException {
        List<TableMetadata> metadata = new ArrayList<TableMetadata>();
        DatabaseMetaData dbMetaData = connection.getConnection().getMetaData();
        ResultSet rs = dbMetaData.getColumns(catalogName, schemaName, tableName, null);
        TableMetadata m = null;
        while (rs.next()) {
            m = new TableMetadata();
            m.setColumnName(rs.getString("COLUMN_NAME"));
            m.setColumnType(rs.getString("TYPE_NAME"));
            m.setColumnSize(rs.getInt("COLUMN_SIZE"));
            m.setColumnPrecision(rs.getInt("DECIMAL_DIGITS"));
            metadata.add(m);
        }
        return metadata;
    }

    public static List<ProcedureMetadata> getProcedureMetadata(IDBConnection connection, String catalogName,
            String schemaName,
            String procedureName, String columnName) throws SQLException {
        List<ProcedureMetadata> metadata = new ArrayList<ProcedureMetadata>();
        DatabaseMetaData dbMetaData = connection.getConnection().getMetaData();
        ResultSet rs = dbMetaData.getProcedureColumns(catalogName, schemaName, procedureName, columnName);
        int pos = 0;
        ProcedureMetadata m = null;
        while (rs.next()) {
            m = new ProcedureMetadata();
            m.setColumnPosition(pos);
            m.setColumnName(rs.getString("COLUMN_NAME"));
            m.setColumnDataType(rs.getInt("DATA_TYPE"));
            m.setColumnReturn(rs.getShort("COLUMN_TYPE"));
            m.setColumnByteLength(rs.getInt("LENGTH"));
            m.setColumnPrecision(rs.getInt("PRECISION"));
            m.setColumnNullable(rs.getShort("NULLABLE"));
            metadata.add(m);
            pos++;
        }
        return metadata;
    }

    public static TableMetadata getMetadataByName(String fieldName, List<TableMetadata> metadata) {
        return metadata.stream().filter(m -> m.getColumnName().compareToIgnoreCase(fieldName) == 0).findAny()
                .orElse(null);
    }

    public static String BuildObjetQuery(StatementParam param) {
        StringBuilder sb = new StringBuilder();
        if (param.getAlias() != null) {
            sb.append(param.getAlias()).append(".");
        }
        if (param.getColumnName() != null) {
            sb.append(param.getColumnName()).append(".");
        }
        if (param.getValue() != null) {
            sb.append(param.getValue());
        }

        return sb.toString();
    }

    public static String prepareStatement(String tableName, List<TableMetadata> metadata,
            List<StatementParam> parameters) throws Exception {
        String where = "";
        if (parameters != null) {
            for (StatementParam p : parameters) {
                if (where != "") {
                    where += " \n AND ";
                }
                TableMetadata m = getMetadataByName(p.getColumnName(), metadata);
                if (m != null) {
                    if (MapTypes.map(MapTypes.map(m.getColumnType())).getName().equalsIgnoreCase(
                            java.lang.Boolean.class.getName())) {
                        where += tableName + "." + m.getColumnName() + p.getOperation()
                                + " case ? when 1 then true else false end";
                    } else {
                        where += tableName + "." + m.getColumnName() + " " + p.getOperation() + " ? ";
                    }
                }
            }
        }
        return "SELECT * FROM " + tableName + (where.length() > 0 ? " WHERE " + where : "");
    }

    public static String prepareStatement(String procedureName, List<ProcedureMetadata> metadata)
            throws Exception {
        String stmt = "";
        for (ProcedureMetadata m : metadata) {
            if (stmt.length() != 0) {
                stmt += ",";
            }
            if (MapTypes.map(m.getColumnDataType()).getName().equalsIgnoreCase(
                    java.lang.Boolean.class.getName())) {
                stmt += m.getColumnName() + " => case ? when 1 then true else false end";
            } else {
                stmt += m.getColumnName() + " => ? ";
            }
        }

        return "{call " + procedureName + "(" + stmt + ")}";
    }

    public static List<StatementParam> getListParameters(String params) throws Exception {
        List<StatementParam> list = new ArrayList<StatementParam>();
        int idx = 0;
        if (params != null) {
            for (String p : params.split(Globals.PARAM_SPLITER)) {
                String[] param = p.split("([.]|(<>)|(!=)|(=)|-|\\+|%)");
                StatementParam stmtParam = new StatementParam();
                stmtParam.setPosition(idx);
                stmtParam.setValue(param[param.length - 1].trim());
                stmtParam.setColumnName(param.length > 1 ? param[param.length - 2].replace('[', ' ').replace(
                        ']', ' ').trim() : null);
                stmtParam.setAlias(param.length > 2 ? param[param.length - 3].replace('[', ' ').replace(']',
                        ' ').trim() : null);
                stmtParam.setFile(param.length > 3 ? param[param.length - 4].replace('[', ' ').replace(']',
                        ' ').trim() : null);
                if (p.contains("<>")) {
                    stmtParam.setOperation("<>");
                } else if (p.contains("-")) {
                    stmtParam.setOperation("<");
                } else if (p.contains("+")) {
                    stmtParam.setOperation(">");
                } else if (p.contains("!=")) {
                    stmtParam.setOperation("!=");
                } else if (p.contains("=")) {
                    stmtParam.setOperation("=");
                } else if (p.contains("%")) {
                    stmtParam.setOperation("like");
                } else {
                    stmtParam.setOperation(null);
                }
                list.add(stmtParam);
                idx++;
            }
        }
        return list;
    }

    public static List<StatementParam> getListBindVariables(String query, List<StatementParam> params,
            String fileName)
            throws Exception {
        List<StatementParam> list = new ArrayList<StatementParam>();
        int idx = 1;
        Pattern pattern = Pattern
                .compile("(\\w+\\.\\w+[\\s=<>like!]*\\?)|([\\w+\\.](\\w+[\\s=<>like!]*)\\?)");
        Matcher m = pattern.matcher(query);
        while (m.find()) {
            List<StatementParam> stmtParam = getListParameters(m.group(0));
            if (!stmtParam.isEmpty()) {
                stmtParam.get(0).setPosition(idx);

                StatementParam p = getParameterByFileAliasColumn(fileName.contains(".") ? fileName.substring(
                        0, fileName.lastIndexOf(".")) : fileName, stmtParam.get(0)
                        .getAlias(), stmtParam.get(0).getColumnName(), params);
                if (p == null) {
                    p = getParameterByFileAliasColumn(null, stmtParam.get(0).getAlias(), stmtParam.get(0)
                            .getColumnName(), params);
                }
                if (p == null) {
                    p = getParameterByFileAliasColumn(null, null, stmtParam.get(0).getColumnName(), params);
                }
                if (p == null) {
                    throw new Exception("No value provided for column: " + stmtParam.get(0).getAlias() + "."
                            + stmtParam.get(0).getColumnName());
                }
                stmtParam.get(0).setValue(p.getValue());
                list.add(stmtParam.get(0));
            }
        }
        return list;
    }

    public static List<File> getFilesByPattern(File[] files, List<StatementParam> patterns) {
        List<File> matchedFiles = new ArrayList<File>();
        patterns.stream().forEach(p -> {
            matchedFiles.addAll(FileUtils.getFilesByPattern(files, p.getColumnName()));
        });
        return matchedFiles;
    }

    public static String getFileDestination(File file, List<StatementParam> patterns) {
        for (StatementParam p : patterns) {
            Pattern pattern = Pattern.compile("^" + p.getColumnName().replace("*", "(.*)") + "$");
            if (pattern.matcher(file.getName()).find()) {
                return p.getValue();
            }
        }

        return null;
    }

    public static List<StatementParam> getParametersByNoName(List<StatementParam> params) {
        return params.stream().filter(p -> p.getColumnName() == null).collect(Collectors.toList());
    }

    public static List<StatementParam> getParametersByAlias(String aliasName, List<StatementParam> params) {
        List<StatementParam> list = (List<StatementParam>) params.stream().filter(
                p -> (p.getAlias() != null && p.getAlias().equalsIgnoreCase(aliasName))).collect(
                Collectors.toList());
        return list;
    }

    public static List<StatementParam> getParametersByNoAlias(List<StatementParam> params) {
        return params.stream().filter(p -> p.getAlias() == null).collect(Collectors.toList());
    }

    public static List<StatementParam> getParametersByFile(String fileName, List<StatementParam> params) {
        List<StatementParam> list = (List<StatementParam>) params.stream().filter(
                p -> (p.getFile() != null && p.getFile().equalsIgnoreCase(fileName))).collect(
                Collectors.toList());
        return list;
    }

    public static List<StatementParam> getParametersByNoFile(List<StatementParam> params) {
        return params.stream().filter(p -> p.getFile() == null).collect(Collectors.toList());
    }

    public static StatementParam getParameterByFileAliasColumn(String fileName, String aliasName,
            String columnName,
            List<StatementParam> params) {
        StatementParam param = (StatementParam) params.stream().filter(
                p -> ((p.getFile() == null && fileName == null) || (p.getFile() != null && p.getFile()
                        .equalsIgnoreCase(fileName)))).filter(
                p -> ((p.getAlias() == null && aliasName == null) || (p.getAlias() != null && p.getAlias()
                        .equalsIgnoreCase(aliasName)))).filter(
                p -> ((p.getColumnName() == null && columnName == null) || (p.getColumnName() != null && p
                        .getColumnName().equalsIgnoreCase(columnName))))
                .findFirst().orElse(null);
        return param;
    }

    public static List<StatementParam> getParametersByFileAliasColumn(String fileName, String aliasName,
            String columnName, List<StatementParam> params) {
        List<StatementParam> parameters = (List<StatementParam>) params.stream().filter(
                p -> ((p.getFile() == null && fileName == null) || (p.getFile() != null && p.getFile()
                        .equalsIgnoreCase(fileName)))).filter(
                p -> ((p.getAlias() == null && aliasName == null) || (p.getAlias() != null && p.getAlias()
                        .equalsIgnoreCase(aliasName)))).filter(
                p -> ((p.getColumnName() == null && columnName == null) || (p.getColumnName() != null && p
                        .getColumnName().equalsIgnoreCase(columnName)))).collect(Collectors.toList());
        return parameters;
    }

    public static StatementParam getParameterByPosition(Integer position, List<StatementParam> params) {
        return (StatementParam) params.stream().filter(p -> p.getPosition() == position).map(
                p -> (StatementParam) p);
    }

    private void showParameters() {

        StringBuilder sb = new StringBuilder();
        this.parameters.forEach((k, v) -> {
            sb.append(k).append(" = [").append(String.valueOf(v)).append("]\n");
        });
        logger.getLogger().info(
                "Executing: " + getClass().getSimpleName() + " with following parameter(s): \n"
                        + sb.toString());
    }

    @Override
    public void initCaller(HashMap<String, Object> parameters) throws Exception {
        this.parameters = parameters;
        this.logger = myAppContext.getApplicationContext().getBean(Log.class);
        showParameters();
        if (parameters.get("CONNECTION") != null) {

            ConnectionString cs = GetConnectionString(String.valueOf(parameters.get("CONNECTION")));
            this.connection = myAppContext.getApplicationContext().getBean(cs.getClassName(),
                    IDBConnection.class);

            this.fileWriter = myAppContext.getApplicationContext().getBean(FileWriterUtil.class);
            this.connection.connect(cs);
        }
    }

    @Override
    public void terminateCaller() throws Exception {

        if (connection.getConnection() != null && connection.isConnected()) {
            connection.disconnect();
        }
        logger.close();
    }

    @Override
    public void errorHandler(Exception ex) throws Exception {
        if (fileWriter != null) {
            fileWriter.closeWriter();
        }
        logger.getLogger().severe(ex.getMessage());
        logger.getLogger().severe(Globals.stackTraceToString(ex));
        logger.close();
        if (parameters.get("REPORT") != null
                && (String.valueOf(parameters.get("REPORT")).equalsIgnoreCase("BOTH") || String.valueOf(
                        parameters.get("REPORT")).equalsIgnoreCase("ERROR"))) {
            if (ex.getClass().equals(CustomException.class)) {
                if (((CustomException) ex).getType() == CustomException.Types.SQL_LOADER) {
                    report("Error... ", logger.getLogFile(), ((CustomException) ex).getFiles());
                }
            } else {
                report("Error... ", logger.getLogFile());
            }
        }
    }

    private void copyLogFile(File file) throws Exception {
        String copyDir = "";
        if (parameters.get("COPY") != null) {
            copyDir = String.valueOf(parameters.get("COPY"));

        } else if (parameters.get("MOVE") != null) {
            copyDir = String.valueOf(parameters.get("MOVE"));
        } else {
            copyDir = XMLHandler.getDirectories(Globals.TagNames.OUTPUT_DIR_TAG);
        }
        File newFile = new File(copyDir
                + (copyDir.endsWith(FileUtils.fileSeparator()) ? "" : FileUtils.fileSeparator())
                + file.getName() + ".error");
        FileUtils.Copy(file, newFile);
    }

    public void report(String message, File file) throws Exception {
        if (parameters.get("REPORTBY") != null) {
            String[] reportOptions = String.valueOf(parameters.get("REPORTBY")).split(
                    Globals.PARAM_SPLITER);
            for (int i = 0; i < reportOptions.length; i++) {
                switch (reportOptions[i].toUpperCase()) {
                    case "MAIL":
                        String subject= null;
                        if (parameters.get("JOBNAME") != null) {
                            subject = message + String.valueOf(parameters.get("JOBNAME"));
                        } else {
                            subject = message + file.getName();
                        }
                        MailUtils.sendEmail(subject, file);
                        break;
                    case "COPY":
                        copyLogFile(file);
                        break;
                    case "DATABASE":
                        // TODO Insert result into oracle database SDSDWHOME schema
                        break;
                }
            }
        }
    }

    public void report(String message, File logFile, File[] files) throws Exception {
        if (parameters.get("REPORTBY") != null) {
            String[] reportOptions = String.valueOf(parameters.get("REPORTBY")).split(Globals.PARAM_SPLITER);
            for (int i = 0; i < reportOptions.length; i++) {
                switch (reportOptions[i].toUpperCase()) {
                    case "MAIL":
                        String subject = null;
                        if (parameters.get("JOBNAME") != null) {
                            subject = message + String.valueOf(parameters.get("JOBNAME"));
                        } else {
                            subject = message + logFile.getName();
                        }
                        MailUtils.sendEmail(subject, logFile, files);
                        break;
                    case "COPY":
                        copyLogFile(logFile);
                        break;
                    case "DATABASE":
                        // TODO Insert result into oracle database SDSDWHOME schema
                        break;
                }
            }
        }
    }

    public static ConnectionString GetConnectionString(String connName) throws Exception {
        ConnectionString cs = new ConnectionString();
        if (connName != null && connName.length() > 0) {
            cs = CallConnectionConfig.getConnectionString(connName);
            if (cs == null) {
                throw new Exception("No conection configured with name: " + connName);
            }
        } else {
            cs = CallConnectionConfig.getConnectionString();
            if (cs == null) {
                throw new Exception("No default conection configured");
            }
        }
        return cs;
    }

    public void afterExecute(File file) throws Exception {
        File f = file;
        if (parameters.get("ZIP") != null) {
            f = ZipUtils.ZipIt(file);
            Files.deleteIfExists(file.toPath());
        }
        if (parameters.get("COPY") != null) {
            FileUtils.Copy(f, new File(String.valueOf(parameters.get("COPY"))));
        }
        if (parameters.get("ARCHIVE") != null) {

            if (parameters.get("ZIP") == null) {
                f = ZipUtils.ZipIt(file);
                FileUtils.MoveToArchive(f, connection.getConnectionParam().getConnectionName());
            } else {
                FileUtils.CopyToArchive(f, connection.getConnectionParam().getConnectionName());
            }
            logger.getLogger().info("File(s) archived");
        }
        if (parameters.get("MOVE") != null) {
            FileUtils.Move(f, new File(String.valueOf(parameters.get("MOVE"))));
        }
    }

    public Log getLogger() {
        return logger;
    }

    public void setLogger(Log logger) {
        this.logger = logger;
    }

    public FileWriterUtil getFileWriter() {
        return fileWriter;
    }

    public void setFileWriter(FileWriterUtil fileWriter) {
        this.fileWriter = fileWriter;
    }

    public IDBConnection getConnection() {
        return connection;
    }

    public void setConnection(IDBConnection connection) {
        this.connection = connection;
    }

    public MyAppContext getMyAppContext() {
        return myAppContext;
    }

    public void setMyAppContext(MyAppContext myAppContext) {
        this.myAppContext = myAppContext;
    }

}
