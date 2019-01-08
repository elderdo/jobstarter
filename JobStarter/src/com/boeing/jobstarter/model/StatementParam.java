package com.boeing.jobstarter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatementParam {

    private int position;

    private String alias;

    private String columnName;

    private String operation; // (=,!=,<>,<,>)

    private String value;

    private String file;
}
