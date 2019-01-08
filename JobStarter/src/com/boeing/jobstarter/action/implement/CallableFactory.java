package com.boeing.jobstarter.action.implement;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;

import com.boeing.jobstarter.MyAppContext;
import com.boeing.jobstarter.utils.Globals;
import com.boeing.jobstarter.utils.XMLHandler;

public class CallableFactory<C> {

    public C LoadClass(MyAppContext appContext, String className, String jarName, Class<C> parentClass)
            throws Exception {
        File jarFile = new File(XMLHandler.getDirectories(Globals.TagNames.LIB_DIR_TAG) + jarName);

        ClassLoader loader = URLClassLoader.newInstance(new URL[] { jarFile.toURI().toURL() }, getClass()
                .getClassLoader());
        Class<?> clazz = Class.forName(className, true, loader);

        Class<? extends C> newClass = clazz.asSubclass(parentClass);
        Constructor<? extends C> constructor = newClass.getConstructor(MyAppContext.class);
        return constructor.newInstance(appContext);
    }
}
