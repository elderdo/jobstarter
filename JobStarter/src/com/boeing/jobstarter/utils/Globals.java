package com.boeing.jobstarter.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public abstract class Globals {

    public static class TagNames {

        public static final String INPUT_DIR_TAG = "input-dir";

        public static final String LOG_DIR_TAG = "log-dir";

        public static final String WORK_DIR_TAG = "work-dir";

        public static final String OUTPUT_DIR_TAG = "output-dir";

        public static final String CTL_DIR_TAG = "ctl-dir";

        public static final String LIB_DIR_TAG = "lib-dir";

        public static final String ARCHIVE_DIR_TAG = "archive-dir";

        public static final String SCRIPT_DIR_TAG = "script-dir";

        public static final String MAIL_SMTP_SERVER_TAG = "smtp-server";

        public static final String MAIL_SMTP_PORT_TAG = "smtp-port";

        public static final String MAIL_SENDER_MAIL_TAG = "sender-mail";

        public static final String MAIL_SENDER_NAME_TAG = "sender-name";

        public static final String MAIL_AUTH_TAG = "auth-type";

        public static final String MAIL_USER_TAG = "user";

        public static final String MAIL_PWD_TAG = "password";

        public static final String BULK_SIZE_TAG = "bulk-size";
    }

    public static class ParamTypes {

        public static final String PARAMETER = "parameter";
        public static final String CALL = "call";

        public static final String LIB = "lib";
        public static final String CONFIG = "config";
    }

    public static class ParamValue {

        public static final String NONE = "none";

        public static final String READ = "read";

        public static final String NEXT = "next";
    }

    public static final String PARAM_SPLITER = ",";

    public static final String FIELD_SPLITER = "=";

    public static final String ALIAS_SPLITER = ".";

    public enum Answer {
        Y, N, YES, NO, D, U;
    }

    public enum AskType {
        YN, DU;
    }

    public static final int ORACLE_BUFFER_SIZE = 1000000;

    public static String ask(String message, boolean required) {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = null;
        boolean valid = false;
        while (!valid) {
            try {
                System.out.print(message);
                line = br.readLine();
                if ((line == null || line.length() == 0) && required) {
                    valid = false;
                } else {
                    valid = true;
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        return ((line != null) ? ((line.length() == 0) ? null : line) : line);
    }

    public static String askPassword(String message, boolean required) {
        String password = null;
        int numEntered = 0;
        String previusString = null;
        boolean valid = false;
        while (!valid) {
            try {

                if (numEntered == 0) {
                    password = PasswordField.readPassword(message);
                } else {
                    password = PasswordField.readPassword("Re-" + message);
                }

                if ((password == null || password.length() == 0) && required) {
                    valid = false;
                } else {
                    if (numEntered == 0) {
                        previusString = password;
                        numEntered++;
                    } else {
                        if (previusString.compareTo(password) == 0) {
                            valid = true;
                        } else {
                            System.out.println("Password don't match first entry. Try again");
                            valid = false;
                            numEntered = 0;
                        }

                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }

        return ((password != null) ? ((password.length() == 0) ? null : password) : password);
    }

    public static Answer askConfirmation(String message, AskType askType, boolean required) {
        String line = null;
        Answer answer = Answer.N;
        boolean valid = false;

        while (!valid) {

            line = ask(message, false);
            switch (askType) {
                case YN:
                    if (Answer.Y.toString().equalsIgnoreCase(line)
                            || Answer.YES.toString().equalsIgnoreCase(line)) {
                        answer = Answer.Y;
                        valid = true;
                    } else if (Answer.N.toString().equalsIgnoreCase(line)
                            || Answer.NO.toString().equalsIgnoreCase(line)) {
                        answer = Answer.N;
                        valid = true;
                    } else if (!required) {
                        valid = true;
                    }
                    break;
                case DU:
                    if (Answer.D.toString().equalsIgnoreCase(line)) {
                        answer = Answer.D;
                        valid = true;
                    } else if (Answer.U.toString().equalsIgnoreCase(line)) {
                        answer = Answer.U;
                        valid = true;
                    } else if (!required) {
                        valid = true;
                    }
                    break;
                default:
                    answer = Answer.N;
                    valid = true;
                    break;
            }

        }
        return answer;
    }



    public static Object nvl(Object obj1, Object obj2) {
        return (obj1 == null) ? obj2 : obj1;
    }


    public static String getVariableValue(String variable) {
        switch (variable.toUpperCase()) {
            case "APP_DIRECTORY":
                return System.getProperty("user.dir");
            default:
                return null;
        }
    }


    public static String stackTraceToString(Exception ex) {
        StringBuilder builder = new StringBuilder();
        StackTraceElement[] trace = ex.getStackTrace();
        for (StackTraceElement traceElement : trace)
            builder.append("\tat " + traceElement + "\n");
        return builder.toString();
    }

    public static String getProcessId(final String fallback) {
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');
        String id = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        if (index < 1) {
            return id;
        }

        try {
            id = Long.toString(Long.parseLong(jvmName.substring(0, index)));
        } catch (NumberFormatException e) {
            // ignore
        }
        return id;
    }

}
