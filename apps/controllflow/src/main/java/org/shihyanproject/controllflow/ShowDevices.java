package org.shihyanproject.controllflow;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onosproject.cli.AbstractShellCommand;

@Service
@Command(scope = "onos",name="getDevices",description = "test for 0325")
public class ShowDevices extends AbstractShellCommand {
    @Override
    protected void doExecute() {
        controllflow service = get(controllflow.class);
        print(service.getDevices());
    }
}
