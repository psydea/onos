package org.shihyanproject.controllflow;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.ConnectPoint;

import java.util.ArrayList;
import java.util.List;

@Service
@Command(scope = "onos",name="makepaths",description = "test for 0325")
public class makePaths extends AbstractShellCommand {

    @Option(name="-s", aliases = "--srcpoint",description = "method",required = true,multiValued = true)
    private String srcString;

    @Option(name="-d", aliases = "--dstpoint",description = "method",required = true,multiValued = true)
    private String dstString;

    private List<ConnectPoint> srcPoint = new ArrayList<>();
    private List<ConnectPoint> dstPoint = new ArrayList<>();

    @Override
    protected void doExecute() {
        controllflow service = get(controllflow.class);
        Change();
        service.GetPaths(srcPoint,dstPoint);
    }

    private void Change() {
        int i;
        String[] temp;
        String delimeter = ",";

        srcString = srcString.substring(1,srcString.length()-1);
        temp = srcString.split(delimeter);
        for(String point :  temp){
            srcPoint.add(ConnectPoint.deviceConnectPoint(point));
        }

        dstString = dstString.substring(1,dstString.length()-1);
        temp = dstString.split(delimeter);
        for(String point :  temp){
            dstPoint.add(ConnectPoint.deviceConnectPoint(point));
        }
    }
}
