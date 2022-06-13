package org.shihyanproject.controllflow;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onlab.packet.VlanId;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

@Service
@Command(scope = "onos",name="modifyflow",description = "test for 0325")
public class ModifyFlow extends AbstractShellCommand {

    @Argument(index=0,name="flow_method",description = "method",required = true,multiValued = false)
    private String flow_method;

    @Argument(index=1,name="devices",description = "method",required = true,multiValued = false)
    private String devices;

    @Argument(index=2,name="vlan",description = "method",required = true,multiValued = false)
    private String vlan;

    @Argument(index=3,name="port",description = "method",required = true,multiValued = false)
    private String port;

    @Argument(index=4,name="port2",description = "method",required = false,multiValued = false)
    private String port2;

    @Override
    protected void doExecute() {
        controllflow service = get(controllflow.class);
        DeviceId deviceId = DeviceId.deviceId(devices);
        VlanId vlanId = VlanId.vlanId(vlan);
        PortNumber portNumber = PortNumber.portNumber(port);
        PortNumber portNumber2 = PortNumber.portNumber(port2);
        if(flow_method.equals("In")) {
            log.info("In flow rule");
            service.createFlowRuleIn(deviceId,vlanId,portNumber,portNumber2);
        }
        else {
            service.createFlowRuleOut(deviceId,vlanId,portNumber);
        }
    }
}
