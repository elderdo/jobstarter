package com.boeing.jobstarter.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boeing.jobstarter.action.interfaces.ICommand;

@Component
public class ShowHelp implements ICommand {

    @Autowired
    public void execute(String[] args) throws Exception {
        System.out.println("Showing Help");

    }

}
