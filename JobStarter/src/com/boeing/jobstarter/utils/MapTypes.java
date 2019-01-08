package com.boeing.jobstarter.utils;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Base64;

import org.springframework.util.StringUtils;

public class MapTypes {

    public static Class<?> map(int jdbcType) throws Exception {

        switch (jdbcType) {
            case java.sql.Types.OTHER:
            case java.sql.Types.BIT:
            case java.sql.Types.BOOLEAN:
                return java.lang.Boolean.class;
            case java.sql.Types.TINYINT:
            case java.sql.Types.SMALLINT:
            case java.sql.Types.INTEGER:
                return java.lang.Integer.class;
            case java.sql.Types.BIGINT:
                return java.lang.Long.class;
            case java.sql.Types.FLOAT:
            case java.sql.Types.DOUBLE:
                return java.lang.Double.class;
            case java.sql.Types.REAL:
                return java.lang.Float.class;
                // according to [1] Table B-1
            case java.sql.Types.NUMERIC:
            case java.sql.Types.DECIMAL:
                return java.math.BigDecimal.class;
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR:
                return java.lang.String.class;
            case java.sql.Types.DATE:
                return java.sql.Date.class;
            case java.sql.Types.TIME:
                return java.sql.Time.class;
            case java.sql.Types.TIMESTAMP:
                return java.sql.Timestamp.class;
            case java.sql.Types.STRUCT:
                return java.sql.Struct.class;
            case java.sql.Types.ARRAY:
                return java.sql.Array.class;
            case java.sql.Types.BLOB:
                return java.sql.Blob.class;
            case java.sql.Types.CLOB:
                return java.sql.Clob.class;
            case java.sql.Types.REF:
                return java.sql.Ref.class;
            case java.sql.Types.DATALINK:
                return java.net.URL.class;
            case java.sql.Types.ROWID:
                return java.sql.RowId.class;
            case java.sql.Types.NULL:
            case java.sql.Types.JAVA_OBJECT:
            case java.sql.Types.DISTINCT:
            case java.sql.Types.BINARY:
            case java.sql.Types.VARBINARY:
            case java.sql.Types.LONGVARBINARY:
            default:
                throw new Exception("Invalid or Unmapped SQL type (" + jdbcType + ")");
        }
    }

    public static int map(String oracleType) throws Exception {
        switch (oracleType.toUpperCase()) {
            case "CHAR":
                return java.sql.Types.CHAR;
            case "VARCHAR2":
            case "VARCHAR":
                return java.sql.Types.VARCHAR;
            case "NCHAR":
                return java.sql.Types.NCHAR;
            case "LONG":
                return java.sql.Types.LONGVARCHAR;
            case "NUMBER":
                return java.sql.Types.NUMERIC;
            case "RAW":
                return java.sql.Types.BINARY;
            case "LONGRAW":
                return java.sql.Types.VARBINARY;
            case "DATE":
                return java.sql.Types.DATE;
            case "TIMESTAMP":
                return java.sql.Types.TIMESTAMP;
            case "BLOB":
                return java.sql.Types.BLOB;
            case "CLOB":
                return java.sql.Types.CLOB;
            case "user-defined object":
                return java.sql.Types.STRUCT;
            case "user-defined reference":
                return java.sql.Types.REF;
            case "user-defined collection":
                return java.sql.Types.ARRAY;
            case "ROWID":
                return java.sql.Types.ROWID;
            case "NCLOB":
                return java.sql.Types.NCLOB;
            default:
                throw new Exception("Invalid or Unmapped SQL type (" + oracleType + ")");
        }
    }



    public static int fulFillPreparedStatement(CallableStatement pstmt, int index, int dataType, String value)
            throws Exception {

        switch (dataType) {
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR:
            case java.sql.Types.NCHAR:
            case java.sql.Types.NVARCHAR:
            case java.sql.Types.LONGNVARCHAR:
                pstmt.setString(index, value);
                break;
            case java.sql.Types.NUMERIC:
            case java.sql.Types.DECIMAL:
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    if (value.indexOf(".") == -1) {
                        Long theLong = Long.valueOf(value);
                        pstmt.setBigDecimal(index, BigDecimal.valueOf(theLong));
                    } else {
                        Double d = Double.valueOf(value);
                        pstmt.setBigDecimal(index, BigDecimal.valueOf(d));
                    }
                }
                break;
            case java.sql.Types.BOOLEAN:
            case java.sql.Types.BIT:
            case java.sql.Types.OTHER:
                if (value.equalsIgnoreCase("true")) {
                    value = "1";
                } else if (value.equalsIgnoreCase("false")) {
                    value = "0";
                }
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    pstmt.setInt(index, Integer.valueOf(value));
                }
                break;

            case java.sql.Types.TINYINT:
            case java.sql.Types.SMALLINT:
            case java.sql.Types.INTEGER:
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    pstmt.setInt(index, Integer.valueOf(value));
                }
                break;
            case java.sql.Types.BIGINT:
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    pstmt.setLong(index, Long.valueOf(value));
                }
                break;
            case java.sql.Types.REAL:
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    pstmt.setFloat(index, Float.valueOf(value));
                }
                break;
            case java.sql.Types.FLOAT:
            case java.sql.Types.DOUBLE:
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    pstmt.setDouble(index, Double.valueOf(value));
                }
                break;
            case java.sql.Types.BINARY:
            case java.sql.Types.VARBINARY:
            case java.sql.Types.LONGVARBINARY:
            case java.sql.Types.BLOB:
            case java.sql.Types.CLOB:
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    byte[] content = null;
                    try {
                        content = Base64.getDecoder().decode(value.getBytes("UTF-8"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (content == null) {
                        pstmt.setNull(index, dataType);
                    } else {
                        pstmt.setBytes(index, content);
                    }
                }
                break;
            case java.sql.Types.DATE:
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    java.sql.Date sqlDate = java.sql.Date.valueOf(value);
                    pstmt.setDate(index, sqlDate);
                }
                break;
            case java.sql.Types.TIME:
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    java.sql.Time t = java.sql.Time.valueOf(value);
                    pstmt.setTime(index, t);
                }
                break;
            case java.sql.Types.TIMESTAMP:
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    String dt = value;
                    if (dt.indexOf("T") >= 0) {
                        dt = dt.replace("T", " ");
                    }
                    java.sql.Timestamp ts = Timestamp.valueOf(dt);
                    pstmt.setTimestamp(index, ts);
                }
                break;

        }

        return index + 1;
    }

    public static int fulFillPreparedStatement(PreparedStatement pstmt, int index, int dataType, String value)
            throws Exception {

        switch (dataType) {
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.LONGVARCHAR:
            case java.sql.Types.NCHAR:
            case java.sql.Types.NVARCHAR:
            case java.sql.Types.LONGNVARCHAR:
                pstmt.setString(index, value);
                break;
            case java.sql.Types.NUMERIC:
            case java.sql.Types.DECIMAL:
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    if (value.indexOf(".") == -1) {
                        Long theLong = Long.valueOf(value);
                        pstmt.setBigDecimal(index, BigDecimal.valueOf(theLong));
                    } else {
                        Double d = Double.valueOf(value);
                        pstmt.setBigDecimal(index, BigDecimal.valueOf(d));
                    }
                }
                break;
            case java.sql.Types.BOOLEAN:
            case java.sql.Types.BIT:
            case java.sql.Types.OTHER:
                if (value.equalsIgnoreCase("true")) {
                    value = "1";
                } else if (value.equalsIgnoreCase("false")) {
                    value = "0";
                }
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    pstmt.setInt(index, Integer.valueOf(value));
                }
                break;

            case java.sql.Types.TINYINT:
            case java.sql.Types.SMALLINT:
            case java.sql.Types.INTEGER:
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    pstmt.setInt(index, Integer.valueOf(value));
                }
                break;
            case java.sql.Types.BIGINT:
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    pstmt.setLong(index, Long.valueOf(value));
                }
                break;
            case java.sql.Types.REAL:
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    pstmt.setFloat(index, Float.valueOf(value));
                }
                break;
            case java.sql.Types.FLOAT:
            case java.sql.Types.DOUBLE:
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    pstmt.setDouble(index, Double.valueOf(value));
                }
                break;
            case java.sql.Types.BINARY:
            case java.sql.Types.VARBINARY:
            case java.sql.Types.LONGVARBINARY:
            case java.sql.Types.BLOB:
            case java.sql.Types.CLOB:
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    byte[] content = null;
                    try {
                        content = Base64.getDecoder().decode(value.getBytes("UTF-8"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (content == null) {
                        pstmt.setNull(index, dataType);
                    } else {
                        pstmt.setBytes(index, content);
                    }
                }
                break;
            case java.sql.Types.DATE:
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    java.sql.Date sqlDate = java.sql.Date.valueOf(value);
                    pstmt.setDate(index, sqlDate);
                }
                break;
            case java.sql.Types.TIME:
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    java.sql.Time t = java.sql.Time.valueOf(value);
                    pstmt.setTime(index, t);
                }
                break;
            case java.sql.Types.TIMESTAMP:
                if (StringUtils.isEmpty(value)) {
                    pstmt.setNull(index, dataType);
                } else {
                    String dt = value;
                    if (dt.indexOf("T") >= 0) {
                        dt = dt.replace("T", " ");
                    }
                    java.sql.Timestamp ts = Timestamp.valueOf(dt);
                    pstmt.setTimestamp(index, ts);
                }
                break;

        }

        return index + 1;
    }
}
