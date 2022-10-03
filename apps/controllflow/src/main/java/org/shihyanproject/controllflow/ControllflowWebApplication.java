package org.shihyanproject.controllflow;

import org.onlab.rest.AbstractWebApplication;

import java.util.Set;

public class ControllflowWebApplication extends AbstractWebApplication {
    @Override
    public Set<Class<?>> getClasses() {
        return getClasses(ControllflowWebResource.class);
    }
}
