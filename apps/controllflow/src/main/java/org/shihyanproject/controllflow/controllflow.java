package org.shihyanproject.controllflow;

import org.onlab.packet.Ethernet;
import org.onlab.packet.IPv4;
import org.onlab.packet.VlanId;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.DisjointPath;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.Path;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.host.HostService;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.topology.PathService;
import org.onosproject.net.topology.TopologyGraph;
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
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

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
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PathService pathService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    TopologyGraph TGraph;
    HostId HostId_Src,HostId_Des;
    ApplicationId appId;

    List<VlanId> Vlanlist = new ArrayList<>();

    int num=10;

    /*
    // Get Srouce IP and Destination IP through IPv4 Packet
    private PacketProcessor processor = new PacketProcessor() {
        @Override
        public void process(PacketContext context) {
            InboundPacket Ipkt = context.inPacket();
            Ethernet ethPkt = Ipkt.parsed();

            if (ethPkt.getEtherType() == Ethernet.TYPE_IPV4) {
                MacAddress MacSrc = ethPkt.getSourceMAC();
                MacAddress MacDes = ethPkt.getDestinationMAC();
                HostId_Src = HostId.hostId(MacSrc);
                HostId_Des = HostId.hostId(MacDes);
                log.info(MacSrc.toString());
                log.info(HostId_Src.toString());
                log.info(getPaths_test(HostId_Src,HostId_Des));
            }

        }
    };
*/
    // Init
    @Activate
    protected void activate() {
        componentConfigService.registerProperties(getClass());
        deviceService.getClass();
        coreService.getClass();
        flowRuleService.getClass();
        appId = coreService.registerApplication("org.onosproject.controllflow");
        //packetService.addProcessor(processor,PacketProcessor.director(0));
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

    // To print all device id
    public String getHost() {
        String show = "\n";
        Iterable<Device> devices = deviceService.getDevices();
        for (Device d : devices) {
            Set<Host> hostTable = hostService.getConnectedHosts(d.id());
            for (Host host : hostTable) {
                show += (host.toString() + "\n");
            }
        }
        return show;
    }

    /* To get device info
       return string type of value*/
    public String getDevices() {
        String show = "\n";
        Iterable<Device> devices = deviceService.getDevices();
        for (Device d : devices) {
            show += (d.toString() + "\n");
        }
        return show;
    }

    /* To get path,disjoin path and k shortest path through device id
        return string type of value*/
    public String getPaths_test(DeviceId DeviceId_Src, DeviceId DeviceId_Des) {
        Set<Path> pathTable = pathService.getPaths(DeviceId_Src, DeviceId_Des);
        Set<DisjointPath> DisjoingPathTable = pathService.getDisjointPaths(DeviceId_Src,DeviceId_Des);
        Stream<Path> KShortestPathTable = pathService.getKShortestPaths(DeviceId_Src,DeviceId_Des);
        String printPath = "printPath: "+pathTable.toString()+"\n";
        String printDisjoinPath = "printDisjoinPath: " + DisjoingPathTable.toString()+"\n";
        Object [] ObjectKShortestPath = KShortestPathTable.toArray();
        String printKShortestPath = "printKShortestPath: ";
        for (Object O:ObjectKShortestPath) {
            printKShortestPath += O.toString();
            printKShortestPath += " ";
        }
        printKShortestPath += "\n";
        printPath+=printDisjoinPath;

        return printPath;
    }

    /* To get flow entry
       return string type of value */
    public String getFlowEntry(Iterable<Device> devices) {
        String flowEntry = "";
        for (Device d : devices) {
            for (FlowEntry r : flowRuleService.getFlowEntries(d.id())) {
                flowEntry += r.toString();
                flowEntry += "\n";
            }
        }
        return flowEntry;
    }

    /* To get flow entry
       return string type of value  */
    public String getFlowEntry(DeviceId deviceID) {
        String flowEntry = "";
        for (FlowEntry r : flowRuleService.getFlowEntries(deviceID)) {
            log.info( "Flow App ID" + String.valueOf(r.appId()));
            log.info( "Application ID" + String.valueOf(appId.id()));
            flowEntry += r.toString();
            flowEntry += "\n";
        }
        return flowEntry;
    }

    public void createFlowRuleIn(DeviceId devices,VlanId vlan, PortNumber HostPort,PortNumber DevicePort) {
        // match
        TrafficSelector.Builder trafficS_In = DefaultTrafficSelector.builder();
        trafficS_In.matchEthType(Ethernet.TYPE_IPV4)
                .matchIPProtocol(IPv4.PROTOCOL_ICMP);

        // action
        TrafficTreatment.Builder trafficT = DefaultTrafficTreatment.builder();
        trafficT.pushVlan()
                .setVlanId(vlan)
                .setOutput(DevicePort)
                .immediate();

        // rule
        FlowRule.Builder flowRuleB_In = DefaultFlowRule.builder();
        flowRuleB_In.withSelector(trafficS_In.build())
                .withTreatment(trafficT.build())
                .forDevice(devices)
                .fromApp(appId)
                .makePermanent()
                .withPriority(num);
        flowRuleService.applyFlowRules(flowRuleB_In.build());
        num++;
    }

    public void createFlowRuleOut(DeviceId devices, VlanId vlan, PortNumber HostPort) {
        // match
        TrafficSelector.Builder trafficS_Out = DefaultTrafficSelector.builder();
        trafficS_Out.matchEthType(Ethernet.TYPE_IPV4)
                .matchIPProtocol(IPv4.PROTOCOL_ICMP)
                .matchVlanId(vlan);
        // action
        TrafficTreatment.Builder trafficT_Out = DefaultTrafficTreatment.builder();
        trafficT_Out.popVlan()
                .setOutput(HostPort);
        // rule
        FlowRule.Builder flowRuleB_Out = DefaultFlowRule.builder();
        flowRuleB_Out.withSelector(trafficS_Out.build())
                .withTreatment(trafficT_Out.build())
                .forDevice(devices)
                .fromApp(appId)
                .makePermanent()
                .withPriority(num);
        flowRuleService.applyFlowRules(flowRuleB_Out.build());
        num++;
    }

    public void GetPaths(List<ConnectPoint> src, List<ConnectPoint> dst) {
        FlowAnalyze fa = new FlowAnalyze();
        fa.appId = appId;
        fa.pathService = pathService;
        fa.flowRuleService = flowRuleService;
        fa.Vlanlist = Vlanlist;
        fa.createFlowEntry(src,dst);
    }
}


