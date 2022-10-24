package org.shihyanproject.controllflow;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onlab.packet.*;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.rest.AbstractWebResource;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.onlab.util.Tools.readTreeFromStream;

@Path("flows")
public class ControllflowWebResource extends AbstractWebResource {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;


    /**
     * Get specific application id.
     * Returns specific application id of flow rule.
     * Input:
     *      Param: AppID
     * Output:
     *      @return 200 OK
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/search/{AppID}")
    public Response searchFlow(@PathParam("AppID") String AppID) {
        ObjectNode root = get(controllflow.class).SearchFlow(AppID);
        return Response.ok(root.toString(),MediaType.APPLICATION_JSON_TYPE).build();
    }

    /**
     * Get specific application id.
     * Returns specific application id of flow rule.
     *
     * @return 200 OK
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addFlow(InputStream stream) {
        try {
            ControllFlowRule flow = jsonToRule(stream);
            ObjectNode GetMessage = get(controllflow.class).AddFlowRuleAPI(flow);
            log.info(GetMessage.get("Code").asText());
            return Response.status(GetMessage.get("Code").asInt()).entity(GetMessage).build();
        } catch (Exception e) {
            log.info(e.toString());
            return Response.serverError().entity(e.getMessage()).build();
        }

    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteFlow(InputStream stream) {
        try {
            JsonNode node;
            String Appid;
            node = readTreeFromStream(mapper(), stream);
            Appid = node.get("ApplicationID").asText();
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode RequestMessage = mapper.createObjectNode();

            if(get(controllflow.class).DeleteFlowRuleAPI(Appid)) {
                RequestMessage.put("Code",200);
                RequestMessage.put("Message","Delete flow rule success!");

            }
            else {
                RequestMessage.put("Code",500);
                RequestMessage.put("Message","Failed to delete flow rule!");
            }
            return Response.status(RequestMessage.get("Code").asInt()).entity(RequestMessage).build();
        } catch (Exception e) {
            log.info(e.toString());
            return Response.serverError().entity(e.getMessage()).build();
        }

    }

    private ControllFlowRule jsonToRule(InputStream stream) {
        JsonNode node;

        try {
            node = readTreeFromStream(mapper(), stream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable", e);
        }

        ControllFlowRule.Builder rule = ControllFlowRule.builder();

        rule.srcPoint(getIPinJSONArray(node,"Source"));
        rule.dstPoint(getIPinJSONArray(node,"Destination"));

        String AppId = node.get("ApplicationID").asText(null);
        if( AppId != null ) {
            rule.appId(AppId);
        }

        String tag = node.get("TagVlan").asText(null);
        if( tag != null) {
            VlanId vlantag = VlanId.vlanId(tag);
            rule.tagVlan(vlantag);
        }
        log.info("162");


        if(node.get("Filter") != null) {
            getFilterInJSON(rule,node);
        }
        log.info("171");

        return rule.build();
    }

    private void getFilterInJSON(ControllFlowRule.Builder rule, JsonNode node) {
        try {
            Iterator<JsonNode> elements = node.get("Filter").elements();
            String[] IndexArray = {"EthernetType","Protocol","SourceIP","SourcePort","DestinationIP","DestinationPort"};
            String ValueString;
            int i = 0;
            EthType.EtherType ProtocolMark = null;
            while(elements.hasNext()){
                JsonNode SourceValue = elements.next();
                ValueString = SourceValue.get(IndexArray[i]).asText();
                if (!ValueString.equals("")) {
                    switch (i) {
                        // EthernetType
                        case 0:
                            try {
                                ProtocolMark = EthType.EtherType.valueOf(ValueString);
                                rule.ethernetType(ProtocolMark);
                            }catch (EnumConstantNotPresentException e) {
                                throw new IllegalArgumentException("EthType Error",e);
                            }
                            break;
                        // Protocol
                        case 1:
                            try {
                                if(ProtocolMark == EthType.EtherType.IPV4) {
                                    switch(ValueString) {
                                        case "TCP":
                                            rule.protocol(IPv4.PROTOCOL_TCP);
                                            break;
                                        case "UDP":
                                            rule.protocol(IPv4.PROTOCOL_UDP);
                                            break;
                                            /*
                                        case "ICMP":
                                            rule.protocol(IPv4.PROTOCOL_ICMP);
                                            break;
                                        case "IGMP":
                                            rule.protocol(IPv4.PROTOCOL_IGMP);
                                            break;
                                        case "PIM":
                                            rule.protocol(IPv4.PROTOCOL_PIM);
                                            break;*/

                                    }
                                }
                                else if(ProtocolMark == EthType.EtherType.IPV6) {
                                    switch(ValueString) {
                                        case "TCP":
                                            rule.protocol(IPv6.PROTOCOL_TCP);
                                            break;
                                        case "UDP":
                                            rule.protocol(IPv6.PROTOCOL_UDP);
                                            break;
                                            /*
                                        case "ICMP":
                                            rule.protocol(IPv6.PROTOCOL_ICMP6);
                                            break;
                                        case "DSTOPT":
                                            rule.protocol(IPv6.PROTOCOL_DSTOPT);
                                            break;
                                        case "ESP":
                                            rule.protocol(IPv6.PROTOCOL_ESP);
                                            break;
                                        case "FRAG":
                                            rule.protocol(IPv6.PROTOCOL_FRAG);
                                            break;
                                        case "HOPOPT":
                                            rule.protocol(IPv6.PROTOCOL_HOPOPT);
                                            break;
                                        case "ROUTING":
                                            rule.protocol(IPv6.PROTOCOL_ROUTING);
                                            break;*/
                                    }
                                }
                            }catch (NullPointerException e) {
                                throw new IllegalArgumentException("IP Protocol Error!",e);
                            }
                            break;
                        // sourceIP
                        case 2:
                            try {
                                rule.srcIP(ValueString);
                            }catch (NullPointerException e) {
                                throw new IllegalArgumentException("Source IP Error!");
                            }
                            break;
                        // Source port number
                        case 3:
                            try {
                                rule.srcPort(ValueString);
                            }catch (Exception e) {
                                throw new IllegalArgumentException("Port Number Error!",e);
                            }
                            break;
                        // Destination IP
                        case 4:
                            try {
                                rule.dstIP(ValueString);
                            }catch (NullPointerException e) {
                                throw new IllegalArgumentException("Source IP Error!");
                            }
                            break;
                        // Destination port number
                        case 5:
                            try {
                                rule.dstPort(ValueString);
                            }catch (Exception e) {
                                throw new IllegalArgumentException("Destination Port Number Error!",e);
                            }
                            break;
                    }
                }
                i+=1;
            }

        }catch(Exception e) {
            log.info(e.toString());
            throw new IllegalArgumentException("Filter Error!",e);
        }

    }

    private List<ConnectPoint> getIPinJSONArray(JsonNode node,String target) {
        JsonNode array = node.get(target);
        List<ConnectPoint> PointRule = new ArrayList<>();
        String ValueString;
        String[] temp;
        String delimeter = ":";
        for ( JsonNode obj : array) {
            ValueString = obj.toString();
            ValueString = ValueString.substring(1,ValueString.length()-1);
            temp = ValueString.split(delimeter);
            ValueString = "of:"+temp[0]+"/"+temp[1];
            PointRule.add(ConnectPoint.deviceConnectPoint(ValueString));
        }

        return PointRule;
    }
}
