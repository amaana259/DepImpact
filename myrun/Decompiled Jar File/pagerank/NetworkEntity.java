/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package pagerank;

import pagerank.Entity;

public class NetworkEntity
extends Entity {
    private String srcAddress;
    private String dstAddress;
    private String sPort;
    private String dPort;
    private String location;

    public NetworkEntity(double reputation, long id, int hopCount, String time1, String time2, String srcAddress, String dstAddress, String sPort, String dPort, long uniqID) {
        super(reputation, id, hopCount, time1, time2, uniqID);
        this.srcAddress = srcAddress;
        this.dstAddress = dstAddress;
        this.sPort = sPort;
        this.dPort = dPort;
    }

    public NetworkEntity(double reputation, long id, int hopCount, String time1, String time2, String srcAddress, String dstAddress, String sPort, String dPort, long uniqID, String host) {
        super(reputation, id, hopCount, time1, time2, uniqID);
        this.srcAddress = srcAddress;
        this.dstAddress = dstAddress;
        this.sPort = sPort;
        this.dPort = dPort;
        this.location = host;
    }

    public NetworkEntity() {
    }

    public String getSrcAddress() {
        return this.srcAddress;
    }

    public String getDstAddress() {
        return this.dstAddress;
    }

    public String getSrcAndDstIP() {
        return this.srcAddress + ":" + this.sPort + "->" + this.dstAddress + ":" + this.dPort;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String s2) {
        this.location = s2;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NetworkEntity)) {
            return false;
        }
        NetworkEntity that = (NetworkEntity)o;
        if (this.srcAddress != null ? !this.srcAddress.equals(that.srcAddress) : that.srcAddress != null) {
            return false;
        }
        if (this.dstAddress != null ? !this.dstAddress.equals(that.dstAddress) : that.dstAddress != null) {
            return false;
        }
        if (this.sPort != null ? !this.sPort.equals(that.sPort) : that.sPort != null) {
            return false;
        }
        return this.dPort != null ? this.dPort.equals(that.dPort) : that.dPort == null;
    }

    public int hashCode() {
        int result = this.srcAddress != null ? this.srcAddress.hashCode() : 0;
        result = 31 * result + (this.dstAddress != null ? this.dstAddress.hashCode() : 0);
        result = 31 * result + (this.sPort != null ? this.sPort.hashCode() : 0);
        result = 31 * result + (this.dPort != null ? this.dPort.hashCode() : 0);
        return result;
    }
}

