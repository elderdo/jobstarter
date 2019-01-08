package com.boeing.jobstarter.services;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.boeing.jobstarter.MyAppContext;
import com.boeing.jobstarter.model.StatementParam;
import com.boeing.jobstarter.model.TableMetadata;
import com.boeing.jobstarter.utils.Crypto;
import com.boeing.jobstarter.utils.CustomException;
import com.boeing.jobstarter.utils.FileUtils;
import com.boeing.jobstarter.utils.Globals;
import com.boeing.jobstarter.utils.XMLHandler;
import com.boeing.jobstarter.utils.ZipUtils;

/**
 * Oracle SQL*Loader wrapper for Java. SQL*Loader is Oracle's bulk load utility. This class provides
 * a simple Java wrapper around the tool to hide the implementation details of creating a control
 * file.
 * <p/>
 * See: http://www.oracle.com/technetwork/testcontent/sql-loader-overview-095816.html
 */

public class CallSqlLoader extends Callable {

    public CallSqlLoader(MyAppContext context) {
        myAppContext = context;
    }

    /**
     * SQL*Loader process exit code. Operating-system independent. The process itself returns an
     * operating-system specific exit code, so we normalize it.
     * <p/>
     * See: https://docs.oracle.com/cd/B19306_01/server.102/b14215/ldr_params.htm#i1005019
     */
    public enum ExitCode {
        SUCCESS, FAIL, WARN, FATAL, UNKNOWN
    }

    /**
     * Return value from our high level API method. It contains SQL*Loader process exit code File
     * objects referring generated files. This class is immutable, so no need for getters and
     * setters.
     */
    public static class Results {

        public final ExitCode exitCode;

        public final File controlFile;

        public final File logFile;

        public final File badFile;

        public final File discardFile;

        public Results(ExitCode exitCode, File controlFile, File logFile, File badFile, File discardFile) {
            this.exitCode = exitCode;
            this.controlFile = controlFile;
            this.logFile = logFile;
            this.badFile = badFile;
            this.discardFile = discardFile;
        }
    }

    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    /**
     * Helper method. Get list of table columns, to be inserted in control file. TSV data file must
     * match this column order.
     */
    public List<String> getTableColumnsToString(List<TableMetadata> metadata) {
        List<String> ret = new ArrayList<>();
        for (TableMetadata m : metadata) {
            ret.add(m.getColumnName() + " " + getColumnTypeConvertion(m));
        }
        if (parameters.get("ADDLOADDATE") != null) {
            ret.add(String.valueOf(parameters.get("ADDLOADDATE")) + " \"sysdate\"");
        }
        return ret;
    }

    private String getColumnTypeConvertion(TableMetadata m) {
        String defaultDateFormat = "'YYYY-MM-DD HH24:MI:SS'";
        switch (m.getColumnType()) {
            case "DATE":
                String dateFormat = parameters.get("DATEFORMAT") != null ? "'"
                        + String.valueOf(parameters.get("DATEFORMAT")) + "'" : defaultDateFormat;
                return "\"TO_DATE(SUBSTR(:" + m.getColumnName() + ", 1, 19), " + dateFormat + ")\"";
            case "BLOB":
            case "CLOB":
                return " Char(10000000)";
            case "CHAR":
            case "VARCHAR":
            case "VARCHAR2":
                return " Char(" + m.getColumnSize() + ")";
            default:
                return "";
        }
    }
    
    private String calculateSkip() {
    	if (parameters.get("NOHEADER") == null && parameters.get("SKIP") == null) {
    		return "SKIP=1, ";
    	} else if (parameters.get("SKIP") != null) {
    		return "SKIP=" + Integer.parseInt(String.valueOf(parameters.get("SKIP"))) + ", ";
    	} else {
    		return "";
    	}
    }
    
    /**
     * Helper method. Generate intermediate control file.
     * 
     * @throws Exception
     */
    public String createControlFileComma(String dataFileName, String badFileName, String discardFileName,
            String tableName, List<String> columnNames, File file) throws Exception {
        String columns = null;
        if (parameters.get("USEHEADER") == null) {
            columns = columnNames.toString().replace("[", "( ").replace("]", " )\n");
        } else {
            columns = "(" + FileUtils.ReadHeader(file).replace(" ", "");
            if (parameters.get("ADDLOADDATE") != null) {
                columns += "," + (String.valueOf(parameters.get("ADDLOADDATE")) + " \"sysdate\"");
            }
            columns += ")";
        }
        return "options ("
                + calculateSkip()
                + "STORAGE=(INITIAL 100M NEXT 100M PCTINCREASE 0), DIRECT=TRUE, PARALLEL=TRUE, ERRORS=0, STREAMSIZE=100000000)\n"
                + "load data infile '" + dataFileName + "'\n" + "badfile '" + badFileName + "'\n"
                + "discardfile '" + discardFileName + "'\n" + "append " + "into table " + tableName + "\n"
                + "fields terminated by ',' optionally enclosed by '\"' TRAILING NULLCOLS" + "\n" + columns;
    }

    /**
     * Run SQL*Loader process.
     */
    public ExitCode runSqlLdrProcess(File initialDir, String stdoutLogFile, String stderrLogFile,
            String controlFile, String logFile)
            throws Exception {
        ProcessBuilder pb = new ProcessBuilder("sqlldr", "control=" + controlFile, "log=" + logFile,
                "userid=" + connection.getConnectionParam().getUser() + "/"
                        + Crypto.decrypt(connection.getConnectionParam().getPassword()) + "@"
                        + connection.getConnectionParam().getDatabaseName());
        pb.directory(initialDir);
        if (stderrLogFile != null)
            pb.redirectError(ProcessBuilder.Redirect.appendTo(new File(initialDir, stderrLogFile)));
        Process process = pb.start();
        try {
            process.waitFor(); // TODO may implement here timeout mechanism and progress monitor
                               // instead of just blocking the caller thread.
        } catch (InterruptedException ignored) {
        }

        final int exitCode = process.exitValue();

        // Exit codes are OS dependent. Convert them to our OS independent.
        // See: https://docs.oracle.com/cd/B19306_01/server.102/b14215/ldr_params.htm#i1005019
        switch (exitCode) {
            case 0:
                return ExitCode.SUCCESS;
            case 1:
                return ExitCode.FAIL;
            case 2:
                return ExitCode.WARN;
            case 3:
                return IS_WINDOWS ? ExitCode.FAIL : ExitCode.FATAL;
            case 4:
                return ExitCode.FATAL;
            default:
                return ExitCode.UNKNOWN;
        }
    }

    /**
     * High level API. Wraps the logic of SQL*Loader tool.
     *
     * @param conn JDBC connection matching username, password and instance arguments. Used to read
     *        the column list of the table.
     * @param username to be fed to SQL*Loader process, should match JDBC connection details.
     * @param password to be fed to SQL*Loader process, should match JDBC connection details.
     * @param instance to be fed to SQL*Loader process, should match JDBC connection details.
     * @param tableName table to be populated
     * @param dataFile tab-separated values file to be inserted to the table. Column order must
     *        match table's column order. Check by running this SQL command:
     * 
     *        <pre>
     *      select * from USER_TAB_COLUMNS where table_name = '[your-table-name]' order by COLUMN_ID
     * </pre>
     */
    public Results bulkLoad(String tableName, File dataFile) throws Exception {
        String baseFileName = dataFile.getName().substring(0, dataFile.getName().lastIndexOf("."));
        File initialDirectory = dataFile.getParentFile();
        String dataFileName = dataFile.getName();
        String controlFileName = baseFileName + ".ctl";
        String logFileName = baseFileName + ".log";
        String badFileName = baseFileName + ".bad";
        String discardFileName = baseFileName + ".discard";
        File controlFile = new File(initialDirectory, controlFileName);
        List<TableMetadata> metadata = getTableMetadata(this.connection, null, null, tableName.toUpperCase());
        String controlFileContents = null;

        if (parameters.get("CTL") == null) {
            controlFileContents = createControlFileComma(dataFileName, badFileName, discardFileName,
                    tableName, getTableColumnsToString(metadata), dataFile);
        } else {
            controlFileContents = FileUtils.ReadFile(new File(XMLHandler
                    .getDirectories(Globals.TagNames.CTL_DIR_TAG)
                    + String.valueOf(parameters.get("CTL"))));
        }
        if (controlFileContents == null) {
            throw new Exception("CTL file " + String.valueOf(parameters.get("CTL")) + " not found in "
                    + XMLHandler.getDirectories(Globals.TagNames.CTL_DIR_TAG));
        }
        Files.write(controlFile.toPath(), controlFileContents.getBytes(), StandardOpenOption.CREATE_NEW);

        final ExitCode exitCode = runSqlLdrProcess(initialDirectory, baseFileName + ".stdout.log",
                baseFileName + ".stderr.log", controlFileName, logFileName);

        // Return to the caller names of files generated inside this method.
        Results ret = new Results(exitCode, controlFile, new File(initialDirectory, logFileName), new File(
                initialDirectory, badFileName), new File(initialDirectory, discardFileName));
        return ret;
    }

    private int truncate(String tableName) throws Exception {
        logger.getLogger().info("Truncating Table " + tableName);
        PreparedStatement pstmt = connection.getConnection().prepareStatement(
                "TRUNCATE TABLE " + tableName + " CASCADE");
        return pstmt.executeUpdate();
    }

    @Override
    public void execute() throws Exception {

        List<StatementParam> listStmtParam = getListParameters(String.valueOf(parameters.get("PARAMETER")));

        while (true) {

            File dataFile = getFile(listStmtParam);
            if (dataFile == null) {
                break;
            }


            if (dataFile != null) {
                String destination = getFileDestination(dataFile, listStmtParam);
                if (destination == null) {
                    throw new Exception("Destination table for file: " + dataFile.getName()
                            + ", not provided");
                }
                String tableName = destination;
                logger.getLogger().info(
                        "Loading file " + dataFile.getName() + " into "
                                + connection.getConnectionParam().getUser() + "." + tableName);

                if (parameters.get("REFRESH") != null) {
                    switch (String.valueOf(parameters.get("REFRESH"))) {
                        case "TRUNCATE":
                            truncate(tableName);
                            break;
                        case "DELETE":
                            delete(tableName);
                    }

                }
                Results results = bulkLoad(tableName, dataFile);

                if (results.exitCode != ExitCode.SUCCESS) {
                    throw new CustomException(CustomException.Types.SQL_LOADER, "Failed. Exit code: "
                            + results.exitCode + ". See log file: " + results.logFile.getName()
                            + " (Attached)", new File[] { results.logFile });
                } else {
                    logger.getLogger().info(
                            "File " + dataFile.getName() + " loaded successfuly into table " + tableName);
                }
            }
        }
        File dir = new File(XMLHandler.getDirectories(Globals.TagNames.WORK_DIR_TAG)
                + connection.getConnectionParam().getUser().toUpperCase() + FileUtils.fileSeparator()
                + Globals.getProcessId("<PID>"));

        afterExecute(dir);

        if (parameters.get("NODELETE") == null) {
            FileUtils.DeleteDirectory(dir);
        }

    }

    private int delete(String tableName) throws Exception {
    	
        String dml = "DELETE FROM " + tableName + " ";
        if (parameters.get("DELETEBY") != null) {
        	logger.getLogger().info("Deleting from Table " + tableName + " Where " + String.valueOf(parameters.get("DELETEBY")));
            dml += "WHERE " + String.valueOf(parameters.get("DELETEBY"));
        } else {
        	logger.getLogger().info("Deleting from Table " + tableName);
        }
        PreparedStatement pstmt = connection.getConnection().prepareCall(dml);
        return pstmt.executeUpdate();
    }

    /**
     * List all files in input folder and return the oldest
     * */
    private File getFile(List<StatementParam> patterns) throws Exception {

        File dir = new File(XMLHandler.getDirectories(Globals.TagNames.INPUT_DIR_TAG));
        String schema = connection.getConnectionParam().getUser().toUpperCase();

        List<File> files = getFilesByPattern(dir.listFiles(), patterns);
        if (!files.isEmpty()) {

            if (parameters.get("LOADLAST") != null && files.size() > 1) {
                files.sort(new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
                    }
                });
                for (int i = 1; i < files.size(); i++) {
                    logger.getLogger().info("Deleting Old file: " + files.get(i).getName());
                    FileUtils.DeleteFile(files.get(i));
                }
                files = files.subList(0, 1);
            } else if (files.size() > 1) {
                
                files.sort(new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                    }
                });
            }

            for (File file : files) {

                if (file.getName().toLowerCase().endsWith(".zip")) {
                    File tempFile = ZipUtils.Unzip(file);
                    FileUtils.DeleteFile(file);
                    file = tempFile;
                }
                if (file.getName().toLowerCase().endsWith("error")) {
                    report("Error... ", FileUtils.MoveToWork(file, file.getName(), schema));
                } else {
                    return FileUtils.MoveToWork(file, file.getName().substring(0,
                            file.getName().lastIndexOf(".")), schema);
                }
            }
        }
        return null;

    }
}