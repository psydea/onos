package org.shihyanproject.controllflow;

import org.onlab.packet.Ethernet;
import org.onlab.packet.VlanId;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultLink;
import org.onosproject.net.DefaultPath;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.topology.PathService;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowAnalyze {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Set<ConnectPoint> FirstNodeSet = new LinkedHashSet<>();
    private Set<ConnectPoint> EndNodeSet = new LinkedHashSet<>();
    private Set<ConnectPoint> NodeSet = new LinkedHashSet<>();
    protected List<VlanId> Vlanlist;

    private Map<DeviceId,Set<PortNumber>> FirstMap = new HashMap<>();
    private Map<DeviceId,Set<PortNumber>> EndMap = new HashMap<>();
    private Map<DeviceId,Set<PortNumber>> NodeMap = new HashMap<>();
    private Map<DeviceId,Set<PortNumber>> FirstPort = new HashMap<>();

    private int PriorityNum = 60000;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PathService pathService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    protected int times;
    protected ApplicationId appid;
    public void createFlowEntry(List<ConnectPoint> src,List<ConnectPoint> dst) {
        VlanId vlanId = chooseVlan();
        appid = coreService.registerApplication(String.valueOf(times));

        for(int i=0;i<src.size();i++) {
            if( !FirstNodeSet.contains(src.get(i)) ) {
                FirstNodeSet.add(src.get(i));
            }
        }
        for(int i=0;i<dst.size();i++) {
            if( !EndNodeSet.contains(dst.get(i)) ) {
                EndNodeSet.add(dst.get(i));
            }
        }

        for( ConnectPoint SourcePoint:FirstNodeSet ) {
            for( ConnectPoint DestinationPoint:EndNodeSet ) {
                GetPaths( SourcePoint.deviceId(), DestinationPoint.deviceId() );
            }
        }

        ArrangeNodeInfo("src");
        ArrangeNodeInfo("dst");

        FirstFlowRule(vlanId);
        NodeFlowRule(vlanId);
        EndFlowRule(vlanId);
    }

    private void GetPaths(DeviceId Source, DeviceId Destination) {
        log.info("GetPath:"+Source.toString()+" -> "+Destination.toString());
        Set<Path> pathTable = pathService.getPaths(Source, Destination);
        Object [] ObjectPath = pathTable.toArray();
        DefaultPath PathInfo = (DefaultPath) ObjectPath[0];
        List<Link> LinkList = PathInfo.links();
        DefaultLink LinkInfo;
        ConnectPoint LinkSource;
        for(int i=0;i<LinkList.size();i++) {
            LinkInfo = (DefaultLink) LinkList.get(i);
            LinkSource = LinkInfo.src();
            if( i==0 ) {
                Set<PortNumber> PortSet = new LinkedHashSet<>();
                if( FirstPort.containsKey(LinkSource.deviceId()) ) {
                    PortSet = FirstPort.get(LinkSource.deviceId());
                }
                PortSet.add(LinkSource.port());
                FirstPort.put(LinkSource.deviceId(),PortSet);
            }
            else if( !NodeSet.contains(LinkSource) ) {
                NodeSet.add(LinkSource);
            }
        }

        ArrangeNodeInfo("Node");
    }

    private void NodeFlowRule(VlanId vlan) {
        Iterator< Map.Entry<DeviceId,Set<PortNumber>> > itera = NodeMap.entrySet().iterator();
        Map.Entry<DeviceId,Set<PortNumber>> entry;
        Set<PortNumber> PortSet;
        while( itera.hasNext() ) {
            entry = itera.next();
            PortSet = entry.getValue();
            TrafficSelector.Builder trafficS_In = DefaultTrafficSelector.builder();
            trafficS_In.matchEthType(Ethernet.TYPE_IPV4)
                    .matchVlanId(vlan);

            // action
            TrafficTreatment.Builder trafficT = DefaultTrafficTreatment.builder();
            trafficT.immediate();

            for( PortNumber port:PortSet ) {
                trafficT.setOutput(port);
            }

            // rule
            FlowRule.Builder flowRuleB_In = DefaultFlowRule.builder();
            flowRuleB_In.withSelector(trafficS_In.build())
                    .withTreatment(trafficT.build())
                    .forDevice(entry.getKey())
                    .fromApp(appid)
                    .makePermanent()
                    .withPriority(PriorityNum);

            flowRuleService.applyFlowRules(flowRuleB_In.build());
        }
    }

    private void FirstFlowRule(VlanId vlan) {
        Iterator< Map.Entry<DeviceId,Set<PortNumber>> > DeviceIterator = FirstMap.entrySet().iterator();
        Map.Entry<DeviceId,Set<PortNumber>> DeviceEntry,PortEntry;
        Set<PortNumber> DeviceSet,PortSet;
        while( DeviceIterator.hasNext() ) {
            // Get device entry and
            DeviceEntry = DeviceIterator.next();
            DeviceSet = DeviceEntry.getValue();

            // action
            TrafficTreatment.Builder trafficT = DefaultTrafficTreatment.builder();
            trafficT.pushVlan()
                    .setVlanId(vlan)
                    .immediate();

            Iterator< Map.Entry<DeviceId,Set<PortNumber>> > itera_Port = FirstPort.entrySet().iterator();
            while( itera_Port.hasNext() ) {
                PortEntry = itera_Port.next();
                if( PortEntry.getKey().equals(DeviceEntry.getKey())) {
                    PortSet = PortEntry.getValue();
                    for( PortNumber port:PortSet ) {
                        trafficT.setOutput(port);
                    }
                }

            }

            for( PortNumber port:DeviceSet ) {
                // match
                TrafficSelector.Builder trafficS_In = DefaultTrafficSelector.builder();
                trafficS_In.matchEthType(Ethernet.TYPE_IPV4);
                trafficS_In.matchInPort(port);

                // rule
                FlowRule.Builder flowRuleB_In = DefaultFlowRule.builder();
                flowRuleB_In.withSelector(trafficS_In.build())
                        .withTreatment(trafficT.build())
                        .forDevice(DeviceEntry.getKey())
                        .fromApp(appid)
                        .makePermanent()
                        .withPriority(PriorityNum);

                flowRuleService.applyFlowRules(flowRuleB_In.build());
                log.info("First Node Entry=" + DeviceEntry + " Key=" + DeviceEntry.getKey() + " Value=" + DeviceEntry.getValue() + "\n");

            }
        }
    }

    private void EndFlowRule(VlanId vlan) {
        Iterator< Map.Entry<DeviceId,Set<PortNumber>> > Iterator = EndMap.entrySet().iterator();
        Map.Entry<DeviceId,Set<PortNumber>> entry;
        Set<PortNumber> PortSet;
        while( Iterator.hasNext() ) {
            entry = Iterator.next();
            PortSet = entry.getValue();
            // match
            TrafficSelector.Builder trafficS_In = DefaultTrafficSelector.builder();
            trafficS_In.matchEthType(Ethernet.TYPE_IPV4)
                    .matchVlanId(vlan);

            // action
            TrafficTreatment.Builder trafficT = DefaultTrafficTreatment.builder();
            trafficT.popVlan()
                    .immediate();

            for( PortNumber port:PortSet ) {
                trafficT.setOutput(port);
            }

            // rule
            FlowRule.Builder flowRuleB_In = DefaultFlowRule.builder();
            flowRuleB_In.withSelector(trafficS_In.build())
                    .withTreatment(trafficT.build())
                    .forDevice(entry.getKey())
                    .fromApp(appid)
                    .makePermanent()
                    .withPriority(PriorityNum);

            flowRuleService.applyFlowRules(flowRuleB_In.build());
        }
    }

    private VlanId chooseVlan() {
        short vlan = 2;
        while(Vlanlist.contains(VlanId.vlanId(vlan))) {
            vlan += 1;
        }
        Vlanlist.add(VlanId.vlanId(vlan));

        return VlanId.vlanId(vlan);
    }

    private void ArrangeNodeInfo(String option) {
        DeviceId PointDeviceId;
        PortNumber PointPort;
        if (option.equals("Node")) {
            for( ConnectPoint Point:NodeSet ) {
                PointDeviceId = Point.deviceId();
                PointPort = Point.port();
                Set<PortNumber> PortSet = new LinkedHashSet<>();
                if( NodeMap.containsKey(PointDeviceId) ) {
                    PortSet = NodeMap.get(PointDeviceId);
                }
                PortSet.add(PointPort);
                NodeMap.put(PointDeviceId,PortSet);
            }
        }
        else if ( option.equals(("src")) ) {
            for( ConnectPoint Point:FirstNodeSet ) {
                PointDeviceId = Point.deviceId();
                PointPort = Point.port();
                Set<PortNumber> PortSet = new LinkedHashSet<>();
                if( FirstMap.containsKey(PointDeviceId) ) {
                    PortSet = FirstMap.get(PointDeviceId);
                }
                PortSet.add(PointPort);
                FirstMap.put(PointDeviceId,PortSet);
            }
        }
        else {
            for( ConnectPoint Point:EndNodeSet ) {
                PointDeviceId = Point.deviceId();
                PointPort = Point.port();
                Set<PortNumber> PortSet = new LinkedHashSet<>();
                if( EndMap.containsKey(PointDeviceId) ) {
                    PortSet = EndMap.get(PointDeviceId);
                }
                PortSet.add(PointPort);
                EndMap.put(PointDeviceId,PortSet);
            }
        }

    }
}
