package com.boeing.jobstarter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcedureMetadata {

    int columnPosition;

    String columnName;

    short columnReturn;

    int columnDataType;

    String columnReturnTypeName;

    int columnPrecision;

    int columnByteLength;

    short columnNullable;

}
