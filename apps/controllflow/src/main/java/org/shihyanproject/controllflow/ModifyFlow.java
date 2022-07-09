package org.shihyanproject.controllflow;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onlab.packet.VlanId;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

@Service
@Command(scope = "onos",name="modifyflow",description = "test for 0325")
public class ModifyFlow extends AbstractShellCommand {

    @Option(name="-i", aliases = "--application-id",description = "application ID",required = true,multiValued = false)
    private String String_applicationID;

    @Override
    protected void doExecute() {
        controllflow service = get(controllflow.class);
        service.DeleteFlowEntry(String_applicationID);
    }
}
