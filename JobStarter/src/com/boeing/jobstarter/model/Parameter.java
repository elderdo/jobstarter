package com.boeing.jobstarter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Parameter {

    private String id;

    private String value;

    private String name;

    private String require;
}
