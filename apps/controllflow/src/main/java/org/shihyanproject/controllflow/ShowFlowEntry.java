package org.shihyanproject.controllflow;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.DeviceId;

@Service
@Command(scope = "onos",name="showflowentry",description = "test for 0325")
public class ShowFlowEntry extends AbstractShellCommand {

    @Argument(index=0,name="deviceId_String",description = "device ID 1")
    private String deviceId_String;

    @Override
    protected void doExecute() {
        controllflow service = get(controllflow.class);
        DeviceId deviceId = DeviceId.deviceId(deviceId_String);
        print(service.getFlowEntry(deviceId));
    }
}
