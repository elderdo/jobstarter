package com.boeing.jobstarter;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.boeing.jobstarter.action.interfaces.ICommand;
import com.boeing.jobstarter.model.Action;
import com.boeing.jobstarter.utils.Globals;
import com.boeing.jobstarter.utils.XMLHandler;

public class Main {

    public static void main(String[] args) {
        String className = null;

        if (args != null) {

            MyAppContext context = new MyAppContext();
            context.setApplicationContext(new ClassPathXmlApplicationContext("settings/BeansConf.xml"));

            if (args[0].startsWith("-")) {
                try {
                    Action action = XMLHandler.getActionCommand(String.valueOf(args[0].substring(0, 2)));
                    if (action.getActionType().equals(Globals.ParamTypes.CALL)) {
                        className = action.getClassName();
                        if (className != null) {
                            ICommand call = context.getApplicationContext()
                                    .getBean(className, ICommand.class);
                            call.execute(args);
                            System.exit(0);
                        } else {
                            System.out.println("Error...Wrong parameter " + args[0]
                                    + ". Execute [JobStarter -h] for help");
                            System.exit(1);
                        }
                    } else {
                        System.out.println("Action " + action.getClassName() + ", is not callable");
                        System.exit(1);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
            } else {
                System.out.println("Error...Action not defined for parameter: " + args[0]
                        + ". Execute [JobStarter -h] for help");
                System.exit(1);
            }

        }
    }

}
