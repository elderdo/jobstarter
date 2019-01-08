package com.boeing.jobstarter.action.implement;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.boeing.jobstarter.MyAppContext;
import com.boeing.jobstarter.action.interfaces.ICaller;
import com.boeing.jobstarter.action.interfaces.ICommand;
import com.boeing.jobstarter.model.Action;
import com.boeing.jobstarter.model.Parameter;
import com.boeing.jobstarter.services.Callable;
import com.boeing.jobstarter.utils.Globals;
import com.boeing.jobstarter.utils.Log;
import com.boeing.jobstarter.utils.XMLHandler;

@Component
public class CallerImpl implements ICommand {

    @Autowired
    Log logger;

    ICaller caller;

    @Autowired
    MyAppContext myAppContext;

    public void execute(String[] args) throws Exception {
        boolean success = false;
        int index = 0;
        int nextRead = 2;
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        List<String> required = null;
        logger.getLogger().info("Process ID: " + Globals.getProcessId("<PID>") + " Started");
        while (index < args.length) {
            String command = args[index].contains(Globals.FIELD_SPLITER) ? args[index].substring(0,
                    args[index].indexOf(Globals.FIELD_SPLITER)) : args[index];
            Parameter parameter = null;
            switch (StringUtils.countOccurrencesOf(command, "-")) {
                case 1:
                    Action action = XMLHandler.getActionCommand(command);
                    if (action == null) {
                        throw new Exception("No logic defined for Command: " + command);
                    }
                    switch (action.getActionType()) {
                        case Globals.ParamTypes.LIB:
                            CallableFactory<Callable> factory = new CallableFactory<Callable>();
                            caller = factory.LoadClass(myAppContext, action.getClassName(), action
                                    .getJarFile(), Callable.class);
                            break;
                        case Globals.ParamTypes.CALL:
                            caller = myAppContext.getApplicationContext().getBean(action.getClassName(),
                                    ICaller.class);
                            break;

                    }
                    parameter = new Parameter();
                    parameter.setId(action.getId());
                    parameter.setName(action.getName());
                    parameter.setValue(action.getValue());
                    required = action.getRequire();
                    break;
                case 2:
                    parameter = XMLHandler.getActionParameter(command);
                    if (parameter == null) {
                        throw new Exception("No logic defined for parameter: " + command);
                    }
                    break;
                default:
                    throw new Exception("Invalid parameter definition. Must star with '-' or '--'");
            }
            switch (parameter.getValue()) {
                case Globals.ParamValue.NONE:
                    parameters.put(parameter.getName(), true);
                    nextRead = 1;
                    break;
                case Globals.ParamValue.READ:
                    parameters.put(parameter.getName(), args[index].substring(args[index]
                            .indexOf(Globals.FIELD_SPLITER) + 1, args[index].length()));
                    nextRead = 1;
                    break;
                case Globals.ParamValue.NEXT:
                    try {
                        parameters.put(parameter.getName(), args[index + 1]);
                    } catch (Exception ex) {
                        throw new Exception("Missing paramater(s) for command: " + args[index]);
                    }
                    nextRead = 2;
                    break;
                default:
                    parameters.put(parameter.getName(), parameter.getValue());
                    nextRead = 1;
                    break;
            }
            index += nextRead;
        }
        String missed = validateRequired(required, parameters);
        if (missed == null) {
            try {
                this.caller.initCaller(parameters);
                this.caller.execute();
                success = true;
            } catch (Exception ex) {
                this.caller.errorHandler(ex);
                success = false;
                throw ex;
            } finally {
                this.caller.terminateCaller();
                if (success
                        && (parameters.get("REPORT") != null && (String.valueOf(parameters.get("REPORT"))
                                .equalsIgnoreCase("SUCCESS") || String.valueOf(parameters.get("REPORT"))
                                .equalsIgnoreCase("BOTH")))) {
                    this.caller.report("Success... ", logger.getLogFile());

                }
            }
        } else {
            throw new Exception("Missing required parameter(s) for Action: "
                    + this.caller.getClass().getSimpleName()
                    + ": [" + missed + "]");
        }

    }

    private String validateRequired(List<String> required, HashMap<String, Object> parameters)
            throws Exception {
        if (required == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String r : required) {
            if (parameters.get(r) == null) {
                sb.append(r).append(", ");
            }
        }
        if (sb.length() != 0) {
            return (sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1).toString());
        }
        return null;
    }

    public Log getLogger() {
        return logger;
    }

    public void setLogger(Log logger) {
        this.logger = logger;
    }

    public MyAppContext getMyAppContext() {
        return myAppContext;
    }

    public void setMyAppContext(MyAppContext myAppContext) {
        this.myAppContext = myAppContext;
    }

}
