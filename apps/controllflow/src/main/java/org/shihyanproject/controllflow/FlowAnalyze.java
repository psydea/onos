package org.shihyanproject.controllflow;

import org.onlab.packet.Ethernet;
import org.onlab.packet.VlanId;
import org.onosproject.core.ApplicationId;
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

    // Store First
    private List<ConnectPoint> FirstNodeList = new ArrayList<>();
    private List<ConnectPoint> EndNodeList = new ArrayList<>();
    private List<ConnectPoint> NodeList = new ArrayList<>();

    private Set<ConnectPoint> FirstNodeSet = new LinkedHashSet<>();
    private Set<ConnectPoint> EndNodeSet = new LinkedHashSet<>();
    private Set<ConnectPoint> NodeSet = new LinkedHashSet<>();
    protected List<VlanId> Vlanlist;
    private Map<DeviceId,Set<PortNumber>> NodeMap = new HashMap<>();
    private int PriorityNum = 60000;
    protected ApplicationId appId;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PathService pathService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    public void createFlowEntry(List<ConnectPoint> src,List<ConnectPoint> dst) {
        DeviceId device_src,device_dst;
        VlanId vlanId = chooseVlan();
        for(int i=0;i<src.size();i++) {
            device_src = src.get(i).deviceId();
            device_dst = dst.get(i).deviceId();
            if( !FirstNodeSet.contains(src.get(i)) ) {
                FirstNodeSet.add(src.get(i));
                FirstNodeList.add(src.get(i));
                log.info("First Node = " + src.get(i));
            }
            if( !EndNodeSet.contains(dst.get(i)) ) {
                EndNodeSet.add(dst.get(i));
                EndNodeList.add(dst.get(i));
                log.info("End Node = " + dst.get(i));
            }
            this.GetPaths(device_src,device_dst);
        }
        for(int i=0;i<src.size();i++) {
            FirstFlowRule(src.get(i),FirstNodeList.get(i).port(),vlanId);
        }

        NodeFlowRule(vlanId);

        for(int i=0;i<dst.size();i++) {
            FirstFlowRule(dst.get(i),EndNodeList.get(i).port(),vlanId);
        }
    }

    private void GetPaths(DeviceId DeviceId_Src, DeviceId DeviceId_Dst) {
        log.info(DeviceId_Src.toString()+" "+DeviceId_Src.toString());
        Set<Path> pathTable = pathService.getPaths(DeviceId_Src, DeviceId_Dst);
        Object [] ObjectPath = pathTable.toArray();
        log.info("ObjectPath="+ObjectPath.length);
        DefaultPath PathInfo = (DefaultPath) ObjectPath[0];
        List<Link> LinkList = PathInfo.links();
        DefaultLink LinkInfo;
        ConnectPoint src,dst;
        NodeList.clear();
        for(int i=0;i<LinkList.size();i++) {
            LinkInfo = (DefaultLink) LinkList.get(i);
            src = LinkInfo.src();
            dst = LinkInfo.dst();
            if( (i!=0) && !NodeSet.contains(src) ) {
                NodeSet.add(src);
                NodeList.add(src);
                log.info("Node = " + src);
            }
        }

        ArrangeNodeInfo();
    }

    private void NodeFlowRule(VlanId vlan) {
        Iterator< Map.Entry<DeviceId,Set<PortNumber>> > itera = NodeMap.entrySet().iterator();
        Map.Entry<DeviceId,Set<PortNumber>> entry;
        Set<PortNumber> PortSet = new LinkedHashSet<>();
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
                    .fromApp(appId)
                    .makePermanent()
                    .withPriority(PriorityNum);

            flowRuleService.applyFlowRules(flowRuleB_In.build());
            log.info("Entry=" + entry + " Key=" + entry.getKey() + " Value=" + entry.getValue() + "\n");
        }
    }

    private void FirstFlowRule(ConnectPoint device, PortNumber out_port, VlanId vlan) {
        // match
        TrafficSelector.Builder trafficS_In = DefaultTrafficSelector.builder();
        trafficS_In.matchEthType(Ethernet.TYPE_IPV4)
                .matchInPort(device.port());

        // action
        TrafficTreatment.Builder trafficT = DefaultTrafficTreatment.builder();
        trafficT.pushVlan()
                .setVlanId(vlan)
                .setOutput(out_port)
                .immediate();

        // rule
        FlowRule.Builder flowRuleB_In = DefaultFlowRule.builder();
        flowRuleB_In.withSelector(trafficS_In.build())
                .withTreatment(trafficT.build())
                .forDevice(device.deviceId())
                .fromApp(appId)
                .makePermanent()
                .withPriority(PriorityNum);

        flowRuleService.applyFlowRules(flowRuleB_In.build());
    }

    private void EndFlowRule(ConnectPoint device,PortNumber inPort,VlanId vlan) {
        // match
        TrafficSelector.Builder trafficS_In = DefaultTrafficSelector.builder();
        trafficS_In.matchEthType(Ethernet.TYPE_IPV4)
                .matchVlanId(vlan);

        // action
        TrafficTreatment.Builder trafficT = DefaultTrafficTreatment.builder();
        trafficT.popVlan()
                .setOutput(device.port())
                .immediate();

        // rule
        FlowRule.Builder flowRuleB_In = DefaultFlowRule.builder();
        flowRuleB_In.withSelector(trafficS_In.build())
                .withTreatment(trafficT.build())
                .forDevice(device.deviceId())
                .fromApp(appId)
                .makePermanent()
                .withPriority(PriorityNum);

        flowRuleService.applyFlowRules(flowRuleB_In.build());
    }

    private VlanId chooseVlan() {
        short vlan = 2;
        while(Vlanlist.contains(VlanId.vlanId(vlan))) {
            vlan += 1;
        }
        Vlanlist.add(VlanId.vlanId(vlan));

        return VlanId.vlanId(vlan);
    }

    private void ArrangeNodeInfo() {
        DeviceId node;
        PortNumber port;
        for( int i=0;i<NodeList.size();i++ ) {
            node = NodeList.get(i).deviceId();
            port = NodeList.get(i).port();
            Set<PortNumber> PortSet = new LinkedHashSet<>();
            if( NodeMap.containsKey(node) ) {
                PortSet = NodeMap.get(node);
            }
            PortSet.add(port);
            NodeMap.put(node,PortSet);
        }


    }
}
