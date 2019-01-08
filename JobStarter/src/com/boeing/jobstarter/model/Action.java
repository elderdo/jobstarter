package com.boeing.jobstarter.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Action {

	private String id;

    private String actionType;

    private String value;

    private String jarFile;

    private String className;

    private String name;

    private List<String> require;
}
