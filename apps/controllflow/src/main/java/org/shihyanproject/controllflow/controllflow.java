package org.shihyanproject.controllflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onlab.packet.Ethernet;
import org.onosproject.cfg.ComponentConfigService;
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
import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.instructions.Instruction;
import org.onosproject.net.topology.PathService;
import org.onosproject.net.topology.TopologyService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.onlab.util.Tools.get;

@Component(immediate = true,
        service = controllflow.class,
        property = {
                "someProperty=Some Default String Value",
        })

public class controllflow {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Some configurable property.
     */
    private String someProperty;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ComponentConfigService componentConfigService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected TopologyService topologyService;


    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PathService pathService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    ApplicationId appId;
    Set<ConnectPoint> FirstNodeSet = new LinkedHashSet<>();
    Set<ConnectPoint> EndNodeSet = new LinkedHashSet<>();
    Set<ConnectPoint> NodeSet = new LinkedHashSet<>();
    Map<DeviceId,Set<PortNumber>> FirstMap = new HashMap<>();
    Map<DeviceId,Set<PortNumber>> EndMap = new HashMap<>();
    Map<DeviceId,Set<PortNumber>> NodeMap = new HashMap<>();
    Map<DeviceId,Set<PortNumber>> FirstPort = new HashMap<>();

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode RequestMessage;

    int PriorityNum = 60000;
    int times = 10;


    // Init
    @Activate
    protected void activate() {
        componentConfigService.registerProperties(getClass());
        coreService.getClass();
        flowRuleService.getClass();
        appId = coreService.registerApplication("org.onosproject.controllflow");
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        componentConfigService.unregisterProperties(getClass(), false);
        log.info("Stopped");

    }

    @Modified
    public void modified(ComponentContext context) {
        Dictionary<?, ?> properties = context != null ? context.getProperties() : new Properties();
        if (context != null) {
            someProperty = get(properties, "someProperty");
        }
        log.info("Reconfigured");
    }

    /*  Delete Flow Entry
        Input: Application ID
     */
    public void DeleteFlowEntry(String id) {
        ApplicationId DeleteApplicationID = coreService.registerApplication(String.valueOf(id));
        flowRuleService.removeFlowRulesById(DeleteApplicationID);
    }

    /**
     * Add Flow Rule
     *
     * Input:
     *      1. Source ConnectPoint List
     *      2. Destination ConnectPoint List
     */
    public List<FlowEntry> ShowFlowEntry() {
        ApplicationId appid;
        List<FlowEntry>  FlowEntryList = new ArrayList<>();
        for( int i=0 ; i<times ; i++ ) {
            appid = coreService.registerApplication(String.valueOf(times));
            for( FlowEntry flow:flowRuleService.getFlowEntriesById(appid)) {
                FlowEntryList.add(flow);
            }
        }
        log.info("Show Flow Entry:"+FlowEntryList.toString());
        return FlowEntryList;
    }

    /**
     * Get all FlowEntry.
     * Returns array of all FlowEntry.
     *
     * @return 200 OK
     */
    /*
        Input:
            rule : ControllFlowRule
        Output:
            true or false
     */
    public ObjectNode SearchFlow(String AppID) {
        short groupId = 0;
        ApplicationId appid = coreService.getAppId(AppID);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        ArrayNode arrayNode = mapper.createArrayNode();

        for( FlowRule flowrule : flowRuleService.getFlowRulesByGroupId(appid,groupId)) {
            ObjectNode node = mapper.createObjectNode();
            if ( flowrule.id() != null ) {
                node.put("FlowID", flowrule.id().toString());
            }
            if( flowrule.appId() > 0 ) {
                node.put("ApplicationID", AppID);
            }
            if ( flowrule.deviceId() != null ) {
                node.put("DeviceID", flowrule.deviceId().toString());
            }
            if ( String.valueOf(flowrule.priority()) != null ) {
                node.put("Priority", String.valueOf(flowrule.priority()));
            }
            if ( flowrule.selector() != null ) {
                TrafficSelector selector = flowrule.selector();
                Set<Criterion> Critera = selector.criteria();
                ArrayNode selctorArray = mapper.createArrayNode();
                for (Criterion c : Critera) {
                    ObjectNode s = mapper.createObjectNode();
                    String[] temp = c.toString().split(":");
                    if (c.type() == Criterion.Type.ETH_TYPE) {
                        s.put("EthernetType", temp[1]);
                    }
                    if (c.type() == Criterion.Type.IN_PORT) {
                        s.put("InPort", temp[1]);
                    }
                    if (c.type() == Criterion.Type.VLAN_VID) {
                        s.put("VlanID", temp[1]);
                    }
                    selctorArray.add(s);
                }
                node.putPOJO("Selector", selctorArray);
            }
            if ( flowrule.treatment() != null ) {
                TrafficTreatment treatment = flowrule.treatment();
                List<Instruction> immediateList = treatment.immediate();
                ObjectNode treamentObject = mapper.createObjectNode();
                ArrayNode immediateArray = mapper.createArrayNode();
                ObjectNode immediateObject = mapper.createObjectNode();
                for (Instruction instructions : immediateList) {
                    if (instructions.type() == Instruction.Type.L2MODIFICATION) {
                        if (instructions.toString().equals("VLAN_POP")) {
                            immediateObject.put("VlanAction", "PopVlan");
                        } else {
                            String[] temp = instructions.toString().split(":");
                            immediateObject.put("VlanAction", "PushVlan");
                            immediateObject.put("VlanID", temp[1]);
                        }
                    }
                    if (instructions.type() == Instruction.Type.OUTPUT) {
                        String[] temp = instructions.toString().split(":");
                        immediateObject.put("Output", temp[1]);
                    }
                }
                immediateArray.add(immediateObject);
                treamentObject.putPOJO("Imediate", immediateArray);
                node.putPOJO("Treament", treamentObject);
            }
            arrayNode.add(node);
        }
        root.put("Success",1);
        root.put("Message","Search FlowRule Success!");
        root.putPOJO("Flow List", arrayNode);

        log.info("Search Flow:"+root.toString());

        return root;
    }

    /**
     * Add Flow Rule using API method
     * Input:
     *      rule : ControllFlowRule
     * return true or false
     */
    public ObjectNode AddFlowRuleAPI(ControllFlowRule rule) {
        ApplicationId appId = coreService.registerApplication(rule.appId());
        RequestMessage = mapper.createObjectNode();
        try {
            for(int i=0;i<rule.srcPoint().size();i++) {
                if( !FirstNodeSet.contains(rule.srcPoint().get(i)) ) {
                    FirstNodeSet.add(rule.srcPoint().get(i));
                }
            }
        }catch (IndexOutOfBoundsException IndexOutException) {
            RequestMessage.put("Success",0);
            RequestMessage.put("Message","Source connectPoint index error");
            log.error("Source ConnectPoint Index Error");
            return RequestMessage;
        }catch (Exception e) {
            RequestMessage.put("Success",0);
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
            RequestMessage.put("Success",0);
            RequestMessage.put("Message","Destination connect point index error");
            log.error("Destination ConnectPoint Index Error");
            return RequestMessage;
        }catch (Exception e) {
            RequestMessage.put("Success",0);
            RequestMessage.put("Message","Destination node error");
            log.error("Destination Node Error");
            return RequestMessage;
        }

        try {
            for( ConnectPoint SourcePoint:FirstNodeSet ) {
                for( ConnectPoint DestinationPoint:EndNodeSet ) {
                    GetPaths( SourcePoint.deviceId(), DestinationPoint.deviceId() );
                }
            }
        }catch (Exception e) {
            RequestMessage.put("Success",0);
            RequestMessage.put("Message","Get paths error");
            log.error("Get Paths Error");
            return RequestMessage;
        }

        try {
            ArrangeNodeInfo("src");
        }catch (Exception e) {
            RequestMessage.put("Success",0);
            RequestMessage.put("Message","Arrange node information on source error");
            log.error("Arrange node info on source error!");
            return RequestMessage;
        }

        try {
            ArrangeNodeInfo("dst");
        }catch (Exception e) {
            RequestMessage.put("Success",0);
            RequestMessage.put("Message","Arrange node information on destination error");
            log.error("Arrange node info on destination error!");
            return RequestMessage;
        }

        try {
            FirstFlowRule(rule,appId);
        }catch (Exception e) {
            RequestMessage.put("Success",0);
            RequestMessage.put("Message","Create first node flow rule error");
            log.error("Create first node flow rule error!");
            return RequestMessage;
        }
        try {
            NodeFlowRule(rule,appId);
        }catch (Exception e) {
            RequestMessage.put("Success",0);
            RequestMessage.put("Message","Create pass node flow rule error");
            log.error("Create pass node flow rule error!");
            return RequestMessage;
        }
        try {
            EndFlowRule(rule,appId);
        }catch (Exception e) {
            RequestMessage.put("Success",0);
            RequestMessage.put("Message","Create end node flow rule error");
            log.error("Create end node flow rule error!");
            return RequestMessage;
        }
        RequestMessage.put("Success",1);
        RequestMessage.put("Message","Success");
        log.info("Created");

        return RequestMessage;
    }

    private void GetPaths(DeviceId Source, DeviceId Destination) {
        try {
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
            /*
            if( rule.protocol() != null ) {
                trafficS_In.matchIPProtocol(rule.protocol().value());
            }*/

            if( rule.ethernetType() != null ) {
                trafficS_In.matchEthType(rule.ethernetType().ethType().toShort());
            }else {
                trafficS_In.matchEthType(Ethernet.TYPE_IPV4);
            }

            if( rule.sourcePort() != null ) {
                trafficS_In.matchInPort(rule.sourcePort());
            }
            /*
            if( rule.destinationPort() != null ) {
                trafficS_In.match
            }*/

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
                // match
                TrafficSelector.Builder trafficS_In = DefaultTrafficSelector.builder();

                /*
                if( rule.protocol() != null ) {
                    trafficS_In.matchIPProtocol(rule.protocol().value());
                }*/

                if( rule.ethernetType() != null ) {
                    trafficS_In.matchEthType(rule.ethernetType().ethType().toShort());
                }else {
                    trafficS_In.matchEthType(Ethernet.TYPE_IPV4);
                }

                if( rule.sourcePort() != null ) {
                    trafficS_In.matchInPort(rule.sourcePort());
                }
                /*
                if( rule.destinationPort() != null ) {
                    trafficS_In.match
                }*/

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

            /*
            if( rule.protocol() != null ) {
                trafficS_In.matchIPProtocol(rule.protocol().value());
            }*/

            if( rule.ethernetType() != null ) {
                trafficS_In.matchEthType(rule.ethernetType().ethType().toShort());
            }else {
                trafficS_In.matchEthType(Ethernet.TYPE_IPV4);
            }

            if( rule.sourcePort() != null ) {
                trafficS_In.matchInPort(rule.sourcePort());
            }
            /*
            if( rule.destinationPort() != null ) {
                trafficS_In.match
            }*/

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

    /**
     * Delete Flow Rule using API method
     * Input:
     *      AppID : String
     * return true or false
     */
    public boolean DeleteFlowRuleAPI(String AppID) {
        try {
            ApplicationId Appid = coreService.getAppId(AppID);
            flowRuleService.removeFlowRulesById(Appid);
            return true;
        }catch (Exception e) {
            log.error("Delete Error");
            return false;
        }
    }


}


