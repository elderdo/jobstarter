package com.boeing.jobstarter.utils;

import java.io.File;


public class CustomException extends Exception {

    private static final long serialVersionUID = 1L;

    public enum Types {
        SQL_LOADER, EXTRACT_TABLE, EXTRACT_FILE, EXECUTE_PROCEDURE
    }

    Types type;

    File[] files;

    public CustomException(Types type, String message, File[] files) {
        super(message);
        setType(type);
        setFiles(files);
    }

    public Types getType() {
        return type;
    }

    public void setType(Types type) {
        this.type = type;
    }

    public File[] getFiles() {
        return files;
    }

    public void setFiles(File[] files) {
        this.files = files;
    }
}
