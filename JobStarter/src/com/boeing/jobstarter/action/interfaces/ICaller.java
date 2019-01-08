package com.boeing.jobstarter.action.interfaces;

import java.io.File;
import java.util.HashMap;

import com.boeing.jobstarter.MyAppContext;

public interface ICaller {

    public void initCaller(HashMap<String, Object> parameters) throws Exception;

    public void terminateCaller() throws Exception;

    public void execute() throws Exception;

    public void errorHandler(Exception ex) throws Exception;

    public void report(String message, File file) throws Exception;

    public void setMyAppContext(MyAppContext myAppContext);
}
