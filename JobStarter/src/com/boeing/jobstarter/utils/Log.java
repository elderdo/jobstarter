package com.boeing.jobstarter.utils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

/*
 * • SEVERE (highest level)
 * • WARNING
 * • INFO
 * • CONFIG
 * • FINE
 * • FINER
 * • FINEST (lowest level)
 */

@Component
public class Log {

    class LogFormatter extends Formatter {

        // Create a DateFormat to format the logger timestamp.
        private final DateFormat df;

        public LogFormatter() {
            df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        }

        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder(10000);
            builder.append(df.format(new Date(record.getMillis()))).append(" - ");
            builder.append("[").append(record.getLevel()).append("]: ");
            builder.append(formatMessage(record));
            builder.append("\n");
            return builder.toString();
        }

        public String getHead(Handler h) {
            return super.getHead(h);
        }

        public String getTail(Handler h) {
            return super.getTail(h);
        }
    }
    Logger logger;

    Handler consoleHandler = null;

    Handler fileHandler = null;

    File logFile;

    public Log() {
        logger = Logger.getLogger("JobStarter");
        LogFormatter formatter = new LogFormatter();
        // consoleHandler = new ConsoleHandler();

        // consoleHandler.setFormatter(formatter);
        // logger.addHandler(consoleHandler);
        // consoleHandler.setLevel(Level.WARNING);
        try {
            FileUtils.CreateDirectory(XMLHandler.getDirectories(Globals.TagNames.LOG_DIR_TAG));
            logFile = new File(XMLHandler.getDirectories(Globals.TagNames.LOG_DIR_TAG)
                    + "jobstarter_" + Globals.getProcessId("<PID>") + ".log");
            fileHandler = new FileHandler(logFile.getPath());

            fileHandler.setFormatter(formatter);
            fileHandler.setLevel(Level.INFO);
            logger.addHandler(fileHandler);
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
    }

    public void flush() {
        fileHandler.flush();
    }

    public void close() {
        fileHandler.flush();
        fileHandler.close();
    }

    public File getLogFile() {
        return logFile;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void setLogFile(File logFile) {
        this.logFile = logFile;
    }
}
