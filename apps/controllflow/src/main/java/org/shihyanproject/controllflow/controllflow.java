package org.shihyanproject.controllflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
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
        root.put("Success",200);
        root.put("Message","Search FlowRule Success!");
        root.putPOJO("Result", arrayNode);

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
        FlowAnalyze newFlowRule = new FlowAnalyze();
        newFlowRule.coreService = coreService;
        newFlowRule.flowRuleService = flowRuleService;
        newFlowRule.pathService = pathService;

        return newFlowRule.createRule(rule);
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


