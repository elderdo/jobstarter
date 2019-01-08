package com.boeing.jobstarter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableMetadata {

    String columnName;

    String columnType;

    int columnSize;

    int columnPrecision;
}
