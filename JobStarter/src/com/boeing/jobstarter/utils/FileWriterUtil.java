package com.boeing.jobstarter.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileWriterUtil {

    private String directoryName;

    private String fileName;

    private File file;

    private FileWriter fileWriter;

    BufferedWriter bufferedWriter;

    private static final int BUFFER_SIZE = 4 * (int) (Math.pow(1024, 2));

    @Autowired
    private Log logger;

    public void InitWriter(String name, String extension, String schemaDest) throws Exception {
        FileUtils.CreateDirectory(XMLHandler.getDirectories(Globals.TagNames.OUTPUT_DIR_TAG));
        directoryName = XMLHandler.getDirectories(Globals.TagNames.OUTPUT_DIR_TAG);

        fileName = name + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        logger.getLogger().info("Generating File to: " + directoryName);
        if (schemaDest != null && schemaDest != "null") {
            file = File.createTempFile(schemaDest + "_" + fileName, extension, new File(directoryName));
        } else {
            file = File.createTempFile(fileName, extension, new File(directoryName));
        }

        fileWriter = new FileWriter(file);
        bufferedWriter = new BufferedWriter(fileWriter, BUFFER_SIZE);
    }

    public void writeResultSetToCSV(ResultSet resultSet, boolean header) throws Exception {
        if (resultSet == null)
            return;
        List<String> columnNames = convertMatadataColumnToList(resultSet.getMetaData());
        int rowCount = 0;
        List<String> lines = new ArrayList<String>();
        if (header) {
            lines.add(columnNames.toString().replace("[", "").replace("]", "\n"));
        }
        try {

            while (resultSet.next()) {
                StringBuilder line = new StringBuilder();

                for (String c : columnNames) {
                    String column = resultSet.getString(c);

                    column = (column == null ? "" : column).replace("\"", "'").replaceAll("[\\r\\n]+", " ");
                    if (column.contains(",")) {
                        line.append("\"").append(column).append("\"").append(",");
                    } else {
                        line.append(column).append(",");
                    }
                }
                lines.add(line.deleteCharAt(line.length() - 1) + "\n");

                if (lines.size() % BUFFER_SIZE == 0) {
                    rowCount += lines.size();
                    writeBuffered(lines);
                    lines.removeAll(lines);
                }

            }
            rowCount += lines.size();
            writeBuffered(lines);
            logger.getLogger().info("File generated with: " + rowCount + " rows");
        } catch (Exception ex) {
            throw ex;
        } finally {
            closeWriter();
        }
    }

    private List<String> convertMatadataColumnToList(ResultSetMetaData metadata) throws Exception {
        List<String> list = new ArrayList<String>();
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            list.add(metadata.getColumnName(i));
        }
        return list;
    }

    public void writeBuffered(List<String> records) throws Exception {
        records.stream().forEach(l -> {
            try {
                bufferedWriter.write(l);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        bufferedWriter.flush();
    }


    public void closeWriter() throws Exception {
        if (bufferedWriter != null) {
            bufferedWriter.close();
        }
    }

    public Log getLogger() {
        return logger;
    }

    public void setLogger(Log logger) {
        this.logger = logger;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
