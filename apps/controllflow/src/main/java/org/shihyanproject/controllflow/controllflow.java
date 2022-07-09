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
    int times=0;

    List<VlanId> Vlanlist = new ArrayList<>();

    int num=10;

    // Init
    @Activate
    protected void activate() {
        componentConfigService.registerProperties(getClass());
        deviceService.getClass();
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

    /*  Add Flow Entry
        Input: 1. Source ConnectPoint List
               2. Destination ConnectPoint List
     */
    public void AddFlowEntry(List<ConnectPoint> src, List<ConnectPoint> dst) {
        FlowAnalyze FA = new FlowAnalyze();
        times+=1;
        FA.times = times;
        FA.coreService = coreService;
        FA.pathService = pathService;
        FA.flowRuleService = flowRuleService;
        FA.Vlanlist = Vlanlist;
        FA.createFlowEntry(src,dst);
    }

    public List<FlowEntry> ShowFlowEntry() {
        ApplicationId appid;
        List<FlowEntry>  FlowEntryList = new ArrayList<>();
        for( int i=0 ; i<times ; i++ ) {
            appid = coreService.registerApplication(String.valueOf(times));
            for( FlowEntry flow:flowRuleService.getFlowEntriesById(appid)) {
                FlowEntryList.add(flow);
            }
        }
        return FlowEntryList;
    }
}


