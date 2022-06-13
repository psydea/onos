package org.shihyanproject.controllflow;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.DeviceId;

@Service
@Command(scope = "onos",name="getpath",description = "test for 0325")
public class GetPath extends AbstractShellCommand {

    @Argument(index=0,name="deviceId_1",description = "Host ID Source",required = true,multiValued = false)
    private String deviceId_1;

    @Argument(index=1,name="deviceId_2",description = "Host ID Destination",required = true,multiValued = false)
    private String deviceId_2;

    @Override
    protected void doExecute() {
        controllflow service = get(controllflow.class);
        DeviceId deviceId_Src = DeviceId.deviceId(deviceId_1);
        DeviceId deviceId_Des = DeviceId.deviceId(deviceId_2);
        print(service.getPaths_test(deviceId_Src,deviceId_Des));
    }
}

/*
        printKShortestPath += "\n";
        printPath+=printDisjoinPath;
        printPath+=printKShortestPath;
        return printPath;
 */