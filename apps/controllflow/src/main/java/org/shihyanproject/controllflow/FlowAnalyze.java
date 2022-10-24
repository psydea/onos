package org.shihyanproject.controllflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onlab.packet.*;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultLink;
import org.onosproject.net.DefaultPath;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.topology.PathService;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

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

    private final Set<ConnectPoint> FirstNodeSet = new LinkedHashSet<>();
    private final Set<ConnectPoint> EndNodeSet = new LinkedHashSet<>();
    private final Set<ConnectPoint> NodeSet = new LinkedHashSet<>();
    private final Map<DeviceId,Set<PortNumber>> FirstMap = new HashMap<>();
    private final Map<DeviceId,Set<PortNumber>> EndMap = new HashMap<>();
    private final Map<DeviceId,Set<PortNumber>> NodeMap = new HashMap<>();
    private final Map<DeviceId,Set<PortNumber>> FirstPort = new HashMap<>();

    private final int PriorityNum = 60000;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PathService pathService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;
    protected ObjectMapper mapper = new ObjectMapper();
    protected ObjectNode RequestMessage;

    public ObjectNode createRule(ControllFlowRule rule) {
        ApplicationId appId = coreService.registerApplication(rule.appId());
        RequestMessage = mapper.createObjectNode();

        // Try to implement stoppage
        /*
        try {
            Thread.sleep(10 * 1000);
            log.info("Stop 1 second");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/

        // Clean source and destination point data
        try {
            for(int i=0;i<rule.srcPoint().size();i++) {
                if( !FirstNodeSet.contains(rule.srcPoint().get(i)) ) {
                    FirstNodeSet.add(rule.srcPoint().get(i));
                }
            }
        }catch (IndexOutOfBoundsException IndexOutException) {
            RequestMessage.put("Code",500);
            RequestMessage.put("Message","Source connectPoint index error");
            log.error("Source ConnectPoint Index Error");
            return RequestMessage;
        }catch (Exception e) {
            RequestMessage.put("Code",500);
            RequestMessage.put("Message","Source node error");
            log.error("Source Node Error");
            return RequestMessage;
        }

        try {
            for(int i=0;i<rule.dstPoint().size();i++) {
                if( !EndNodeSet.contains(rule.dstPoint().get(i)) ) {
                    EndNodeSet.add(rule.dstPoint().get(i));
                }
            }
        }catch (IndexOutOfBoundsException IndexOutException) {
            RequestMessage.put("Code",500);
            RequestMessage.put("Message","Destination connect point index error");
            log.error("Destination ConnectPoint Index Error");
            return RequestMessage;
        }catch (Exception e) {
            RequestMessage.put("Code",500);
            RequestMessage.put("Message","Destination node error");
            log.error("Destination Node Error");
            return RequestMessage;
        }

        // Get path data
        try {
            for( ConnectPoint SourcePoint:FirstNodeSet ) {
                for( ConnectPoint DestinationPoint:EndNodeSet ) {
                    GetPaths( SourcePoint.deviceId(), DestinationPoint.deviceId() );
                }
            }
        }catch (Exception e) {
            RequestMessage.put("Code",500);
            RequestMessage.put("Message","Get paths error");
            log.error("Get Paths Error");
            return RequestMessage;
        }

        // Clean data
        try {
            ArrangeNodeInfo("src");
        }catch (Exception e) {
            RequestMessage.put("Code",500);
            RequestMessage.put("Message","Arrange node information on source error");
            log.error("Arrange node info on source error!");
            return RequestMessage;
        }

        try {
            ArrangeNodeInfo("dst");
        }catch (Exception e) {
            RequestMessage.put("Code",500);
            RequestMessage.put("Message","Arrange node information on destination error");
            log.error("Arrange node info on destination error!");
            return RequestMessage;
        }

        // Add flow rule
        try {
            FirstFlowRule(rule,appId);
        }catch (Exception e) {
            RequestMessage.put("Code",500);
            RequestMessage.put("Message","Create first node flow rule error");
            log.error("Create first node flow rule error!");
            return RequestMessage;
        }
        try {
            NodeFlowRule(rule,appId);
        }catch (Exception e) {
            RequestMessage.put("Code",500);
            RequestMessage.put("Message","Create pass node flow rule error");
            log.error("Create pass node flow rule error!");
            return RequestMessage;
        }
        try {
            EndFlowRule(rule,appId);
        }catch (Exception e) {
            RequestMessage.put("Code",500);
            RequestMessage.put("Message","Create end node flow rule error");
            log.error("Create end node flow rule error!");
            return RequestMessage;
        }
        RequestMessage.put("Code",200);
        RequestMessage.put("Message","Add flow rule success");
        log.info("Created");

        return RequestMessage;
    }

    private void GetPaths(DeviceId Source, DeviceId Destination) {
        try {
            Set<Path> pathTable = pathService.getPaths(Source, Destination);
            Object [] ObjectPath = pathTable.toArray();
            DefaultPath PathInfo = (DefaultPath) ObjectPath[0];
            log.info("Path:"+PathInfo.toString());
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
        }catch(Exception e) {
            log.error("GetPaths Error");
            throw new IllegalArgumentException("Get paths error",e);
        }

        ArrangeNodeInfo("Node");
    }

    private void NodeFlowRule(ControllFlowRule rule, ApplicationId appID) {
        Iterator< Map.Entry<DeviceId,Set<PortNumber>> > itera = NodeMap.entrySet().iterator();
        Map.Entry<DeviceId,Set<PortNumber>> entry;
        Set<PortNumber> PortSet;
        while( itera.hasNext() ) {
            entry = itera.next();
            PortSet = entry.getValue();
            TrafficSelector.Builder trafficS_In = DefaultTrafficSelector.builder();

            if( rule.tagVlan() != null ) {
                trafficS_In.matchVlanId(rule.tagVlan());
            }

            // action
            TrafficTreatment.Builder trafficT = DefaultTrafficTreatment.builder();
            trafficT.immediate();

            for( PortNumber port:PortSet ) {
                trafficT.setOutput(port);
            }

            // rule
            org.onosproject.net.flow.FlowRule.Builder flowRuleB_In = DefaultFlowRule.builder();
            flowRuleB_In.withSelector(trafficS_In.build())
                    .withTreatment(trafficT.build())
                    .forDevice(entry.getKey())
                    .makePermanent()
                    .withPriority(PriorityNum)
                    .fromApp(appID);

            flowRuleService.applyFlowRules(flowRuleB_In.build());
        }
    }

    private void FirstFlowRule(ControllFlowRule rule, ApplicationId appID) {
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
                    .setVlanId(rule.tagVlan())
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
                log.info("Firtst Device Port:"+port.toString());
                // match
                TrafficSelector.Builder trafficS_In = DefaultTrafficSelector.builder();
                trafficS_In.matchInPort(port);

                if( rule.ethernetType() != null ) {
                    trafficS_In.matchEthType(rule.ethernetType().ethType().toShort());
                }else {
                    trafficS_In.matchEthType(Ethernet.TYPE_IPV4);
                }

                if( rule.protocol() != 0 ) {
                    trafficS_In.matchIPProtocol(rule.protocol());
                }

                if( rule.sourceIP() != null ) {
                    if( rule.protocol() == IPv4.PROTOCOL_TCP || rule.protocol() == IPv4.PROTOCOL_UDP) {
                        trafficS_In.matchIPSrc(IpPrefix.valueOf(rule.sourceIP()));
                    }
                    else {
                        trafficS_In.matchIPv6Src(IpPrefix.valueOf(rule.sourceIP()));
                    }
                }

                if( rule.destinationIP() != null ) {
                    if( rule.protocol() == IPv4.PROTOCOL_TCP || rule.protocol() == IPv4.PROTOCOL_UDP) {
                        trafficS_In.matchIPDst(IpPrefix.valueOf(rule.destinationIP()));
                    }
                    else {
                        trafficS_In.matchIPv6Dst(IpPrefix.valueOf(rule.destinationIP()));
                    }
                }

                if( rule.sourcePort() != null ) {
                    TpPort SourcePort = TpPort.tpPort(Integer.parseInt(rule.sourcePort()));
                    if(rule.protocol() == IPv4.PROTOCOL_TCP || rule.protocol() == IPv6.PROTOCOL_TCP) {
                        trafficS_In.matchTcpSrc(SourcePort);
                    }
                    else {
                        trafficS_In.matchUdpSrc(SourcePort);
                    }
                }

                if( rule.destinationPort() != null ) {
                    TpPort DestinationPort = TpPort.tpPort(Integer.parseInt(rule.destinationPort()));
                    if(rule.protocol() == IPv4.PROTOCOL_TCP || rule.protocol() == IPv6.PROTOCOL_TCP) {
                        trafficS_In.matchTcpDst(DestinationPort);
                    }
                    else {
                        trafficS_In.matchUdpDst(DestinationPort);
                    }
                }

                if( rule.tagVlan() != null ) {
                    trafficS_In.matchVlanId(rule.tagVlan());
                }

                // rule
                org.onosproject.net.flow.FlowRule.Builder flowRuleB_In = DefaultFlowRule.builder();
                flowRuleB_In.withSelector(trafficS_In.build())
                        .withTreatment(trafficT.build())
                        .forDevice(DeviceEntry.getKey())
                        .makePermanent()
                        .withPriority(PriorityNum)
                        .fromApp(appID);

                flowRuleService.applyFlowRules(flowRuleB_In.build());
                log.info("First Node Entry=" + DeviceEntry + " Key=" + DeviceEntry.getKey() + " Value=" + DeviceEntry.getValue() + "\n");

            }
        }
    }

    private void EndFlowRule(ControllFlowRule rule, ApplicationId appID) {
        Iterator< Map.Entry<DeviceId,Set<PortNumber>> > Iterator = EndMap.entrySet().iterator();
        Map.Entry<DeviceId,Set<PortNumber>> entry;
        Set<PortNumber> PortSet;
        while( Iterator.hasNext() ) {
            entry = Iterator.next();
            PortSet = entry.getValue();
            // match
            TrafficSelector.Builder trafficS_In = DefaultTrafficSelector.builder();

            if( rule.tagVlan() != null ) {
                trafficS_In.matchVlanId(rule.tagVlan());
            }

            // action
            TrafficTreatment.Builder trafficT = DefaultTrafficTreatment.builder();
            trafficT.popVlan()
                    .immediate();

            for( PortNumber port:PortSet ) {
                trafficT.setOutput(port);
            }

            // rule
            org.onosproject.net.flow.FlowRule.Builder flowRuleB_In = DefaultFlowRule.builder();
            flowRuleB_In.withSelector(trafficS_In.build())
                    .withTreatment(trafficT.build())
                    .forDevice(entry.getKey())
                    .makePermanent()
                    .withPriority(PriorityNum)
                    .fromApp(appID);

            flowRuleService.applyFlowRules(flowRuleB_In.build());
        }
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
            log.info("Arrange Node:"+NodeMap.toString());
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
            log.info("Arrange First:"+FirstMap.toString());
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
            log.info("Arrange End:"+EndMap.toString());
        }

    }
}
