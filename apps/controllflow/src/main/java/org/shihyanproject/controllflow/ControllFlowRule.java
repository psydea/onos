package org.shihyanproject.controllflow;

import com.google.common.base.MoreObjects;
import org.onlab.packet.EthType;
import org.onlab.packet.IpAddress;
import org.onlab.packet.VlanId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.PortNumber;

import java.util.List;

public final class ControllFlowRule {
    private final List<ConnectPoint> srcPoint;
    private final List<ConnectPoint> dstPoint;
    private final EthType.EtherType ethernetType;
    private final byte protocol;
    private final String sourceIP;
    private final String destinationIP;
    private final String sourcePort;
    private final String destinationPort;
    private final String appId;
    private final VlanId tagVlan;

    private ControllFlowRule() {
        this.srcPoint = null;
        this.dstPoint = null;
        this.ethernetType = null;
        this.protocol = 0;
        this.sourceIP = null;
        this.destinationIP = null;
        this.sourcePort = null;
        this.destinationPort = null;
        this.appId = null;
        this.tagVlan = null;
    }

    /**
     * Create a new Flow rule.
     *
     * @param srcPoint     source DeviceId and Port
     * @param dstPoint    destination DeviceId and Port
     */
    private ControllFlowRule(List<ConnectPoint> srcPoint, List<ConnectPoint> dstPoint, EthType.EtherType EthernetType, byte Protocol, String SourceIP, String DestinationIP, String SourcePort, String DestinationPort, String ApplicationId, VlanId tagVlan) {
        this.srcPoint = srcPoint;
        this.dstPoint = dstPoint;
        this.ethernetType = EthernetType;
        this.protocol = Protocol;
        this.sourceIP = SourceIP;
        this.destinationIP = DestinationIP;
        this.sourcePort = SourcePort;
        this.destinationPort = DestinationPort;
        this.appId = ApplicationId;
        this.tagVlan = tagVlan;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private List<ConnectPoint> srcPoint = null;
        private List<ConnectPoint> dstPoint = null;
        private EthType.EtherType ethernetType = null;
        private byte protocol = 0;
        private String sourceIP = null;
        private String destinationIP = null;
        private String sourcePort = null;
        private String destinationPort = null;
        private String appId = null;
        private VlanId tagVlan = null;

        private Builder() {

        }

        public Builder srcPoint(List<ConnectPoint> srcPoint) {
            this.srcPoint = srcPoint;
            return this;
        }

        public Builder dstPoint(List<ConnectPoint> dstPoint) {
            this.dstPoint = dstPoint;
            return this;
        }

        public Builder ethernetType(EthType.EtherType ethernetType) {
            this.ethernetType = ethernetType;
            return this;
        }

        public Builder protocol(byte protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder srcIP(String sourceIP) {
            this.sourceIP = sourceIP;
            return this;
        }

        public Builder dstIP(String destinationIP) {
            this.destinationIP = destinationIP;
            return this;
        }

        public Builder srcPort(String sourcePort) {
            this.sourcePort = sourcePort;
            return this;
        }

        public Builder dstPort(String destinationPort) {
            this.destinationPort  = destinationPort;
            return this;
        }

        public Builder appId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder tagVlan(VlanId tagVlan) {
            this.tagVlan = tagVlan;
            return this;
        }

        public ControllFlowRule build() {
            return new ControllFlowRule(srcPoint, dstPoint, ethernetType, protocol, sourceIP, destinationIP, sourcePort, destinationPort, appId, tagVlan);
        }
    }

    public List<ConnectPoint> srcPoint() {
        return srcPoint;
    }

    public List<ConnectPoint> dstPoint() {
        return dstPoint;
    }

    public EthType.EtherType ethernetType() {
        return ethernetType;
    }

    public byte protocol() {
        return protocol;
    }

    public String sourceIP() {
        return sourceIP;
    }

    public String destinationIP() {
        return destinationIP;
    }

    public String sourcePort() {
        return sourcePort;
    }

    public String destinationPort() {
        return destinationPort;
    }

    public String appId() {
        return appId;
    }

    public VlanId tagVlan() {
        return tagVlan;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("srcPorint",srcPoint)
                .add("dstPoint",dstPoint)
                .add("ethernetType",ethernetType.toString())
                .add("protocol",protocol)
                .add("sourceIP",sourceIP)
                .add("destinationIP",destinationIP)
                .add("sourcePort",sourcePort)
                .add("destinationPort",destinationPort)
                .add("appId",appId.toString())
                .add("tagVlan",tagVlan.toString())
                .toString();
    }
}
